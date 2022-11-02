package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

public class RandomColorGlitch extends LXPattern {

  public RandomColorGlitch(LX lx) {
    super(lx);
  }

  final int brokenCubeIndex = (int)EntwinedUtils.random(model.points.length);

  final int cubeColor = (int)EntwinedUtils.random(360);

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint cube : model.points) {
      if (cube.index == brokenCubeIndex) {
        colors[cube.index] = LX.hsb(
          EntwinedUtils.random(360),
          100,
          100
        );
      } else {
        colors[cube.index] = LX.hsb(
          cubeColor,
          100,
          100
        );
      }
    }
  }
}