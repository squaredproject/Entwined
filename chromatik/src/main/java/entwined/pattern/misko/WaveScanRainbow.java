package entwined.pattern.misko;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

// Modified BB to have different controllers for Hue and Line Speed
public class WaveScanRainbow extends LXPattern {
  // Variable Declarations go here
  // private float waveWidth = 1;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms = 0.0;
  final CompoundParameter lSpeedParam = new CompoundParameter("LSpeed", 10, 1, 30);
  final CompoundParameter hSpeedParam = new CompoundParameter("HSpeed", 2000, 1000, 20000);
  final CompoundParameter waveSlope = new CompoundParameter("wvSlope", 360, 1, 720);
  final CompoundParameter theta = new CompoundParameter("theta", 45, 0, 360);
  final CompoundParameter wave_width = new CompoundParameter("wvWidth", 20, 1, 50);
  final SawLFO wave360 = new SawLFO(0, 360, hSpeedParam);  // Hue LFO - period in ms

  public WaveScanRainbow(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addParameter("waveSlope", waveSlope);
    addParameter("waveWidth", wave_width);
    addParameter("lSpeed", lSpeedParam);
    addParameter("hueSpeed", hSpeedParam);
    addParameter("theta", theta);
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
                (dist(cube.x,cube.z)/(wave_width.getValuef())) + (lSpeedParam.getValuef()*total_ms/1000.0))
            + 1.0)
          );
        colors[cube.index] = LX.hsb( wave360.getValuef(), 100, d);
      }
  }
}

