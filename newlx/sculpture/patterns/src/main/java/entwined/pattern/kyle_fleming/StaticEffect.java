package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.BoundedParameter;

public class StaticEffect extends LXEffect {

  final BoundedParameter amount = new BoundedParameter("STTC");

  private boolean isCreatingStatic = false;

  public StaticEffect(LX lx) {
    super(lx);
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
          colors[i] = (int)EntwinedUtils.random(255);
        }
      }
    }
  }
}
