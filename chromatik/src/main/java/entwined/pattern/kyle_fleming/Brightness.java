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
  public void onTriggered(float strength) {
    setColors(LX.hsb(0, 0, 100 * strength));
  }

  @Override
  public void onRelease() {
    setColors(LXColor.BLACK);
  }
}