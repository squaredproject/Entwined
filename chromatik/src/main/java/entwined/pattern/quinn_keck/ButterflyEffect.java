package entwined.pattern.quinn_keck;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;


public class ButterflyEffect extends LXPattern {

   float minValue = 0;
   float maxValue = 100;
   final SinLFO brightSin = new SinLFO(minValue, maxValue, 5000);

  float startValue = 65;
  final CompoundParameter satParam = new CompoundParameter("Saturation", startValue, minValue, maxValue);

  float periodMs = 10;
  final SawLFO hueSaw = new SawLFO(minValue, maxValue, periodMs);

 public ButterflyEffect(LX lx) {
    super(lx);
  }

float time = 10000;
  @Override
  public void run(double deltaMs) {

      for (LXPoint cube : model.points) {
        time += deltaMs/50;
        colors[cube.index] = LX.hsb(cube.theta, satParam.getValuef(), (-(time)));
        if (time > 8800000.0) {
          time = 1;
        }

      }
  }
}
