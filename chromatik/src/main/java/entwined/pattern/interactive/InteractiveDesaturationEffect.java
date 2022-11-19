package entwined.pattern.interactive;

import entwined.pattern.anon.ColorEffect;
import heronarts.lx.LX;
import heronarts.lx.LXDeviceComponent;


public class InteractiveDesaturationEffect extends InteractiveEffect {
  public InteractiveDesaturationEffect(LX lx) {
    super(lx, ColorEffect.class);
  }

  @Override
  void onChildEffectCreated(LXDeviceComponent childEffect) {
    ColorEffect child = (ColorEffect)childEffect;
    child.desaturation.setValue(.66);
  }
}