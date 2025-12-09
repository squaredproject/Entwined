package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

public class FadeTextureEffect extends LXEffect {

  public final CompoundParameter amount = new CompoundParameter("FADE");

  final SawLFO colr = new SawLFO(0, 360, 10000);

  public FadeTextureEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);

    addModulator(colr).start();
  }

  @Override
  public void run(double deltaMs, double strength) {
    if (amount.getValue() > 0) {
      float newHue = colr.getValuef();
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
