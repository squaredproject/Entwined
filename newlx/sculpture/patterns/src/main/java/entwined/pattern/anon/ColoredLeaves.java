package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;

public class ColoredLeaves extends LXPattern {

  private SawLFO[] movement;
  private SinLFO[] bright;

  public ColoredLeaves(LX lx) {
    super(lx);
    movement = new SawLFO[3];
    for (int i = 0; i < movement.length; ++i) {
      movement[i] = new SawLFO(0, 360, 60000 / (1 + i));
      addModulator(movement[i]).start();
    }
    bright = new SinLFO[5];
    for (int i = 0; i < bright.length; ++i) {
      bright[i] = new SinLFO(100, 0, 60000 / (1 + i));
      addModulator(bright[i]).start();
    }
  }

  // XXX Trees appear to be all the same hue. WTF.
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint cube : model.points) {
      colors[cube.index] = LX.hsb(
        (360 + movement[cube.index  % movement.length].getValuef()) % 360,
        100,
        bright[cube.index % bright.length].getValuef()
      );
    }
  }
}