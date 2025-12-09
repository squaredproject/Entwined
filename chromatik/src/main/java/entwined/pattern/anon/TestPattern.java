package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

@LXCategory(LXCategory.TEST)
public class TestPattern extends LXPattern {

  int CUBE_MOD = 14;

  final CompoundParameter period = new CompoundParameter("RATE", 3000, 2000, 6000);
  final SinLFO cubeIndex = new SinLFO(0, CUBE_MOD, period);

  public TestPattern(LX lx) {
    super(lx);
    addModulator(cubeIndex).start();
    addParameter("period", period);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    int ci = 0;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    for (LXPoint cube : model.points) {
      setColor(cube, LX.hsb(
        (currentBaseHue + cube.x + cube.y) % 360,
        100,
        Math.max(0, 100 - 30* Math.abs((ci % CUBE_MOD) - cubeIndex.getValuef()))
      ));
      ++ci;
    }
  }
}
