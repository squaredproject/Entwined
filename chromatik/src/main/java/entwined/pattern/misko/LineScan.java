package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

public class LineScan extends LXPattern {
  // Variable Declarations go here
  // private float waveWidth = 1;
  private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms =0.0;
  final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter waveSlope = new CompoundParameter("wvSlope", 360, 1, 720);
  final CompoundParameter theta = new CompoundParameter("theta", 45, 0, 360);
  final CompoundParameter hue = new CompoundParameter("hue", 45, 0, 360);
  final CompoundParameter wave_width = new CompoundParameter("wvWidth", 500, 10, 1500);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

  public LineScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter("waveSlope", waveSlope);
    addParameter("speedParam", speedParam);
    addParameter("theta", theta);
    addParameter("hue", hue);
    addParameter("waveWidth", wave_width);


  }
  private float dist(float  x, float z) {
  return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0)       return;

    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);

    float field_len=7000;
    total_ms+=deltaMs;
    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points) {
      double d = Math.abs(dist(cube.x,cube.z)-speedParam.getValuef()*(total_ms%field_len-field_len/2)/10);
      if (d<wave_width.getValuef()) {
        //float d = (float)(50.0*(Math.sin(dist(cube.x,cube.z)/(wave_width.getValuef()) + speedParam.getValuef()*total_ms/1000.0)+1.0));
        colors[cube.index] = LX.hsb( hue.getValuef()  , 100, (float)((1.0-d/wave_width.getValuef())*100.0 ));
      } else {
        colors[cube.index] = LX.hsb( hue.getValuef()  , 100, 0);
      }
    }
  }
}

