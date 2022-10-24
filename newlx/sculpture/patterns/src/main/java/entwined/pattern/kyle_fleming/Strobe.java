package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class Strobe extends LXPattern { // XXX TSTriggerablePattern {

  final BoundedParameter speed = new BoundedParameter("SPEE", 200, 3000, 30).setExponent(.5);
  final BoundedParameter balance = new BoundedParameter("BAL", .5, .01, .99);

  int timer = 0;
  boolean on = false;

  Strobe(LX lx) {
    super(lx);

    addParameter("speed", speed);
    addParameter("balance", balance);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    if (triggered) {
      timer += deltaMs;
      if (timer >= speed.getValuef() * (on ? balance.getValuef() : 1 - balance.getValuef())) {
        timer = 0;
        on = !on;
      }

      setColors(on ? LXColor.WHITE : LXColor.BLACK);
    }
  }

  public void onTriggered(float strength) {
    super.onTriggered(strength);

    on = true;
  }

  public void onRelease() {
    super.onRelease();

    timer = 0;
    on = false;
    setColors(LXColor.BLACK);
  }
}
