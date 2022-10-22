package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

public class Palette extends LXPattern {

  public Palette(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint cube : model.points) {
      colors[cube.index] = LX.hsb(
        cube.index % 360,
        100,
        100
      );
    }
  }
}

