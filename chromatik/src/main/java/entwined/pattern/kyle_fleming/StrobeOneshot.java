package entwined.pattern.kyle_fleming;

import entwined.core.TSTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class StrobeOneshot extends TSTriggerablePattern {
  private double firedTimer = 0;

  public StrobeOneshot(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;
  }

  @Override
  public void run(double deltaMs) {
    firedTimer += deltaMs;
    if (firedTimer >= 80) {
      firedTimer=0;
      this.enabled.setValue(false);
    } else {
      setColors(LXColor.WHITE);
    }
  }
}

