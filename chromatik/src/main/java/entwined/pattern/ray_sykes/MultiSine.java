package entwined.pattern.ray_sykes;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class MultiSine extends LXPattern {
  final int numLayers = 3;
  int[][] distLayerDivisors = {{50, 140, 200}, {360, 60, 45}};
  final CompoundParameter brightEffect = new CompoundParameter("Bright", 100, 0, 100);

  final CompoundParameter[] timingSettings =  {
    new CompoundParameter("T1", 6300, 5000, 30000),
    new CompoundParameter("T2", 4300, 2000, 10000),
    new CompoundParameter("T3", 11000, 10000, 20000)
  };
  SinLFO[] frequencies = {
    new SinLFO(0, 1, timingSettings[0]),
    new SinLFO(0, 1, timingSettings[1]),
    new SinLFO(0, 1, timingSettings[2])
  };

  public MultiSine(LX lx) {
    super(lx);
    for (int i = 0; i < numLayers; i++){
      addParameter("timing_" + i, timingSettings[i]);
      addModulator(frequencies[i]).start();
    }
    addParameter("bright", brightEffect);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float brightValue = brightEffect.getValuef();
    float currentBaseHue = lx.engine.palette.color.getHuef();

    for (LXModel component : model.children) {
      for (LXPoint cube : component.points) {
        float localTheta = (float)Math.atan2(cube.z - component.cz, cube.x - component.cx) * 180/LX.PIf; // XXX - could avoid the extra math below, but for clarity...
        float[] combinedDistanceSines = {0, 0};
        for (int i = 0; i < numLayers; i++){
          combinedDistanceSines[0] += LXUtils.sinf(LX.TWO_PI * frequencies[i].getValuef() + cube.y / distLayerDivisors[0][i]) / numLayers;
          combinedDistanceSines[1] += LXUtils.sinf(LX.TWO_PI * frequencies[i].getValuef() + LX.TWO_PI*(localTheta / distLayerDivisors[1][i])) / numLayers;
        }
        float hueVal = (currentBaseHue + 20 * LXUtils.sinf(LX.TWO_PI * (combinedDistanceSines[0] + combinedDistanceSines[1]))) % 360;
        float brightVal = (100 - brightValue) + brightValue * (2 + combinedDistanceSines[0] + combinedDistanceSines[1]) / 4;
        float satVal = 90 + 10 * LXUtils.sinf(LX.TWO_PI * (combinedDistanceSines[0] + combinedDistanceSines[1]));
        colors[cube.index] = LX.hsb(hueVal,  satVal, brightVal);
      }
    }
  }
}
