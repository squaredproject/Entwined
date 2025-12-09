package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class SpeedEffect extends LXEffect {

  public final CompoundParameter speed = new CompoundParameter("SPEED", 1, .1, 10).setExponent(2);

  public SpeedEffect(final LX lx) {
    super(lx);
    addParameter("speed", speed);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    super.onParameterChanged(p);
    if (p == this.speed) {
      lx.engine.setSpeed(this.speed.getValue());
    }
  }

  @Override
  protected void onEnable() {
    super.onEnable();
    lx.engine.setSpeed(speed.getValue());
  }

  @Override
  public void run(double deltaMs, double strength) {}
}
