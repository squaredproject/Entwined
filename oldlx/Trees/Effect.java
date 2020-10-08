import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;

abstract class Effect extends LXEffect {

  protected final Model model;

  Effect(LX lx) {
    super(lx);
    model = (Model)lx.model;
  }

  @Override
  public void loop(double deltaMs) {
    if (isEnabled()) {
      super.loop(deltaMs);
    }
  }
}

class TSEffectController {

  String name;
  LXEffect effect;
  Triggerable triggerable;

  TSEffectController(String name, LXEffect effect, Triggerable triggerable) {
    this.name = name;
    this.effect = effect;
    this.triggerable = triggerable;
  }

  String getName() {
    return name;
  }

  boolean getEnabled() {
    return triggerable.isTriggered();
  }

  void setEnabled(boolean enabled) {
    if (enabled) {
      triggerable.onTriggered(1);
    } else {
      triggerable.onRelease();
    }
  }
}

