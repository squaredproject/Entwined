package entwined.pattern.misko;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class Circles extends LXPattern {

  // Variable Declarations go here
  private float minz = Float.MAX_VALUE;
  private float maxz = -Float.MAX_VALUE;
  // private float waveWidth = 1;
  private float speedMult = 1000;

  final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter waveSlope = new CompoundParameter("wvSlope", 360, 1, 720);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SawLFO wave100 = new SawLFO(0, 100, speedParam.getValuef() * speedMult);

  // add speed, wave width

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if you're using Parameters or sin waves
  public Circles(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter("waveSlope", waveSlope);
    addParameter("speedParam", speedParam);

    for (LXPoint cube : model.points) {
      if (cube.z < minz) {minz = cube.z;}
      if (cube.z > maxz) {maxz = cube.z;}
    }
  }


  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);

      // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points) {
      CubeData cdata = CubeManager.getCube(lx, cube.index);
      float v = (float)( (-wave360.getValuef() + waveSlope.getValuef()) + Math.sqrt(Math.pow(cdata.localX,2)+Math.pow(cdata.localZ,2))*5 );
      colors[cube.index] = LX.hsb( v % 360, 100,  100);
      //colors[cube.index] = lx.hsb( (float)( Math.sqrt(Math.pow(cube.sx,2)+Math.pow(cube.sz,2)) % 360), 100, 100);
    }
  }
}
