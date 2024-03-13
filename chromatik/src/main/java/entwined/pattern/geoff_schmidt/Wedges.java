package entwined.pattern.geoff_schmidt;

import entwined.core.CubeManager;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Wedges extends LXPattern {
  final CompoundParameter pSpeed = new CompoundParameter("SPD", .52);
  final CompoundParameter pCount = new CompoundParameter("COUNT", 4.0/15.0);
  final CompoundParameter pSat = new CompoundParameter("SAT", 5.0/15.0);
  final CompoundParameter pHue = new CompoundParameter("HUE", .5);
  double rotation = 0; // degrees

  public Wedges(LX lx) {
    super(lx);

    addParameter("speed", pSpeed);
    addParameter("count", pCount);
    addParameter("saturation", pSat);
    addParameter("hue", pHue);
    rotation = 0;
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float vSpeed = pSpeed.getValuef();
    float vCount = pCount.getValuef();
    float vSat = pSat.getValuef();
    float vHue = pHue.getValuef();

    rotation += deltaMs/1000.0f * (2 * (vSpeed - .5f) * 360.0f * 1.0f);
    rotation = rotation % 360.0f;

    double sections = Math.floor(1.0f + vCount * 10.0f);
    double quant = 360.0f/sections;

    for (LXPoint cube : model.points) {
      colors[cube.index] = LXColor.hsb(
        Math.floor((rotation - CubeManager.getCube(lx, cube.index).localTheta) / quant) * quant + vHue * 360.0f,
        (1 - vSat) * 100,
        100
      );
    }
  }
}

