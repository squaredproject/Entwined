package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Fade extends LXPattern {

  final CompoundParameter speed = new CompoundParameter("SPEE", 11000, 100000, 1000).setExponent(0.5);
  final CompoundParameter smoothness = new CompoundParameter("SMOO", 100, 1, 100).setExponent(2);

  final SinLFO colr = new SinLFO(0, 360, speed);

  public Fade(LX lx) {
    super(lx);
    addParameter("speed", speed);
    addParameter("smoothness", smoothness);
    addModulator(colr).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint cube : model.points) {
      colors[cube.index] = LX.hsb(
        (int)((int)colr.getValuef() * smoothness.getValuef() / 100) * 100 / smoothness.getValuef(),
        100,
        100
      );
    }
  }
}
