package entwined.pattern.interactive;

import entwined.pattern.irene_zhou.Fire;
import heronarts.lx.LX;
import heronarts.lx.LXDeviceComponent;

public class InteractiveFireEffect extends InteractiveEffect {
  public InteractiveFireEffect(LX lx) {
    super(lx, Fire.class);
  }

  @Override
  void onChildEffectCreated(LXDeviceComponent childEffect) {
    Fire child = (Fire)childEffect;
    child.onTriggered();
  }
}

