package entwined.pattern.anon;

import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.utils.LXUtils;

import entwined.core.CubeManager;
import entwined.core.CubeData;

// NB - This one works.
public class Twister extends LXPattern {

  final SinLFO spin = new SinLFO(0, 5*360, 16000);
  boolean firstTime = true;

  float coil(float basis) {
    return (float) Math.sin(basis*2*Math.PI - Math.PI);
  }

  public Twister(LX lx) {
    super(lx);
    addModulator(spin).start();
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;  // XXX Mark sez we probably shouldn't do this

    float spinf = spin.getValuef(); // * 180/LX.PIf;
    float coilf = 2*coil(spin.getBasisf());

    float currentBaseHue = lx.engine.palette.color.getHuef();  // XXX this modulates in the previous LX studio

    for (LXModel child: model.children) {
      /* float offsetX = child.cx;
      float offsetZ = child.cz;
      float offsetY = 0;  // NB - this should be 0 for current models
      float pi = LX.PIf; */
      for (LXPoint cube: child.points) {
        // Calculate local coordinates. Note that our code wants theta in degrees,
        // not radians.
        // float localY = cube.y - offsetY;
        //
        //float localTheta = (float) Math.atan2(cube.z - offsetZ, cube.x - offsetX);
        CubeData cubeData = CubeManager.getCube(cube.index);

        float localTheta = cubeData.localTheta; // XXX want in radians and degrees... And
        float localY = cubeData.localY;
        //localTheta = (180 + 180/pi*localTheta) % 360;  // just in case...
        float wrapdist = LXUtils.wrapdistf(localTheta, (spinf + (model.yMax - cube.y)*coilf)%360, 360);
        float yn = cube.y/model.yMax;
        float width = 10 + 30 * yn;
        float df = LXUtils.maxf(0, 100 - (100 / 45) * LXUtils.maxf(0, wrapdist-width));
        colors[cube.index] = LX.hsb(
          (currentBaseHue + .2f*localY - 360 - wrapdist) % 360,
          LXUtils.maxf(0, 100 - 500*LXUtils.maxf(0, yn-.8f)),
          df
        );
      }
    }
  }
}

