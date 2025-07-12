package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import toxi.geom.Vec2D;

abstract class MultiObject extends Layer {

    boolean firstRun = true;
    float runningTimer = 0;
    float runningTimerEnd = 1000;
    boolean running = true;
    float progress;
    int hue = LXColor.BLACK;
    float thickness;
    boolean shouldFade = true;

    Vec2D lastPoint;
    Vec2D currentPoint;
    float fadeIn = 1;
    float fadeOut = 1;

    MultiObject(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        if (running) {
            if (firstRun) {
                advance(0);
                firstRun = false;
            } else {
                advance(deltaMs);
            }
            if (running) {
                for (BaseCube cube : model.baseCubes) {
                    blendColor(cube.index, getColorForCube(cube), LXColor.Blend.LIGHTEST);
                }
            }
        }
    }

    protected void advance(double deltaMs) {
        if (running) {
            runningTimer += deltaMs;
            if (runningTimer >= runningTimerEnd) {
                running = false;
            } else {
                progress = runningTimer / runningTimerEnd;
                if (shouldFade) {
                    fadeIn = Utilities.min(1, 3 * (1 - progress));
                    fadeOut = Utilities.min(1, 3 * progress);
                }
                if (currentPoint == null) {}
                lastPoint = currentPoint;
                onProgressChanged(progress);
            }
        }
    }

    public int getColorForCube(BaseCube cube) {
        return LX.hsb(hue, 100, getBrightnessForCube(cube));
    }

    public float getBrightnessForCube(BaseCube cube) {
        Vec2D cubePointPrime =
                VecUtils.movePointToSamePlane(currentPoint, cube.transformedCylinderPoint);
        float dist = Float.MAX_VALUE;

        Vec2D localLastPoint = lastPoint;
        if (localLastPoint != null) {
            while (localLastPoint.distanceToSquared(currentPoint) > 100) {
                Vec2D point = currentPoint.sub(localLastPoint);
                point.limit(10).addSelf(localLastPoint);

                if (isInsideBoundingBox(cube, cubePointPrime, point)) {
                    dist =
                            Utilities.min(
                                    dist, getDistanceFromGeometry(cube, cubePointPrime, point));
                }
                localLastPoint = point;
            }
        }

        if (isInsideBoundingBox(cube, cubePointPrime, currentPoint)) {
            dist = Utilities.min(dist, getDistanceFromGeometry(cube, cubePointPrime, currentPoint));
        }
        return 100 * Utilities.min(Utilities.max(1 - dist / thickness, 0), 1) * fadeIn * fadeOut;
    }

    public boolean isInsideBoundingBox(BaseCube cube, Vec2D cubePointPrime, Vec2D currentPoint) {
        return VecUtils.insideOfBoundingBox(currentPoint, cubePointPrime, thickness, thickness);
    }

    public float getDistanceFromGeometry(BaseCube cube, Vec2D cubePointPrime, Vec2D currentPoint) {
        return cubePointPrime.distanceTo(currentPoint);
    }

    void init() {}

    public void onProgressChanged(float progress) {}
}
