import java.util.*;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
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
  private static LXVector[] corners;
  static LXVector modelCenter;

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

/**
 Interconnected growth
 */
class Growth extends TSPattern {

  final BasicParameter growthSpeedParam = new BasicParameter("spd", 2, .1, 10);
  final BasicParameter lifeSpeedParam = new BasicParameter("life", 5, 0, 10);
  final BasicParameter fertilityParam = new BasicParameter("fert", 1.15, 1, 3);
  final BasicParameter angleParam = new BasicParameter("angl", 60, 30, 180);

  private class Root {
    // Width of fade at start/end of growing/dying root
    final static double fadeDist = 30;
    
    Grower src;
    Grower dest;
    double angle;
    double srcHue;
    double srcSat;
    double hueDelta;
    double satDelta;
    // <0: pre-life; 0=>1: growing; 1=>(lifeSpeedParam): changing; (lifeSpeedParam)=>(lifeSpeedParam+1): dying
    double age = 0;
    boolean dead = false;
    float srcMinR = Float.MAX_VALUE;
    float srcMaxR = 0;
    float destMinR = Float.MAX_VALUE;
    float destMaxR = 0;
    public Root(Grower src, Grower dest, Root parent){
      this.src = src;
      this.dest = dest;
      src.roots.add(this);
      // Add a random amount of delay before we grow.
      //age = -1 * Math.random() * (1 + lifeSpeedParam.getValue());
      if (parent != null) {
        srcHue = parent.srcHue + parent.hueDelta;
        srcSat = parent.srcSat + parent.satDelta;
      } else {
        srcHue = Math.random() * 360;
        srcSat = Math.sqrt(Math.random()) * 100;
      }
      // Keep hue increasing so we don't always float around some hue
      hueDelta = Math.random() * 60;
      // Vary saturation by 25 (out of 100) on each root
      satDelta = Math.max(0, Math.min(100, srcSat + (Math.random() - 0.5) * 50)) - srcSat;
      
      for (BaseCube c : src.cubes()) {
        if (shouldRenderCube(src, c)) {
          if (c.r < srcMinR) {
            srcMinR = c.r;
          }
          if (c.r > srcMaxR) {
            srcMaxR = c.r;
          }
        }
      }
      if (dest != null) {
        for (BaseCube c : dest.cubes()) {
          if (shouldRenderCube(dest, c)) {
            if (c.r < destMinR) {
              destMinR = c.r;
            }
            if (c.r > destMaxR) {
              destMaxR = c.r;
            }
          }
        }
        dest.roots.add(this);
        angle = (Math.atan2(dest.z - src.z, dest.x - src.x) + 2*Math.PI) % (2*Math.PI);
      } else {
        destMinR = 0;
        destMaxR = 0;
        angle = Math.random() * 2 * Math.PI;
      }
    }
    
    boolean shouldRenderCube(Grower g, BaseCube c) {
      return angleDiff(c.theta * Math.PI / 180, (angle + (g == src ? 0 : Math.PI)) % (2*Math.PI)) < angleParam.getValue() / 2 * Math.PI / 180;
    }
    
    void runGrower(Grower g) {
      if (age < 0) {
        return;
      }
      double deathStartAge = lifeSpeedParam.getValue() + 1;
      // Total "length" of this root (we ignore the space between src and dest)
      double range = (srcMaxR - srcMinR) + (destMaxR - destMinR);
      for (BaseCube c : g.cubes()) {
        // If this cube is within the angle cutoff for this root,
        if (shouldRenderCube(g, c)) {
          // Cube's position within length of the root [0, 1]
          double cubePos = (g == src ? c.r - srcMinR : range - (c.r - destMinR)) / range;
          float hue = (float)(srcHue + hueDelta * cubePos) % 360;
          float sat = (float)(srcSat + satDelta * cubePos);
          double bright = 75 + SimplexNoise.noise(angle, age - cubePos) * 25;
          if (age < 1 || age >= deathStartAge) {
            // The position of the "wipe" which matches cube position [0, 1] but may be <0 if we're just
            // starting to grow the root or >1 if we're almost done killing it.
            double wipePos;
            if (age < 1) {
              // map age [0, 1] to [-fadeDist/range, 1]
              wipePos = age * (fadeDist/range + 1) - fadeDist/range;
            } else {
              // map (age-deathStartAge) [0, 1] to [0, 1+fadeDist/range]
              wipePos = (age-deathStartAge) * (fadeDist/range + 1);
            }
            // This cube's distance from the wipe
            double distance;
            // Growing: Everything behind the wipe is full on.
            if ((age < 1 && cubePos <= wipePos) ||
                // Dying: everything in front of the wipe is full on.
                (age >= deathStartAge && cubePos >= wipePos)) {
              distance = 0;
            } else {
              distance = Math.abs(cubePos - wipePos);
            }
            // I worked out this math...I think it's right...
            double brightChange = 100 * distance / fadeDist * range;
            bright = Math.max(0, bright - brightChange);
          }
          colors[c.index] = LXColor.screen(colors[c.index], lx.hsb(hue, sat, (float)bright));
        }
      }
    }
    
    public void run(double deltaMs) {
      runGrower(src);
      if (dest != null) {
        runGrower(dest);
      }
      
      double ageDelta = deltaMs * growthSpeedParam.getValue() / 1000;
      
      age += ageDelta;
      if (age >= 1 && age - ageDelta < 1 && dest != null) {
        // This root just entered its living phase! Tell dest to grow more.
        dest.growRoots(this);
      }
      dead = age > lifeSpeedParam.getValue() + 2;
      if (dead) {
        src.roots.remove(this);
        if (dest != null) {
          dest.roots.remove(this);
        }
      }
    }
  }
  
  private class Grower {
    LXModel sculpture;
    float x;
    float z;
    boolean isTree;
    ArrayList<Root> roots = new ArrayList<Root>();
    ArrayList<Grower> neighbors;

    public Grower(LXModel sculpture) {
      this.sculpture = sculpture;
      isTree = sculpture instanceof Tree;
      if (sculpture instanceof Tree) {
        x = ((Tree)sculpture).x;
        z = ((Tree)sculpture).z;
      } else if (sculpture instanceof Shrub) {
        x = ((Shrub)sculpture).x;
        z = ((Shrub)sculpture).z;
      }
    }
    
    List<Cube> cubes() {
      if (sculpture instanceof Tree) {
        return ((Tree)sculpture).cubes;
      } else if (sculpture instanceof Shrub) {
        return (List<Cube>)(List<?>)((Shrub)sculpture).cubes;
      }
      return null;
    }
    
    void growRoots(Root parent) {
      if (allRoots.size() > growers.size() * 10) {
        return;
      }
      double fertility = fertilityParam.getValue();
      int numNew = (int)Math.floor(fertility);
      if (Math.random() < fertility - numNew) {
        numNew++;
      }
      numNew = 1;
      for (int i = 0; numNew > 0 && i < neighbors.size(); i++) {
        // Sometimes skip a nearest neighbor, so we don't always grow to the same place
        /*if (Math.random() > .5) {
          continue;
        }
        if (Math.random() < 0.1) {
          // Sometimes grow a root in a random direction to nowhere
          allRoots.add(new Root(this, null, parent));
          numNew--;
          continue;
        }*/
        Grower g = neighbors.get(i);
        boolean alreadyLinked = false;
        // Trying this simpler metric: only draw roots to neighbors with no roots.
        alreadyLinked = g.roots.size() > 0;
        /*
        // Search for a root that already links this grower and its neighbor.
        for (Root r : g.roots) {
          if (r.src == this || r.dest == this) {
            // These growers are already linked; don't grow another root between them.
            alreadyLinked = true;
            break;
          }
        }*/
        if (!alreadyLinked) {
          allRoots.add(new Root(this, g, parent));
          numNew--;
          // Nothing below this, but put this here for completeness
          continue;
        }
      }
    }
  }
  
  public double distance(Grower a, Grower b) {
    return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2));
  }
 
  class SortByDistance implements Comparator<Grower> {
    Grower target;
    public SortByDistance(Grower target) {
      this.target = target;
    }
    public int compare(Grower a, Grower b) {
      return (int)(distance(target, a) - distance(target, b));
    }
  }

  ArrayList<Grower> growers = new ArrayList<Grower>();
  ArrayList<Root> allRoots = new ArrayList<Root>();

  public Growth(LX lx) {
    super(lx);
    addParameter(growthSpeedParam);
    addParameter(lifeSpeedParam);
    addParameter(fertilityParam);
    addParameter(angleParam);

    for (Tree tree : model.trees) {
      growers.add(new Grower((LXModel)tree));
    }
    for (Shrub shrub : model.shrubs) {
      growers.add(new Grower((LXModel)shrub));
    }
    for (Grower g : growers) {
      g.neighbors = new ArrayList<Grower>(growers);
      Collections.sort(g.neighbors, new SortByDistance(g));
      g.neighbors.remove(0);
    }
  }

  double angleDiff(double a1, double a2) {
    double diff = Math.abs(a1 - a2);
    if (diff > Math.PI) {
      diff = 2*Math.PI - diff;
    }
    return diff;
  }

  public void run(double deltaMs) {
    clearColors();
    
    for (int i = 0; i < allRoots.size(); i++) {
      allRoots.get(i).run(deltaMs);
    }
    
    ListIterator<Root> iter = allRoots.listIterator();
    while (iter.hasNext()) {
      Root r = iter.next();
      if (r.dead) {
        iter.remove();
      }
    }
    
    if (allRoots.size() == 0) {
      for (int i = 0; i < Math.ceil(fertilityParam.getValue()); i++) {
        growers.get((int)Math.floor(Math.random() * growers.size())).growRoots(null);
      }
    }
  }
}
