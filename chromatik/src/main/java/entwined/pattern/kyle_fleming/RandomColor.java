package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class RandomColor extends LXPattern {

  final CompoundParameter speed = new CompoundParameter("Speed", 200.0, 20.0, 1500.0);

  double msCount = 0.0f;

  public RandomColor(LX lx) {
    super(lx);
    addParameter("speed", speed);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    msCount += deltaMs;
    if (msCount >= speed.getValue()) {
      for (LXPoint cube : model.points) {
        colors[cube.index] = LX.hsb(
          EntwinedUtils.random(360),
          100,
          100
        );
      }
      msCount = 0.0f;
    }
  }
}
