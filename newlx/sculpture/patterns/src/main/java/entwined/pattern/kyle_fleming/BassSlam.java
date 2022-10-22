package entwined.pattern.kyle_fleming;

import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

public class BassSlam extends LXPattern { // XXX TSTriggerablePattern {

  final private double flashTimePercent = 0.1f;
  final private int patternHue = 200;

  BassSlam(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    if (triggerableModeEnabled) {
      firedTimer += deltaMs / 800;
      if (firedTimer > 1) {
        setCallRun(false);
        return;
      }
    }

    if (progress() < flashTimePercent) {
      setColors(LX.hsb(patternHue, 100, 100));
    } else {
      float time = (float)((progress() - flashTimePercent) / (1 - flashTimePercent) * 1.3755f);
      float y;
      // y = 0 when time = 1.3755f
      if (time < 1) {
        y = 1 + EntwinedUtils.pow(time + 0.16f, 2) * EntwinedUtils.sin(18 * (time + 0.16f)) / 4;
      } else {
        y = 1.32f - 20 * EntwinedUtils.pow(time - 1, 2);
      }
      y = EntwinedUtils.max(0, 100 * (y - 1) + 250);

      for (LXPoint cube : model.points) {
        setColor(cube.index, LX.hsb(patternHue, 100, EntwinedUtils.constrainf(100 - 2 * EntwinedUtils.abs(y - CubeManager.getCube(cube.index).localY), 0, 100)));
      }
    }
  }

  double progress() {
    return triggerableModeEnabled ? ((firedTimer + flashTimePercent) % 1) : lx.engine.tempo.ramp();
  }
}
