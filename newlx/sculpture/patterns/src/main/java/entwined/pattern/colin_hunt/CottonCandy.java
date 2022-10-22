package entwined.pattern.colin_hunt;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class CottonCandy extends LXPattern {

  // Variable Declarations go here

  final BoundedParameter speed = new BoundedParameter("Speed", 2500, 20000, 1000);
  final BoundedParameter swirlMult = new BoundedParameter("Swirl", .5, 2, .1);
  final SawLFO spinner = new SawLFO(0, 360, speed);

  float hue = 0;

  // Constructor
  public CottonCandy(LX lx) {
    super(lx);

    addModulator(spinner).start();
    addParameter("speed", speed);
    addParameter("swirl", swirlMult);

  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {
    float spinnerVal = spinner.getValuef();

    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint cube : model.points) {
// XXX  theta and r on the points
      if (((cube.theta + spinnerVal
      // plus the further from the center, the more hue is added, giving a swirl effect
      - cube.r / 2// * swirlMult.getValuef()
      ) % 120) > 60) {
        hue = 330;
      } else {
        hue = 180;
      }
      colors[cube.index] = LX.hsb(hue, 100.0f, 100.0f);
    }
  }
}