package entwined.pattern.colin_hunt;

import entwined.core.CubeManager;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import org.openjdk.nashorn.api.tree.CompoundAssignmentTree;

public class CottonCandy extends LXPattern {

  // Variable Declarations go here

  final CompoundParameter speed = new CompoundParameter("Speed", 2500, 20000, 1000);
  final CompoundParameter swirlMult = new CompoundParameter("Swirl", .5, 2, .1);
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
      if (((CubeManager.getCube(lx, cube.index).localTheta + spinnerVal
      // plus the further from the center, the more hue is added, giving a swirl effect
      - CubeManager.getCube(lx, cube.index).localR / 2// * swirlMult.getValuef()
      ) % 120) > 60) {
        hue = 330;
      } else {
        hue = 180;
      }
      colors[cube.index] = LX.hsb(hue, 100.0f, 100.0f);
    }
  }
}