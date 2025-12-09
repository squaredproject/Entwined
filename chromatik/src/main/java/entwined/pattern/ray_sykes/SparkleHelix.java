package entwined.pattern.ray_sykes;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.utils.LXUtils;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

// import org.apache.commons.lang3.ArrayUtils;

public class SparkleHelix extends LXPattern {
  final CompoundParameter minCoil = new CompoundParameter("MinCOIL", .02, .005, .05);
  final CompoundParameter maxCoil = new CompoundParameter("MaxCOIL", .03, .005, .05);
  final CompoundParameter sparkle = new CompoundParameter("Spark", 80, 160, 10);
  final CompoundParameter sparkleSaturation = new CompoundParameter("Sat", 50, 0, 100);
  final CompoundParameter counterSpiralStrength = new CompoundParameter("Double", 0, 0, 1);

  final SinLFO coil = new SinLFO(minCoil, maxCoil, 8000);
  final SinLFO rate = new SinLFO(6000, 1000, 19000);
  final SawLFO spin = new SawLFO(0, LX.TWO_PI, rate);
  final SinLFO width = new SinLFO(10, 20, 11000);
  int[] sparkleTimeOuts;

  public SparkleHelix(LX lx) {
    super(lx);
    addParameter("minCoil", minCoil);
    addParameter("maxCoil", maxCoil);
    addParameter("sparkle", sparkle);
    addParameter("sparkleSat", sparkleSaturation);
    addParameter("double", counterSpiralStrength);
    addModulator(rate).start();
    addModulator(coil).start();
    addModulator(spin).start();
    addModulator(width).start();
    sparkleTimeOuts = new int[model.points.length];
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float coilValue = coil.getValuef();
    float widthValue = width.getValuef();
    float spinValue = spin.getValuef();
    float counterSpiralStrengthValue = counterSpiralStrength.getValuef();
    float sparkleValue = sparkle.getValuef();
    float currentBaseHue = lx.engine.palette.color.getHuef();

    for (LXModel component:  model.children) {
      for (LXPoint cube : component.points) {
        CubeData cubeData = CubeManager.getCube(lx, cube.index);
        float localTheta = cubeData.localTheta * LX.PIf/180;  // wrapping using radians...
        float compensatedWidth = (0.7f + .02f / coilValue) * widthValue;
        float wrapAngleForward = (spinValue + coilValue * cubeData.localY) % LX.TWO_PIf;
        float wrapAngleBackwards = LX.TWO_PIf - wrapAngleForward;
        float spiralVal = LXUtils.maxf(
            0,
            100 - ((100*LX.TWO_PIf/(compensatedWidth))*LXUtils.wrapdistf(localTheta, wrapAngleForward, LX.TWO_PIf))
        );
        float counterSpiralVal = counterSpiralStrengthValue * LXUtils.maxf(
            0,
            100 - (100*LX.TWO_PIf/(compensatedWidth)*LXUtils.wrapdistf(localTheta, wrapAngleBackwards, LX.TWO_PIf))
        );
        float hueVal = (currentBaseHue + .1f*cubeData.localY) % 360;
        if (sparkleTimeOuts[cube.index] > EntwinedUtils.millis()){
          colors[cube.index] = LX.hsb(hueVal, sparkleSaturation.getValuef(), 100);
        }
        else{
          sparkleTimeOuts[cube.index] -= deltaMs;
          colors[cube.index] = LX.hsb(hueVal, 100, (LXUtils.maxf(spiralVal, counterSpiralVal)) % 100);
          if (EntwinedUtils.random(LXUtils.maxf(spiralVal, counterSpiralVal)) > sparkleValue){
            sparkleTimeOuts[cube.index] = EntwinedUtils.millis() + 100;
          }
        }
      }
    }
  }
}


