import java.util.*;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;

import toxi.geom.Plane;
import toxi.geom.Vec3D;
import toxi.math.noise.SimplexNoise;

/**
Ripples in a pond
*/
class Pond extends TSPattern {

  // Rendering is O(cubes * ripples), so limit the number of concurrent ripples
  final static int maxRipples = 20;

  private class Ripple {
    float hue;
    float saturation;
    // Point from which this ripple originates
    LXVector origin;
    // Grows as the ripple expands
    double radius;
    
    Ripple(float hue, float saturation, LXVector origin) {
      this.hue = hue;
      this.saturation = saturation;
      this.origin = origin;
      this.radius = 0;
    }
  }
  
  private ArrayList<Ripple> ripples = new ArrayList<Ripple>();
  private LXVector[] corners;
  LXVector modelCenter;
  
  final BasicParameter speedParam = new BasicParameter("speed", 14, 4, 40);
  final BasicParameter sizeParam = new BasicParameter("size", .5, .1, 5);
  final BasicParameter amountParam = new BasicParameter("amount", .3, .1, 1);

  Pond(LX lx) {
    super(lx);
    addParameter(speedParam);
    addParameter(sizeParam);
    addParameter(amountParam);
    
    // Build array of the corners of the model's cube;
    // used to determine if a ripple is outside the model's space.
    corners = new LXVector[]{
      new LXVector(model.xMin, model.yMin, model.zMin),
      new LXVector(model.xMax, model.yMin, model.zMin),
      new LXVector(model.xMin, model.yMax, model.zMin),
      new LXVector(model.xMax, model.yMax, model.zMin),
      new LXVector(model.xMin, model.yMin, model.zMax),
      new LXVector(model.xMax, model.yMin, model.zMax),
      new LXVector(model.xMin, model.yMax, model.zMax),
      new LXVector(model.xMax, model.yMax, model.zMax),
    };
    // TODO: model.center is defined in current LXStudio
    modelCenter = new LXVector(model.cx, model.cy, model.cz);
  }
  
  public void run(double deltaMs) {
    ListIterator<Ripple> iter = ripples.listIterator();
    float rippleWidth = 200 * sizeParam.getValuef();
    while (iter.hasNext()) {
      Ripple ripple = iter.next();
      // Increase ripple's radius based on deltaMS and our speed param
      ripple.radius += deltaMs / 500 * Math.pow(speedParam.getValuef(), 2);
      // Make sure the ripple's current radius plus width is inside at least one of the bounds corners
      boolean inBounds = false;
      // TODO: Once we have new LXStudio we can probably do:
      // if (ripple.origin.dist(model.center) + ripple.radius + rippleWidth > model.rcMax) {
      for (LXVector corner : corners) {
        if (ripple.radius - rippleWidth < ripple.origin.dist(corner)) {
          // Rendered ripple is inside this corner of the model's bounds, so we shouldn't delete it.
          inBounds = true;
          break;
        }
      }
      if (!inBounds) {
        // Ripple is completely outside the bounds of our model; we can delete it.
        iter.remove();
      }
    }
    // Black out all cubes and add colors from each ripple
    clearColors();
    for (BaseCube cube : model.baseCubes) {
      for (Ripple ripple : ripples) {
        // Distance from ripple's origin to this cube
        double distance = ripple.origin.dist(new LXVector(cube.x, cube.y, cube.z));
        // Distance from ripple's current radius to this cube
        distance = Math.abs(distance - ripple.radius);
        // Use lightest() to add any existing ripple color to the color of this ripple, if any.
        colors[cube.index] = LXColor.lightest(colors[cube.index], lx.hsb(ripple.hue, ripple.saturation, (float)Math.max(0, 100 - distance / sizeParam.getValuef())));
      }
    }

    // If we aren't at maxRipples, create a new ripple if a random number is inside bounds defined by
    // time passed since last render loop and our amountParam.
    if (ripples.size() < maxRipples && Math.random() * deltaMs < amountParam.getValuef()) {
      // Random hue, saturation, x/y/z
      ripples.add(new Ripple((float)Math.random()*360, (float)Math.random()*100, new LXVector(
        (float)(model.xMin + model.xRange*Math.random()),
        (float)(model.yMin + model.yRange*Math.random()),
        (float)(model.zMin + model.zRange*Math.random()))));
    }
  }
}


/**
Planes rotating through the space
*/
class Planes extends TSPattern {
  // Random seed for our noise functions so it's different on every run
  double seed;
  // These offsets increase relative to deltaMs and speed parameters
  double positionOffset = 0;
  double rotationOffset = 0;
  double colorOffset = 0;
  
  // Number of planes
  final DiscreteParameter countParam = new DiscreteParameter("count", 3, 1, 10);
  // Rate of change of position, rotation, and color
  final BasicParameter positionSpeedParam = new BasicParameter("posSpd", 0.2, 0.01, 1);
  final BasicParameter rotationSpeedParam = new BasicParameter("rotSpd", 0.1, 0, 1);
  final BasicParameter colorSpeedParam = new BasicParameter("clrSpd", 0.2, 0.01, 1);
  // Width of each rendered plane
  final BasicParameter sizeParam = new BasicParameter("size", .5, .1, 5);
  // How different each plane is from the others in position, rotation, and color
  // (0 means all planes have the same position/rotation/color)
  final BasicParameter positionVarianceParam = new BasicParameter("posVar", 0.5, 0, 0.5);
  final BasicParameter rotationVarianceParam = new BasicParameter("rotVar", 0.5, 0, 0.5);
  final BasicParameter colorVarianceParam = new BasicParameter("clrVar", 0.3, 0, 0.3);
  
  public Planes(LX lx) {
    super(lx);
    addParameter(countParam);
    addParameter(positionSpeedParam);
    addParameter(rotationSpeedParam);
    addParameter(colorSpeedParam);
    addParameter(sizeParam);
    addParameter(positionVarianceParam);
    addParameter(rotationVarianceParam);
    addParameter(colorVarianceParam);
    
    seed = Math.random() * 1000;
  }
  
  public void run(double deltaMs) {
    // Increase each offset based on time since last run() and speed param values
    positionOffset += deltaMs * positionSpeedParam.getValuef() / 1000;
    rotationOffset += deltaMs * rotationSpeedParam.getValuef() / 2000;
    colorOffset += deltaMs * colorSpeedParam.getValuef() / 1000;
    float positionVariance = positionVarianceParam.getValuef();
    float rotationVariance = rotationVarianceParam.getValuef();
    float colorVariance = colorVarianceParam.getValuef();
    
    // Black out all cubes and add colors from each plane
    clearColors();
    int countValue = (int)countParam.getValue();
    for (int i = 0; i < countValue; i++) {
      // For each plane we want to display, compute position, rotation, and color from SimplexNoise function
      float x = (float)(model.cx + SimplexNoise.noise(i * positionVariance, positionOffset, seed, 0) * model.xRange / 2.0);
      float y = (float)(model.cy + SimplexNoise.noise(i * positionVariance, positionOffset, seed, 100) * model.yRange / 2.0);
      float z = (float)(model.cz + SimplexNoise.noise(i * positionVariance, positionOffset, seed, 200) * model.zRange / 2.0);
      float yrot = (float)(SimplexNoise.noise(i * rotationVariance, rotationOffset, seed, 300) * Math.PI);
      float zrot = (float)(SimplexNoise.noise(i * rotationVariance, rotationOffset, seed, 400) * Math.PI);
      Plane plane = new Plane(new Vec3D(x, y, z), new Vec3D(1, 0, 0).rotateY(yrot).rotateZ(zrot));
      // Noise hovers around 0 between -1 and 1; double the hue range so we actually get red sometimes.
      int hue = (int)((SimplexNoise.noise(i * colorVariance, colorOffset, seed, 500) + 1) * 360) % 360;
      // Here we want full saturation most of the time, so turn 0 into full and -1 or 1 into none.
      // But take the square root to curve a little back towards less saturation.
      int saturation = (int)((1.0 - Math.sqrt(Math.abs(SimplexNoise.noise(i * colorVariance, colorOffset, seed, 600)))) * 100);
      for (BaseCube cube : model.baseCubes) {
        double distance = plane.getDistanceToPoint(new Vec3D(cube.x, cube.y, cube.z));
        colors[cube.index] = LXColor.lightest(colors[cube.index], lx.hsb(hue, saturation, (float)Math.max(0, 100 - distance / sizeParam.getValuef())));  
      }
    }
  }
}
