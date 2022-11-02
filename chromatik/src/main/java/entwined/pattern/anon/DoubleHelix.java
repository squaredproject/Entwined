package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

import entwined.utils.EntwinedUtils;
import entwined.core.CubeManager;

public class DoubleHelix extends LXPattern {

  final SinLFO rate = new SinLFO(400, 3000, 11000);
  final SawLFO theta = new SawLFO(0, 180, rate);
  final SinLFO coil = new SinLFO(0.2, 2, 13000);

  public DoubleHelix(LX lx) {
    super(lx);
    addModulator(rate).start();
    addModulator(theta).start();
    addModulator(coil).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float currentBaseHue = lx.engine.palette.color.getHuef();

    for (LXModel component : model.children) {
      // Top level children only
      // Transformed points are what happens when you apply a transform to a model, It's an alternate way of changing a pattern.
      // These are *not* the local coordinates of the point.
      for (LXPoint cube : component.points) {
        float coilf = coil.getValuef() * (cube.y - model.cy);
        float localTheta = CubeManager.getCube(cube.index).localTheta;

        colors[cube.index] = LX.hsb(
          currentBaseHue + .4f*EntwinedUtils.abs(cube.y - model.cy) +.2f* EntwinedUtils.abs(localTheta * 180/LX.PIf - 180),
          100,
          EntwinedUtils.max(0, 100 - 2*LXUtils.wrapdistf(localTheta, theta.getValuef() + coilf, 180))
        );
      }
    }
  }
}