package entwined.pattern.mary_wang;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Twinkle extends LXPattern {

  private SinLFO[] bright;
  final CompoundParameter brightnessParam = new CompoundParameter("Brightness", 0.8, 0.5, 1);
  final int numBrights = 18;
  final int density = 20;
  int[] sparkleTimeOuts;
  int[] cubeToModulatorMapping;

  public Twinkle(LX lx) {
    super(lx);
    addParameter("brightness", brightnessParam);

    sparkleTimeOuts = new int[model.points.length];
    cubeToModulatorMapping = new int[model.points.length];

    for (int i = 0; i < cubeToModulatorMapping.length; i++ ) {
      cubeToModulatorMapping[i] = (int)EntwinedUtils.random(numBrights);
    }

    bright = new SinLFO[numBrights];
    int numLight = density / 100 * bright.length; // number of brights array that are most bright
    int numDarkReverse = (bright.length - numLight) / 2; // number of brights array that go from light to dark

    for (int i = 0; i < bright.length; i++ ) {
      if (i <= numLight) {
        if (EntwinedUtils.random(1) < 0.5f) {
          bright[i] = new SinLFO((int)EntwinedUtils.random(80, 100), 0, (int)EntwinedUtils.random(2300, 7700));
        }
        else {
          bright[i] = new SinLFO(0, (int)EntwinedUtils.random(70, 90), (int)EntwinedUtils.random(5300, 9200));
        }
      }
      else if ( i < numDarkReverse ) {
        bright[i] = new SinLFO((int)EntwinedUtils.random(50, 70), 0, (int)EntwinedUtils.random(3300, 11300));
      }
      else {
        bright[i] = new SinLFO(0, (int)EntwinedUtils.random(30, 80), (int)EntwinedUtils.random(3100, 9300));
      }
      addModulator(bright[i]).start();
    }
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    for (LXPoint cube : model.points) {
      if (sparkleTimeOuts[cube.index] < EntwinedUtils.millis()) {
        // randomly change modulators
        if (EntwinedUtils.random(10) <= 3) {
          cubeToModulatorMapping[cube.index] = (int)EntwinedUtils.random(numBrights);
        }
        sparkleTimeOuts[cube.index] = EntwinedUtils.millis() + (int)EntwinedUtils.random(11100, 23300);
      }
      colors[cube.index] = LX.hsb(
        0,
        0,
        bright[cubeToModulatorMapping[cube.index]].getValuef() * brightnessParam.getValuef()
        );
    }
  }
}

