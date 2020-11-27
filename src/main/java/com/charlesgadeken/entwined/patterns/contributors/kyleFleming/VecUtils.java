package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.utils.LXUtils;
import toxi.geom.Vec2D;

class VecUtils {
    static boolean insideOfBoundingBox(
            Vec2D origin, Vec2D point, float xTolerance, float yTolerance) {
        return Utilities.abs(origin.x - point.x) <= xTolerance
                && Utilities.abs(origin.y - point.y) <= yTolerance;
    }

    static float wrapDist2d(Vec2D a, Vec2D b) {
        return Utilities.sqrt(
                Utilities.pow((LXUtils.wrapdistf(a.x, b.x, 360)), 2) + Utilities.pow(a.y - b.y, 2));
    }

    static Vec2D movePointToSamePlane(Vec2D reference, Vec2D point) {
        return new Vec2D(VecUtils.moveThetaToSamePlane(reference.x, point.x), point.y);
    }

    // Assumes thetaA as a reference point
    // Moves thetaB to within 180 degrees, letting thetaB go beyond [0, 360)
    static float moveThetaToSamePlane(float thetaA, float thetaB) {
        if (thetaA - thetaB > 180) {
            return thetaB + 360;
        } else if (thetaB - thetaA > 180) {
            return thetaB - 360;
        } else {
            return thetaB;
        }
    }

    static float thetaDistance(float thetaA, float thetaB) {
        return LXUtils.wrapdistf(thetaA, thetaB, 360);
    }
}
