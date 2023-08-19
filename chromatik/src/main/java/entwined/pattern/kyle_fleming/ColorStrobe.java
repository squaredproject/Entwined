package entwined.pattern.kyle_fleming;

import entwined.core.TSTriggerablePattern;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;

public class ColorStrobe extends TSTriggerablePattern {

  double timer = 0;

  public ColorStrobe(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    timer += deltaMs;
    if (timer > 16) {
      timer = 0;
      setColors(LX.hsb(EntwinedUtils.random(360), 100, 100));
    }
  }
}
