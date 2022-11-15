package entwined.core;

import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import entwined.utils.VecUtils;
import heronarts.lx.LX;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public abstract class MultiObject extends LXLayer {

  boolean firstRun = true;
  float runningTimer = 0;
  protected float runningTimerEnd = 1000;
  protected boolean running = true;
  float progress;
  protected int hue = LXColor.BLACK;
  protected float thickness;
  protected boolean shouldFade = true;

  protected Vec2D lastPoint;
  protected Vec2D currentPoint;
  float fadeIn = 1;
  float fadeOut = 1;

  public MultiObject(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    if (running) {
      if (firstRun) {
        advance(0);
        firstRun = false;
      } else {
        advance(deltaMs);
      }
      if (running) {
        for (LXPoint cube : model.points) {
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
          fadeIn = EntwinedUtils.min(1, 3 * (1 - progress));
          fadeOut = EntwinedUtils.min(1, 3 * progress);
        }
        if (currentPoint == null) {
        }
        lastPoint = currentPoint;
        onProgressChanged(progress);
      }
    }
  }

  public int getHue() {
    return hue;
  }

  public void setHue(int val) {
    hue = val % 360;
  }

  public void setRunningTimerEnd(float val) {
    runningTimerEnd = val;
  }

  public void setThickness(float val) {
    thickness = val;
  }

  public int getColorForCube(LXPoint cube) {
    return LX.hsb(hue, 100, getBrightnessForCube(cube));
  }

  public float getBrightnessForCube(LXPoint cube) {
    Vec2D cubePointPrime = VecUtils.movePointToSamePlane(currentPoint, CubeManager.getCube(lx, cube.index).cylinderPoint);
    float dist = Float.MAX_VALUE;

    Vec2D localLastPoint = lastPoint;
    if (localLastPoint != null) {
      while (localLastPoint.distanceToSquared(currentPoint) > 100) {
        Vec2D point = currentPoint.sub(localLastPoint);
        point.limit(10).addSelf(localLastPoint);

        if (isInsideBoundingBox(cube, cubePointPrime, point)) {
          dist = EntwinedUtils.min(dist, getDistanceFromGeometry(cube, cubePointPrime, point));
        }
        localLastPoint = point;
      }
    }

    if (isInsideBoundingBox(cube, cubePointPrime, currentPoint)) {
      dist = EntwinedUtils.min(dist, getDistanceFromGeometry(cube, cubePointPrime, currentPoint));
    }
    return 100 * EntwinedUtils.min(EntwinedUtils.max(1 - dist / thickness, 0), 1) * fadeIn * fadeOut;
  }

  public boolean isInsideBoundingBox(LXPoint cube, Vec2D cubePointPrime, Vec2D currentPoint) {
    return VecUtils.insideOfBoundingBox(currentPoint, cubePointPrime, thickness, thickness);
  }

  public float getDistanceFromGeometry(LXPoint cube, Vec2D cubePointPrime, Vec2D currentPoint) {
    return cubePointPrime.distanceTo(currentPoint);
  }

  public void init() { }

  public void onProgressChanged(float progress) { }
}
