package entwined.pattern.adam_n_katie;

import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;

/**
Circular hue cycle waves starting far from the main tree and moving toward it.
*/

// NOTE! This pattern depends on there being a tree at position 0
// which is not true with entwined.
// It has thus been removed from the main pattern list until repaired.

public class WavesToMainTree extends AutographedPattern{
  // Constants
  static final float speedMult = 1000;

  // Parameters (to show up on UI).
  final BoundedParameter speedParam =
    new BoundedParameter("Speed", 5, 20, .01);
  final BoundedParameter waveSlope =
    new BoundedParameter("waveSlope", 360, 1, 720);
  final SawLFO wave =
    new SawLFO(0, 360, speedParam.getValuef() * speedMult);

  // Variables
  private LXModel theMainTree;
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
    theMainTree = model.sub("TREE").get(0);// Trees have no Y position.
    theMainTreePos0To1 =
      PosRawToPos0To1(theMainTree.cx, 0.0f, theMainTree.cz);

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

