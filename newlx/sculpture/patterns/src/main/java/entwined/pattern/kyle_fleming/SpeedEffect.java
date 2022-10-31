package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class SpeedEffect extends LXEffect {

  final BoundedParameter speed = new BoundedParameter("SPEED", 1, .1, 10).setExponent(2);
  public SpeedEffect(final LX lx) {
    super(lx);
    addParameter("speed", speed);

    speed.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        lx.engine.setSpeed(speed.getValue());
      }
    });
  }

  @Override
  protected void onEnable() {
    super.onEnable();
    lx.engine.setSpeed(speed.getValue());
  }

  @Override
  public void run(double deltaMs, double strength) {}
}
