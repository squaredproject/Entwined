package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;

public class ColorStrobeTextureEffect extends LXEffect {

  public final CompoundParameter amount = new CompoundParameter("SEIZ", 0, 0, 1).setExponent(2);

  public ColorStrobeTextureEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);
  }

  @Override
  public void run(double deltaMs, double strength) {
    if (amount.getValue() > 0) {
      float newHue = EntwinedUtils.random(360);
      int newColor = LX.hsb(newHue, 100, 100);
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = LX.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }

  public float getAmount() {
    return amount.getValuef();
  }
}

