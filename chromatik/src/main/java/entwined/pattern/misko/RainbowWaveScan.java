package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

// Modified BB to have different controllers for Hue and Line Speed
public class RainbowWaveScan extends LXPattern {
  // Variable Declarations go here
  // private float waveWidth = 1;
  // private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms = 0.0;
  final BoundedParameter speedParam = new BoundedParameter("Speed", 5, 20, .01);
  final BoundedParameter lSpeedParam = new BoundedParameter("LSpeed", 5000, 100000, 1000);
  final BoundedParameter hSpeedParam = new BoundedParameter("HSpeed", 5000, 1000, 1000000);
  final BoundedParameter waveSlope = new BoundedParameter("wvSlope", 360, 1, 720);
  final BoundedParameter theta = new BoundedParameter("theta", 45, 0, 360);
  final BoundedParameter hue = new BoundedParameter("hue", 45, 0, 360);
  final BoundedParameter wave_width = new BoundedParameter("wvWidth", 20, 1, 50);
  final SawLFO wave360 = new SawLFO(0, 360, hSpeedParam);  // Hue LFO - period in ms
  final SinLFO wave100 = new SinLFO(0, 100, lSpeedParam);  // brightness LFO

  public RainbowWaveScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter("waveSlope", waveSlope);
    addParameter("waveWidth", wave_width);
    addParameter("lSpeedParam", lSpeedParam);
    addParameter("hSpeedParam", hSpeedParam);
    addParameter("theta", theta);
    addParameter("hue", hue);


  }
  private float dist(float  x, float z) {
    return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    total_ms+=deltaMs;
    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points) {
      float d = (float)( 50.0 *
          (Math.sin(
                (dist(cube.x,cube.z)/(wave_width.getValuef())) + (wave100.getValuef()*total_ms/1000.0))
            + 1.0)
          );
        colors[cube.index] = LX.hsb( wave360.getValuef(), 100, d);
      }
  }
}

