package entwined.utils;

import heronarts.lx.utils.LXUtils;

public class VecUtils {
  public static boolean insideOfBoundingBox(Vec2D origin, Vec2D point, float xTolerance, float yTolerance) {
    return EntwinedUtils.abs(origin.x - point.x) <= xTolerance && EntwinedUtils.abs(origin.y - point.y) <= yTolerance;
  }

  public static float wrapDist2d(Vec2D a, Vec2D b) {
    return EntwinedUtils.sqrt(EntwinedUtils.pow((LXUtils.wrapdistf(a.x, b.x, 360)), 2) + EntwinedUtils.pow(a.y - b.y, 2));
  }

  public static Vec2D movePointToSamePlane(Vec2D reference, Vec2D point) {
    return new Vec2D(VecUtils.moveThetaToSamePlane(reference.x, point.x), point.y);
  }

  // Assumes thetaA as a reference point
  // Moves thetaB to within 180 degrees, letting thetaB go beyond [0, 360)
  public static float moveThetaToSamePlane(float thetaA, float thetaB) {
    if (thetaA - thetaB > 180) {
      return thetaB + 360;
    } else if (thetaB - thetaA > 180) {
      return thetaB - 360;
    } else {
      return thetaB;
    }
  }

  public static float thetaDistance(float thetaA, float thetaB) {
    return LXUtils.wrapdistf(thetaA, thetaB, 360);
  }

}
