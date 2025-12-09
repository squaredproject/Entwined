package entwined.pattern.kyle_fleming;

import entwined.utils.EntwinedUtils;
import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class CandyCloud extends LXPattern {

  final CompoundParameter darkness = new CompoundParameter("DARK", 8, 0, 12);

  final CompoundParameter scale = new CompoundParameter("SCAL", 2400, 600, 10000);
  final CompoundParameter speed = new CompoundParameter("SPD", 1, 1, 2);

  double time = 0;

  public CandyCloud(LX lx) {
    super(lx);

    addParameter("darkness", darkness);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    time += deltaMs;
    for (LXPoint cube : model.points) {
      double adjustedX = cube.x / scale.getValue();
      double adjustedY = cube.y / scale.getValue();
      double adjustedZ = cube.z / scale.getValue();
      double adjustedTime = time * speed.getValue() / 5000;

      float hue = ((float)SimplexNoise.noise(adjustedX, adjustedY, adjustedZ, adjustedTime) + 1) / 2 * 1080 % 360;

      float brightness = EntwinedUtils.min(EntwinedUtils.max((float)SimplexNoise.noise(cube.x / 250, cube.y / 250, cube.z / 250 + 10000, time / 5000) * 8 + 8 - darkness.getValuef(), 0), 1) * 100;

      colors[cube.index] = LX.hsb(hue, 100, brightness);
    }

  }
}
