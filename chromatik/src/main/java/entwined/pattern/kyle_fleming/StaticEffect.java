package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;

public class StaticEffect extends LXEffect {

  final CompoundParameter amount = new CompoundParameter("STTC", 0, 0, 1);

  private boolean isCreatingStatic = false;

  public StaticEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);
  }

  public int getAmount() {
    return (int)(amount.getValue());
  }

  public void setAmount(double val) {
    amount.setValue(val);
  }

  @Override
  protected void run(double deltaMs, double strength) {
    if (amount.getValue() > 0) {
      if (isCreatingStatic) {
        double chance = EntwinedUtils.random(1);
        if (chance > amount.getValue()) {
          isCreatingStatic = false;
        }
      } else {
        double chance = EntwinedUtils.random(1);
        if (chance < amount.getValue()) {
          isCreatingStatic = true;
        }
      }
      if (isCreatingStatic) {
        for (int i = 0; i < colors.length; i++) {
          float h = EntwinedUtils.random(360);
          colors[i] = LX.hsb( h, LXColor.s(colors[i]), LXColor.b(colors[i]));
        }
      }
    }
  }
}
