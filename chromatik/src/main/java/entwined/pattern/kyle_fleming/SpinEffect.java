package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.effect.LXEffect;

import entwined.core.CubeManager;


public class SpinEffect extends LXEffect{

  public final CompoundParameter spin = new CompoundParameter("SPIN");
  final FunctionalParameter rotationPeriodMs = new FunctionalParameter() {
    @Override
    public double getValue() {
      return 5000 - 4800 * spin.getValue();
    }
  };
  public final SawLFO rotation = new SawLFO(0, 360, rotationPeriodMs);

  public SpinEffect(final LX lx) {
    super(lx);

    addModulator(rotation);
    spin.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (spin.getValue() > 0) {
          rotation.start();
          rotation.setLooping(true);
        } else {
          rotation.setLooping(false);
        }
      }
    });
  }

  @Override
  public void run(double deltaMs, double strength) {
    if (rotation.getValue() > 0 && rotation.getValue() < 360) {
      float rotationTheta = rotation.getValuef();
      for (LXPoint cube : model.points) {
        // XXX need to transform the point values
        // As well as model max, maxY, maxZ
        //cube.transformedTheta = (cube.transformedTheta + 360 - rotationTheta) % 360;
      }
    }
  }
}
