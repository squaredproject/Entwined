package entwined.pattern.kyle_fleming;

import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

public class AcidTripTextureEffect extends LXEffect {

  final CompoundParameter amount = new CompoundParameter("ACID");

  final SawLFO trails = new SawLFO(360, 0, 7000);

  public AcidTripTextureEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);
    addModulator(trails).start();
  }

  @Override
  public void run(double deltaMs, double strength) {
    if (amount.getValue() > 0) {
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        LXPoint cube = model.points[i];
        // TODO ashley modify the rest of the file for shrubCubes
        // ShrubCube shrubCube = model.shrubCubes.get(i);

        float newHue = EntwinedUtils.abs(model.cy - CubeManager.getCube(lx, cube.index).localY) + EntwinedUtils.abs(model.cy - CubeManager.getCube(lx, cube.index).localTheta) + trails.getValuef() % 360;
        int newColor = LX.hsb(newHue, 100, 100);
        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = LX.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }
}
