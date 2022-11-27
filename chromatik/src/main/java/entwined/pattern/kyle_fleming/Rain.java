package entwined.pattern.kyle_fleming;

import entwined.core.MultiObject;
import entwined.core.MultiObjectPattern;
import entwined.utils.EntwinedUtils;
import entwined.utils.Vec2D;
import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.utils.LXUtils;

public class Rain extends MultiObjectPattern<RainDrop> {

  public Rain(LX lx) {
    super(lx);
    fadeTime = 500;
  }

  @Override
  protected CompoundParameter getFrequencyParameter() {
    return new CompoundParameter("FREQ", 40, 1, 75);
  }

  @Override
  protected RainDrop generateObject(float strength) {
    RainDrop rainDrop = new RainDrop(lx);

    rainDrop.setRunningTimerEnd(180 + EntwinedUtils.random(20));
    rainDrop.theta = EntwinedUtils.random(360);
    rainDrop.startY = model.yMax + 20;
    rainDrop.endY = model.yMin - 20;
    rainDrop.setHue(200 + (int)EntwinedUtils.random(20));
    rainDrop.setThickness(10 * (1.5f + EntwinedUtils.random(.6f)));

    return rainDrop;
  }
}

class RainDrop extends MultiObject {

  float theta;
  float startY;
  float endY;

  RainDrop(LX lx) {
    super(lx);
    shouldFade = false;
  }

  @Override
  public void onProgressChanged(float progress) {
    currentPoint = new Vec2D(theta, (float)LXUtils.lerp(startY, endY, progress));
  }
}
