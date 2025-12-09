package entwined.pattern.ray_sykes;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class Ripple extends LXPattern {
  final CompoundParameter speed = new CompoundParameter("Speed", 15000, 25000, 8000);
  final CompoundParameter baseBrightness = new CompoundParameter("Bright", 0, 0, 100);
  final SawLFO rippleAge = new SawLFO(0, 100, speed);

  boolean resetDone = false;
  float yCenter;
  float thetaCenter;
  public Ripple(LX lx) {
    super(lx);
    addParameter("speed", speed);
    addParameter("bright", baseBrightness);
    addModulator(rippleAge).start();
  }

  // XXX - random - what was the previous function with only a single value?
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    if (rippleAge.getValuef() < 5){
      if (!resetDone){
        yCenter = 150 + (float)Math.random() * 300;  // WTF is this 150 about?
        thetaCenter = (float)Math.random() * 360;
        resetDone = true;
      }
    }
    else {
      resetDone = false;
    }

    float radius = (float)Math.pow(rippleAge.getValuef(), 2) / 3;
    float rippleAgeValue = rippleAge.getValuef();
    float baseBrightValue = baseBrightness.getValuef();
    float currentBaseHue = lx.engine.palette.color.getHuef();

    float hueVal = 0;
    float brightVal = 0;

    for (LXModel component : model.children) {
      for (LXPoint cube : component.points) {
        float localTheta = (float)Math.atan2(cube.z - component.cz, cube.x - component.cx) * 180/LX.PIf;
        float distVal = (float)Math.sqrt(Math.pow((LXUtils.wrapdistf(thetaCenter, localTheta, 360)) * 0.8f, 2) + Math.pow(yCenter - cube.y, 2));
        float heightHueVariance = 0.1f * cube.y;
        if (distVal < radius){
          float rippleDecayFactor = (100 - rippleAgeValue) / 100;
          float timeDistanceCombination = distVal/20 - rippleAgeValue;
          hueVal = (currentBaseHue + 40 * LXUtils.sinf(LX.TWO_PIf * (12.5f + rippleAgeValue )/ 200) * rippleDecayFactor * LXUtils.sinf(timeDistanceCombination) + heightHueVariance + 360) % 360;
          brightVal = LXUtils.constrainf((baseBrightValue + rippleDecayFactor * (100 - baseBrightValue)) + 80 * rippleDecayFactor * LXUtils.sinf(timeDistanceCombination + LX.TWO_PIf / 8), 0, 100);
        }
        else {
          hueVal = (currentBaseHue + heightHueVariance) % 360;
          brightVal = baseBrightValue;
        }
        colors[cube.index] = LX.hsb(hueVal,  100, brightVal);
      }
    }
  }
}
