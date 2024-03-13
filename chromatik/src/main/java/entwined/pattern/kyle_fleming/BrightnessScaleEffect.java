package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;

public class BrightnessScaleEffect extends LXEffect {

  final CompoundParameter amount = new CompoundParameter("Brightness", 1);

  public BrightnessScaleEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);
    enable();
  }

  public float getAmount() {
    return( amount.getValuef() );
  }

  public void setAmount(double val) {
    amount.setValue(val);
  }

  public CompoundParameter getParameter() {
    return(amount);
  }

  @Override
  public void run(double deltaMs, double strength) {
    float amountVal = amount.getValuef();
    if (amountVal < 1) {
      final int mult = LXColor.grayn(amountVal);
      for (int i = 0; i < colors.length; ++i) {
        colors[i] = LXColor.multiply(colors[i], mult);
      }
    }
  }
}


