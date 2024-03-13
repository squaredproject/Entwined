package entwined.pattern.irene_zhou;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.utils.LXUtils;

public class Springs extends LXPattern {
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
  private BooleanParameter automated = new BooleanParameter("AUTO", true);
  private final Accelerator gravity = new Accelerator(0, 0, 0);
  private final Click reset = new Click(9600);
  private boolean isRising = false;
  final SinLFO spin = new SinLFO(0, 360, 9600);

  float coil(float basis) {
    return 4 * LXUtils.sinf(basis*LX.TWO_PIf + LX.PIf) ;
  }

  public Springs(LX lx) {
    super(lx);
    addModulator("gravity", gravity);
    addModulator("reset", reset).start();
    addModulator("spin", spin).start();
    addParameter("hue", hue);
    addParameter("automated", automated);
    trigger();
  }

  @Override
  public void onParameterChanged(LXParameter parameter) {
    super.onParameterChanged(parameter);
    if (parameter == automated) {
      if (automated.isOn()) {
        trigger();
      }
    }
  }

  private void trigger() {
    isRising = !isRising;
    if (isRising) {
      gravity.setSpeed(0.25f, 0).start();
    }
    else {
      gravity.setVelocity(0).setAcceleration(-1.75f);
    }
  }

  @Override
  public void run(double deltaMS) {
    if (getChannel().fader.getNormalized() == 0) return;

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    if (!isRising) {
      gravity.start();
      if (gravity.getValuef() < 0) {
        gravity.setValue(-gravity.getValuef());
        gravity.setVelocity(-gravity.getVelocityf() * EntwinedUtils.random(0.74f, 0.84f));
      }
    }

    float spinf = spin.getValuef();
    //float coilf = 2*coil(spin.getBasisf());

    for (LXPoint cube : model.points) {
      CubeData cdata = CubeManager.getCube(lx, cube.index);
      float yn =  cdata.localY/model.yMax;
      float width = (1-yn) * 25;
      float wrapdist = LXUtils.wrapdistf(cdata.localTheta, (spinf + (cdata.localY) * 1/(gravity.getValuef() + 0.2f))%360, 360);
      float df = EntwinedUtils.max(0, 100 - EntwinedUtils.max(0, wrapdist-width));
      colors[cube.index] = LX.hsb(
        EntwinedUtils.max(0, (currentBaseHue - yn * 20 + hue.getValuef()) % 360),
        EntwinedUtils.constrain((1- yn) * 100 + wrapdist, 0, 100),
        EntwinedUtils.max(0, df - yn * 50)
      );
    }
  }
}
