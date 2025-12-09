package entwined.pattern.adam_n_katie;

import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

//----------------------------------------------------------------------------
/**
Radiating arms that can be spiralled.
*/


public class SpiralArms extends AutographedPattern{
  // Constants
  static final float maxNumArms = 20;
  static final float maxNumTwists = 3;
  static final float maxSpinsPerS = 2;

  // Parameters (to show up on UI).
  final CompoundParameter numArmsParam =
    new CompoundParameter("Num", 15, 1, 100);
  final CompoundParameter armThicknessParam =
    new CompoundParameter("THC", 30, 1, 100);
  final CompoundParameter twistParam =
    new CompoundParameter("TWS", 10, 0, 100);
  final CompoundParameter rotSpeedParam =
    new CompoundParameter("SPD", 3, 1, 100);

  // Variables
  private Vec3D theMainTreePos0To1;
  private float distFromMainTreeXZMax = -Float.MAX_VALUE;

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if you're using
  //  Parameters or oscillators
  public SpiralArms(LX lx){
    super(lx);
    addParameter("numArms", numArmsParam);
    addParameter("armThickness", armThicknessParam);
    addParameter("twist", twistParam);
    addParameter("rotSpeed", rotSpeedParam);

    // Get the Main tree's position.
    float centerElement_x = 0;
    float centerElement_z = 0;
    try {
      LXModel centerElement = model.sub("CENTER").get(0);// Trees have no Y position.
      if (centerElement != null) {
        centerElement_x = centerElement.cx;
        centerElement_z = centerElement.cz;
      }
    } catch (Exception e) {
      System.out.println(" SpiralArms: no center object, using 0,0 of sculpture");
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

     // Read the parameter knobs.
    float numArms0To1Desired =
      (float)numArmsParam.getValue() / 100.0f;
    float armThickness0To1Desired =
      (float)armThicknessParam.getValue() / 100.0f;
    float twist0To1Desired=
      (float)twistParam.getValue() / 100.0f;
    float rotSpeed0To1Desired =
      rotSpeedParam.getValuef() / 100.0f;

    // Get the parameter values into the ranges we want.
    float numArms = Math.max(1,(int)(numArms0To1Desired * maxNumArms));
    float twistRadsPerUnit = twist0To1Desired * maxNumTwists * LX.TWO_PIf;
    float rotRadsPerS = rotSpeed0To1Desired * maxSpinsPerS * LX.TWO_PIf;

    // Derived params from the above.
    float angStep0ToPi = LX.TWO_PIf / (1 + numArms);

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

      // Get the base angle and distance.
      float baseR = distFromMainTreeXZPortion;

      // Get the angle to use.
      float baseAngXZRads =
        (float)Math.atan2(posRelToMainTreeXZ.z, posRelToMainTreeXZ.x);
      float currAngRads =
        baseAngXZRads +
        (baseR * twistRadsPerUnit) +
        (elapsedTimeS * rotRadsPerS);

      float ang0To2Pi = currAngRads % LX.TWO_PIf;
      // boolean onAnArm = (((int)(ang0To2Pi / angStep0ToPi)) % 2 == 0);
      float armArea0To1 = (ang0To2Pi % angStep0ToPi) / angStep0ToPi;
      boolean inFilledPartOfArm = (armArea0To1 < armThickness0To1Desired);
      float brightness0to1 = inFilledPartOfArm?1.0f:0.0f;
      float hue0To1 = armArea0To1 / armThickness0To1Desired;

      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = LX.hsb(hue0To1 * 360, 100, brightness0to1 * 100);
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class SpiralArms extends TSPattern


