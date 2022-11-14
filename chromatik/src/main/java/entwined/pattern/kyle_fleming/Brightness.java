package entwined.pattern.kyle_fleming;

import entwined.core.TSTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class Brightness extends TSTriggerablePattern {

  public Brightness(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
  }

  @Override
  public void onTriggered() {
    super.onTriggered();
    float strength = 1.0f;  // XXX - used to be from the drumpad value. Giving up on that for right now.
    setColors(LX.hsb(0, 0, 100 * strength));
  }

  @Override
  public void onReleased() {
    super.onReleased();
    setColors(LXColor.BLACK);
  }
}