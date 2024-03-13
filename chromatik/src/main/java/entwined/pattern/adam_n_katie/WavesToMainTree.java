package entwined.pattern.adam_n_katie;

import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

/**
Circular hue cycle waves starting far from the main tree and moving toward it.
*/

public class WavesToMainTree extends AutographedPattern{
  // Constants
  static final float speedMult = 1000;

  // Parameters (to show up on UI).
  final CompoundParameter speedParam =
    new CompoundParameter("Speed", 5, 20, .01);
  final CompoundParameter waveSlope =
    new CompoundParameter("waveSlope", 360, 1, 720);
  final SawLFO wave =
    new SawLFO(0, 360, speedParam.getValuef() * speedMult);

  // Variables
  private Vec3D theMainTreePos0To1;
  private float distFromMainTreeXZMax = -Float.MAX_VALUE;

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if you're using
  //  Parameters or oscillators
  public WavesToMainTree(LX lx){
    super(lx);
    addModulator(wave).start();
    addParameter("waveSlope", waveSlope);
    addParameter("speed", speedParam);

    // Get the Main tree's position.
    float centerElement_x = 0;
    float centerElement_z = 0; // we're on a plane and we don't care about y.
    try {
      LXModel centerElement = model.sub("CENTER").get(0);
      if (centerElement != null) {
        centerElement_x = centerElement.cx;
        centerElement_z = centerElement.cz;
      }
    } catch (Exception e) {
      System.out.println("WavesToMainTree: no center object, using 0,0 of sculpture");
    }
    theMainTreePos0To1 =
      PosRawToPos0To1(centerElement_x, 0.0f, centerElement_z);

    // Get the maximum distance from the Main tree.
    for (LXPoint cube : model.points){
      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      Vec3D posRelToMainTreeXZ = cubePos0To1.sub(theMainTreePos0To1);
      posRelToMainTreeXZ.y = 0.0f;

      float distFromMainTreeXZ =
        posRelToMainTreeXZ.magnitude();
      if(distFromMainTreeXZ > distFromMainTreeXZMax){
        distFromMainTreeXZMax = distFromMainTreeXZ;
      }
    }
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    // uPdate the wave speed.
    wave.setPeriod(speedParam.getValuef() * speedMult);

    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points){

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      // Get the relative position from the main tree in XZ.
      Vec3D posRelToMainTreeXZ = cubePos0To1.sub(theMainTreePos0To1);
      posRelToMainTreeXZ.y = 0.0f;

      // Get it's proportional distance as compared against
      // the max distance.
      float distFromMainTreeXZ =
        posRelToMainTreeXZ.magnitude();
      float distFromMainTreeXZPortion =
        distFromMainTreeXZ / distFromMainTreeXZMax;

      // Determine the hue to use.
      float val0To1 = distFromMainTreeXZPortion;
      float hue0To360 =
        (wave.getValuef() + waveSlope.getValuef() * val0To1 ) % 360;

      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = LX.hsb( hue0To360, 100, 100);
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class WavesToMainTree extends TSPattern

