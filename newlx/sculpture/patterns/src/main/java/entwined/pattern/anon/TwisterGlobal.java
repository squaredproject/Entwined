package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.utils.LXUtils;
import heronarts.lx.model.LXPoint;

import entwined.utils.EntwinedUtils;

public class TwisterGlobal extends LXPattern {

  final SinLFO spin = new SinLFO(0, 5*360, 16000);

  float coil(float basis) {
    return EntwinedUtils.sin(basis*LX.TWO_PIf - LX.PIf);
  }

  public TwisterGlobal(LX lx) {
    super(lx);
    addModulator(spin).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    float spinf = spin.getValuef();
    float coilf = 2*coil(spin.getBasisf());
    for (LXPoint cube : model.points) {
      float wrapdist = LXUtils.wrapdistf(cube.theta, (spinf + (model.yMax - cube.y)*coilf) % 360, 360);
      float yn = (cube.y / model.yMax);
      float width = 10 + 30 * yn;
      float df = EntwinedUtils.max(0, 100 - (100 / 45) * EntwinedUtils.max(0, wrapdist-width));
      colors[cube.index] = LX.hsb(
        (currentBaseHue + .2f*cube.y - 360 - wrapdist) % 360,
        EntwinedUtils.max(0, 100 - 500*EntwinedUtils.max(0, yn-.8f)),
        df
      );
    }
  }
}
