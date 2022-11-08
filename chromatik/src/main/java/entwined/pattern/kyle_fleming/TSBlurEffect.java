package entwined.pattern.kyle_fleming;

import heronarts.lx.effect.BlurEffect;
import heronarts.lx.LX;


public class TSBlurEffect extends BlurEffect {
  public TSBlurEffect(LX lx) {
    super(lx);
  }

  /*
  public void setAmount(double val) {
    amount.setValue(val);
  }
  */

  @Override
  public void loop(double deltaMs) {
    if (isEnabled()) {
      super.loop(deltaMs);
    }
  }
}

