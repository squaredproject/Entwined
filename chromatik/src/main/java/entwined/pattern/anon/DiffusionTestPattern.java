package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.TEST)
public class DiffusionTestPattern extends LXPattern {

  final CompoundParameter hue = new CompoundParameter("HUE", 0, 360);
  final CompoundParameter sat = new CompoundParameter("SAT", 1);
  final CompoundParameter brt = new CompoundParameter("BRT", 1);
  final CompoundParameter spread = new CompoundParameter("SPREAD", 0, 360);

  public DiffusionTestPattern(LX lx) {
    super(lx);
    addParameter("hue", hue);
    addParameter("saturation", sat);
    addParameter("bright", brt);
    addParameter("spread", spread);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    setColors(LXColor.BLACK);
    for (int i = 0; i < 12; ++i) {
      colors[i] = LX.hsb(
        (hue.getValuef() + (i / 4) * spread.getValuef()) % 360,
        sat.getValuef() * 100,
        Math.min(100, brt.getValuef() * (i+1) / 12.f * 200)
      );
    }
  }
}
