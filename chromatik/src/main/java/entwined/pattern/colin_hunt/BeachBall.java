package entwined.pattern.colin_hunt;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

/**
RGB spiral from center tree
*/
public class BeachBall extends LXPattern {

  // Variable Declarations go here

  final CompoundParameter speed = new CompoundParameter("Speed", 5000, 20000, 1000);
  final CompoundParameter swirlMult = new CompoundParameter("Swirl", .5, 2, .1);
  final SawLFO spinner = new SawLFO(0, 360, speed);

  // Constructor
  public BeachBall(LX lx) {
    super(lx);

    addModulator(spinner).start();
    addParameter("speed", speed);
    addParameter("swirl", swirlMult);
  }


  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint baseCube : model.points) {
      colors[baseCube.index] = LX.hsb(
      // Color is based on degrees from the center point, plus the spinner saw wave to rotate
      baseCube.theta + spinner.getValuef()
      // plus the further from the center, the more hue is added, giving a swirl effect
      // XXX - how is rc different from r?
      - baseCube.r * swirlMult.getValuef(),
      100.0f,
      100.0f);
    }
  }
}