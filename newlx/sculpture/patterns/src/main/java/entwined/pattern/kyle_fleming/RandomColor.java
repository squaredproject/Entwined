package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class RandomColor extends LXPattern {

  final BoundedParameter speed = new BoundedParameter("Speed", 1, 1, 10);

  int frameCount = 0;

  public RandomColor(LX lx) {
    super(lx);
    addParameter("speed", speed);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    frameCount++;
    if (frameCount >= speed.getValuef()) {
      for (LXPoint cube : model.points) {
        colors[cube.index] = LX.hsb(
          EntwinedUtils.random(360),
          100,
          100
        );
      }
      frameCount = 0;
    }
  }
}
