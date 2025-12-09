package entwined.pattern.irene_zhou;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class Lattice extends LXPattern {
  final SawLFO spin = new SawLFO(0, 4320, 24000);
  final SinLFO yClimb = new SinLFO(60, 30, 24000);
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
  final CompoundParameter yHeight = new CompoundParameter("HEIGHT", 0, -500, 500);

  float coil(float basis) {
    return EntwinedUtils.sin(basis*LX.PIf);
  }

  public Lattice(LX lx) {
    super(lx);
    addModulator(spin).start();
    addModulator(yClimb).start();
    addParameter("hue", hue);
    addParameter("yHeight", yHeight);
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float spinf = spin.getValuef();
    float coilf = 2*coil(spin.getBasisf());
    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    for (LXPoint cube : model.points) {
      CubeData cdata = CubeManager.getCube(lx, cube.index);
      float wrapdistleft  = LXUtils.wrapdistf(cdata.localTheta,  (spinf + (model.yMax - cdata.localY) * coilf) % 180, 180);
      float wrapdistright = LXUtils.wrapdistf(cdata.localTheta, (-spinf - (model.yMax - cdata.localY) * coilf) % 180, 180);
      float width = yClimb.getValuef() + ((cdata.localY - yHeight.getValuef())/model.yMax) * 50;
      float df = EntwinedUtils.min(100, 3 * EntwinedUtils.max(0, wrapdistleft - width) + 3 * EntwinedUtils.max(0, wrapdistright - width));

      colors[cube.index] = LX.hsb(
        (hue.getValuef() + currentBaseHue + .2f*cdata.localY - 360) % 360,
        100,
        df
      );
    }
  }
}

