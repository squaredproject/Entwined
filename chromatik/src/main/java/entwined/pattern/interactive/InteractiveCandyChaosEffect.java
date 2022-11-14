package entwined.pattern.interactive;

import entwined.pattern.kyle_fleming.CandyTextureEffect;
import heronarts.lx.LX;
import heronarts.lx.LXDeviceComponent;

public class InteractiveCandyChaosEffect extends InteractiveEffect {
  public InteractiveCandyChaosEffect(LX lx) {
    super(lx);
    this.childClass = CandyTextureEffect.class;
  }

  @Override
  void onChildEffectCreated(LXDeviceComponent childEffect) {
    CandyTextureEffect child = (CandyTextureEffect)childEffect;
    child.setAmount(1.0);
  }
}