package entwined.pattern.adam_n_katie;

import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

/**
A moving point source farling outward.
*/
public class MovingPoint extends AutographedPattern{
  // Constants
  static final float durationMaxS = 10.0f;
  static final float speedMaxPerS = 5.0f;
  static final float sizeMax = 2.0f;
  static final int paletteSize = 6;

  // Parameters (to show up on UI).
  final BoundedParameter durationParam = new BoundedParameter("Duration", 25, 0, 100);
  final BoundedParameter sizeParam = new BoundedParameter("Size", 60, 0, 100);
  final BoundedParameter speedParam = new BoundedParameter("Speed", 5, 0, 100);

  // Variables
  private float sourcePosX0To1 = 0.5f;
  private float sourcePosZ0To1 = 0.5f;
  private float sourceDirX0To1 = 0.5f;
  private float sourceDirZ0To1 = 0.5f;
  private ColorPalette palette;

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if you're using Parameters or oscilators
  public MovingPoint(LX lx){
    super(lx);
    addParameter("duration", durationParam);
    addParameter("size", sizeParam);
    addParameter("speed", speedParam);

    // Pick a new source location.
    sourcePosX0To1 = (float)Math.random();
    sourcePosZ0To1 = (float)Math.random();

    // Pick a new source dir.
    float sourceDirRads0ToTwoPi = (float)Math.random() * LX.TWO_PIf;
    sourceDirX0To1 = (float)Math.cos(sourceDirRads0ToTwoPi);
    sourceDirZ0To1 = (float)Math.sin(sourceDirRads0ToTwoPi);

    // Set up a random palette of colors.
    palette = new ColorPalette(paletteSize);
    palette.InitToRandom(
      0.0f,// hueMin0To360
      360.0f,// hueMax0To360
      80.0f,// satMin0To100
      100.0f,// satMax0To100
      100.0f,// briMin0To100
      100.0f);// briMax0To100
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    float duration0To1 = durationParam.getValuef() / 100.0f;
    float duration0ToMaxS = duration0To1 * durationMaxS;
    if(elapsedTimeS > duration0ToMaxS){
      // Pick a new source location.
      //sourcePosX0To1 = (float)Math.random();
      //sourcePosZ0To1 = (float)Math.random();

      // Pick a new source dir.
      float sourceDirRads0ToTwoPi = (float)Math.random() * LX.TWO_PIf;
      sourceDirX0To1 = (float)Math.cos(sourceDirRads0ToTwoPi);
      sourceDirZ0To1 = (float)Math.sin(sourceDirRads0ToTwoPi);

      // Pick a new palette.
      palette.InitToRandom(
        0.0f,// hueMin0To360
        360.0f,// hueMax0To360
        80.0f,// satMin0To100
        100.0f,// satMax0To100
        100.0f,// briMin0To100
        100.0f);// briMax0To100

      ResetElapsedTime();
    }

    // Update the size- if necessary.
    float size0To1 = sizeParam.getValuef() / 100.0f;
    float radius0To1 = size0To1 / 2.0f;
    float radius0ToMax = radius0To1 * sizeMax;

    // Update the position.
    float speed0To1 = speedParam.getValuef() / 100.0f;
    float speed0ToMaxPerS = speed0To1 * speedMaxPerS;
    sourcePosX0To1 += (sourceDirX0To1 * speed0ToMaxPerS * deltaTimeS);
    sourcePosZ0To1 += (sourceDirZ0To1 * speed0ToMaxPerS * deltaTimeS);

    if (sourcePosX0To1 < -radius0ToMax) sourcePosX0To1 = 1.0f+radius0ToMax;
    if (sourcePosZ0To1 < -radius0ToMax) sourcePosZ0To1 = 1.0f+radius0ToMax;
    if (sourcePosX0To1 > 1.0f+radius0ToMax) sourcePosX0To1 = 0.0f-radius0ToMax;
    if (sourcePosZ0To1 > 1.0f+radius0ToMax) sourcePosZ0To1 = 0.0f-radius0ToMax;


    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points){

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      float posRelToSourceX = cubePos0To1.x - sourcePosX0To1;
      float posRelToSourceZ = cubePos0To1.z - sourcePosZ0To1;

      float distFromSource =
        (float)Math.sqrt(
          (posRelToSourceX * posRelToSourceX) +
          (posRelToSourceZ * posRelToSourceZ));

      // Determine the value to use to drive the color selection.
      int colorArrIdx = cube.index;
      if(distFromSource <= radius0ToMax){
        float value0To1 = (1.0f - (distFromSource / radius0ToMax));

        // Get the color from the palette.
        int chosenPaletteIdx = (int)(value0To1 * paletteSize);
        if(chosenPaletteIdx == paletteSize){
          chosenPaletteIdx = paletteSize - 1;
        }
        ColorHSB colHSB = palette.hsbColors[chosenPaletteIdx];

        // Set the color of the cube.
        colors[colorArrIdx] = colHSB.GetPackedColor();//lx.hsb( 0, 0, brightness0To100);
      }else{
        colors[colorArrIdx] = black;
      }
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class MovingPoint extends TSPattern}
