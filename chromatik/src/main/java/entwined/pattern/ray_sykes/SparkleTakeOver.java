package entwined.pattern.ray_sykes;

import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class SparkleTakeOver extends LXPattern {
  int[] sparkleTimeOuts;
  int lastComplimentaryToggle = 0;
  int complimentaryToggle = 0;
  boolean resetDone = false;
  final SinLFO timing = new SinLFO(6000, 10000, 20000);
  final SawLFO coverage = new SawLFO(0, 100, timing);
  final CompoundParameter hueVariation = new CompoundParameter("HueVar", 0.1, 0.1, 0.4);
  float hueSeparation = 180;
  float newHueVal;
  float oldHueVal;
  float newBrightVal = 100;
  float oldBrightVal = 100;
  public SparkleTakeOver(LX lx) {
    super(lx);
    sparkleTimeOuts = new int[model.points.length];
    addModulator(timing).start();
    addModulator(coverage).start();
    addParameter("hueVar", hueVariation);
  }
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    if (coverage.getValuef() < 5){
      if (!resetDone){
        lastComplimentaryToggle = complimentaryToggle;
        oldBrightVal = newBrightVal;
        if (Math.random()*5 < 2){
          complimentaryToggle = 1 - complimentaryToggle;
          newBrightVal = 100;
        }
        else {
          newBrightVal = (newBrightVal == 100) ? 70 : 100;
        }
        for (int i = 0; i < sparkleTimeOuts.length; i++){
          sparkleTimeOuts[i] = 0;
        }
        resetDone = true;
      }
    }
    else {
      resetDone = false;
    }

    float hueVariationValue = hueVariation.getValuef();
    float coverageValue = coverage.getValuef();
    float currentBaseHue = lx.engine.palette.color.getHuef();

    for (LXPoint cube : model.points) {
      float localTheta = CubeManager.getCube(lx, cube.index).localTheta * LX.TWO_PIf/360;
      float localY = CubeManager.getCube(lx, cube.index).localY;
      float newHueVal = (currentBaseHue + complimentaryToggle * hueSeparation + hueVariationValue * localY) % 360;
      // (float)Math.atan2(cube.z - component.cz, cube.x - component.cx) * 180/LX.PIf;
      float oldHueVal = (currentBaseHue + lastComplimentaryToggle * hueSeparation + hueVariationValue * localY) % 360;
      if (sparkleTimeOuts[cube.index] > EntwinedUtils.millis()){
        colors[cube.index] = LX.hsb(newHueVal, ((30  + coverageValue) / 1.3f) % 100, newBrightVal);
      }
      else {
        colors[cube.index] = LX.hsb(oldHueVal, ((140 - coverageValue) / 1.4f) % 100, oldBrightVal);
        float chance = EntwinedUtils.random(EntwinedUtils.abs(LXUtils.sinf((LX.TWO_PIf / 360) * localTheta * 4)   * 50)
                                          + EntwinedUtils.abs(LXUtils.sinf( LX.TWO_PIf        * (cube.y / 9000))) * 50);
        if (chance > (100 - 100*(Math.pow(coverageValue/100, 2)))){
          sparkleTimeOuts[cube.index] = EntwinedUtils.millis() + 50000;
        }
        else if (chance > 1.1f * (100 - coverage.getValuef())){
          sparkleTimeOuts[cube.index] = EntwinedUtils.millis() + 100;
        }
      }
    }
  }
}

