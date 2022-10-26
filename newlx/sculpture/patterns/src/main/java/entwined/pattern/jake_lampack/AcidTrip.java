package entwined.pattern.jake_lampack;

import entwined.core.CubeManager;
import entwined.core.CubeData;
import entwined.utils.EntwinedUtils;

import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;

public class AcidTrip extends LXPattern {

  final SawLFO trails = new SawLFO(360, 0, 7000);

  public AcidTrip(LX lx) {
    super(lx);

    addModulator(trails).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint cube : model.points) {
      CubeData cdata = CubeManager.getCube(cube.index);
      colors[cube.index] = LX.hsb(
        EntwinedUtils.abs(model.cy - cdata.localY) + EntwinedUtils.abs(model.cy - cdata.localTheta) + trails.getValuef() % 360,
        100,
        100
      );
    }
  }
}
