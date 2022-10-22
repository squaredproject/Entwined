package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.pattern.LXPattern;

public class Brightness extends LXPattern { // XXX TSTriggerablePattern {

  public Brightness(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
  }

  public void onTriggered(float strength) {
    setColors(LX.hsb(0, 0, 100 * strength));
  }

  public void onRelease() {
    setColors(LXColor.BLACK);
  }
}