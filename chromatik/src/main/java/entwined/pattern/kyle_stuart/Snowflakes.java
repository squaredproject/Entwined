package entwined.pattern.kyle_stuart;

import heronarts.lx.LX;
import heronarts.lx.utils.LXUtils;
import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import entwined.core.MultiObject;
import entwined.core.MultiObjectPattern;

public class Snowflakes extends MultiObjectPattern<SnowFlake> {

  public Snowflakes(LX lx) {
    super(lx);
  }

  /*
  BoundedParameter getFrequencyParameter() {
    return new BoundedParameter("FREQ", 40, .1, 400).setExponent(2);
  }
  */

  @Override
  public SnowFlake generateObject(float strength) {
    SnowFlake snowFlake = new SnowFlake(lx);
    snowFlake.runningTimer = 0;
    snowFlake.runningTimerEnd = 90 + EntwinedUtils.random(50);
    snowFlake.decayTime = snowFlake.runningTimerEnd;
    float pathDirection = 270;
    snowFlake.pathDist = model.yMax - model.yMin + 40;
    snowFlake.startTheta = EntwinedUtils.random(160);
    snowFlake.startY = model.yMax + 20;
    snowFlake.startPoint = new Vec2D(snowFlake.startTheta, snowFlake.startY);
    snowFlake.endTheta = snowFlake.startTheta;
    snowFlake.endY = model.yMin - 20;
    snowFlake.displayColor = 200 + (int)EntwinedUtils.random(20);
    snowFlake.thickness = .5f + EntwinedUtils.random(.6f);

    return snowFlake;
  }
}

class SnowFlake extends MultiObject {

  float runningTimer;
  float runningTimerEnd;
  float decayTime;

  Vec2D startPoint;
  float startTheta;
  float startY;
  float endTheta;
  float endY;
  float pathDist;

  int displayColor;
  float thickness;

  float percentDone;
  Vec2D currentPoint;
  float currentTheta;
  float currentY;

  SnowFlake(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    if (running) {
      runningTimer += deltaMs;
      if (runningTimer >= runningTimerEnd + decayTime) {
        running = false;
      } else {
        percentDone = EntwinedUtils.min(runningTimer, runningTimerEnd) / runningTimerEnd;
        currentTheta = (float)LXUtils.lerp(startTheta, endTheta, percentDone);
        currentY = (float)LXUtils.lerp(startY, endY, percentDone);
        currentPoint = new Vec2D(currentTheta, currentY);
      }
    }
  }
}
