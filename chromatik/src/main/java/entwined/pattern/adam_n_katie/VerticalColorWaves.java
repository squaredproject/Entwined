package entwined.pattern.adam_n_katie;

import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

/**
Vertical hue cycle waves starting at the ground and moving upward.
*/
public class VerticalColorWaves extends AutographedPattern{
  // Constants
  static final float speedMult = 1000;

  // Parameters (to show up on UI).
  final CompoundParameter speedParam = new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter waveSlope = new CompoundParameter("waveSlope", 360, 1, 720);
  final SawLFO wave = new SawLFO(0, 360, speedParam.getValuef() * speedMult);

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if you're using Parameters or oscillators
  public VerticalColorWaves(LX lx){
    super(lx);
    addModulator(wave).start();
    addParameter("waveSlope", waveSlope);
    addParameter("speed", speedParam);
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    wave.setPeriod(speedParam.getValuef() * speedMult);

    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points){

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      // Determine the hue to use.
      float val0To1 = 1.0f - cubePos0To1.y;
      float hue0To360 = (wave.getValuef() + waveSlope.getValuef() * val0To1 ) % 360;

      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = LX.hsb( hue0To360, 100, 100);
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class VerticalColorWaves extends TSPattern