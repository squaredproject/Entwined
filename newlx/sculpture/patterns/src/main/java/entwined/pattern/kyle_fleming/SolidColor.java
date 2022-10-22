package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class SolidColor extends LXPattern {
  // 235 = blue, 135 = green, 0 = red
  final BoundedParameter hue = new BoundedParameter("HUE", 135, 0, 360);
  final BoundedParameter brightness = new BoundedParameter("BRT", 100, 0, 100);

  public SolidColor(LX lx) {
    super(lx);
    addParameter("hue", hue);
    addParameter("brightness", brightness);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    setColors(LX.hsb(hue.getValuef(), 100, (float)brightness.getValue()));
  }
}
