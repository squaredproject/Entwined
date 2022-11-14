package entwined.pattern.interactive;

import entwined.pattern.kyle_fleming.CandyCloudTextureEffect;
import heronarts.lx.LX;
import heronarts.lx.LXDeviceComponent;

public class InteractiveRainbowEffect extends InteractiveEffect {
  public InteractiveRainbowEffect(LX lx) {
    super(lx);
    this.childClass = CandyCloudTextureEffect.class;
  }

  @Override
  void onChildEffectCreated(LXDeviceComponent childEffect) {
    CandyCloudTextureEffect child = (CandyCloudTextureEffect)childEffect;
    child.amount.setValue(1);
  }
}