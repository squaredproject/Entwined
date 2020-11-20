package com.charlesgadeken.entwined.patterns.contributors.grantPatterson;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.ListIterator;

@LXCategory("Grant Patterson")
public class Pond extends EntwinedBasePattern {
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

  final BoundedParameter speedParam = new BoundedParameter("speed", 14, 4, 40);
  final BoundedParameter sizeParam = new BoundedParameter("size", .5, .1, 5);
  final BoundedParameter amountParam = new BoundedParameter("amount", .3, .1, 1);

  public Pond(LX lx) {
    super(lx);
    addParameter("grantPatterson/pond/speed", speedParam);
    addParameter("grantPatterson/pond/size", sizeParam);
    addParameter("grantPatterson/pond/amount", amountParam);

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

  private void runCube(BaseCube cube) {
    // Clear cube first
    colors[cube.index] = LXColor.BLACK;
    for (Ripple ripple : ripples) {
      // Distance from ripple's origin to this cube
      double distance = ripple.origin.dist(new LXVector(cube.x, cube.y, cube.z));
      // Distance from ripple's current radius to this cube
      distance = Math.abs(distance - ripple.radius);
      // Use lightest() to add any existing ripple color to the color of this ripple, if any.
      colors[cube.index] = LXColor.lightest(colors[cube.index], LX.hsb(ripple.hue, ripple.saturation, (float)Math.max(0, 100 - distance / sizeParam.getValuef())));
    }
  }

  @Override
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
    // Run common code to render each Cube and ShrubCube
    for (BaseCube cube : model.baseCubes) {
      runCube(cube);
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
