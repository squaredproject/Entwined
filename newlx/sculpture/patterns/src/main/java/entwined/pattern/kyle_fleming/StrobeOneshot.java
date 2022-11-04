package entwined.pattern.kyle_fleming;

import entwined.core.TSTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.pattern.LXPattern;

public class StrobeOneshot extends TSTriggerablePattern {

  StrobeOneshot(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    setColors(LXColor.WHITE);
  }

  @Override
  public void run(double deltaMs) {
    firedTimer += deltaMs;
    if (firedTimer >= 80) {
      setCallRun(false);
    }
  }
}
