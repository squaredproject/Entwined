package entwined.pattern.kyle_fleming;

import entwined.core.TSTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;


public class Strobe extends TSTriggerablePattern {

  final CompoundParameter speed = new CompoundParameter("SPEE", 200, 3000, 30).setExponent(.5);
  final CompoundParameter balance = new CompoundParameter("BAL", .5, .01, .99);

  int timer = 0;
  boolean on = false;

  public Strobe(LX lx) {
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

  @Override
  public void onTriggered() {
    super.onTriggered();

    on = true;
  }

  @Override
  public void onReleased() {
    super.onReleased();

    timer = 0;
    on = false;
    setColors(LXColor.BLACK);
  }
}
