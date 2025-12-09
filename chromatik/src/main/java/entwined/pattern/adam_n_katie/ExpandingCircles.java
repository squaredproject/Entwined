package entwined.pattern.adam_n_katie;

import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

/**
Bright expanding circles starting at random locations.
*/
public class ExpandingCircles extends AutographedPattern{
  // Constants
  static final float durationMaxS = 30.0f;

  // Parameters (to show up on UI).
  final CompoundParameter durationParam =
    new CompoundParameter("Duration", 30, 0, 100);
  final CompoundParameter widthParam =
    new CompoundParameter("Width", 50, 0, 100);
  final CompoundParameter crestParam =
    new CompoundParameter("Crest", 3, 0, 100);
  final CompoundParameter fadeParam =
    new CompoundParameter("Fade", 8, 1, 100);

  // Variables.
  private float circleSourcePosX0To1 = 0.5f;
  private float circleSourcePosZ0To1 = 0.5f;
  private float circleSourceHue0To1 = 0.0f;

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if
  // you're using Parameters or oscillators
  public ExpandingCircles(LX lx){
    super(lx);
    addParameter("duration", durationParam);
    addParameter("width", widthParam);
    addParameter("crest", crestParam);
    addParameter("fade", fadeParam);

     // Pick an initial circle source location and color.
     circleSourcePosX0To1 = (float)Math.random();
     circleSourcePosZ0To1 = (float)Math.random();
     circleSourceHue0To1 = (float)Math.random();
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    // Do a thing at the given interval
    float duration0To1 = durationParam.getValuef() / 100.0f;
    float duration0ToMaxS = duration0To1 * durationMaxS;
    if(elapsedTimeS > duration0ToMaxS){
       // Pick a new source location.
       circleSourcePosX0To1 = (float)Math.random();
       circleSourcePosZ0To1 = (float)Math.random();
       circleSourceHue0To1 = (float)Math.random();

       ResetElapsedTime();
    }

    float width0To1 = widthParam.getValuef() / 100.0f;
    float radius0To1 = elapsedTimeS / duration0ToMaxS;
    float radius0ToMax =
      radius0To1 * (1.0f + width0To1) * (float)Math.sqrt(2.0f);
    float radiusOuter = radius0ToMax;
    float radiusInner = Math.max(radius0ToMax - width0To1, 0.0f);
    float crest0To1 = 1.0f - (crestParam.getValuef() / 100.0f);
    float fade1To100 = fadeParam.getValuef();

    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points){

      // Get the position on 0 to 1 of the cube.
      // XXX - local or global CSW
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      float posRelToCircleSourceX = cubePos0To1.x - circleSourcePosX0To1;
      float posRelToCircleSourceZ = cubePos0To1.z - circleSourcePosZ0To1;

      float distFromSourceXZ =
        (float)Math.sqrt(
          (posRelToCircleSourceX * posRelToCircleSourceX) +
          (posRelToCircleSourceZ * posRelToCircleSourceZ));

      // Determine the brightness to use.
      float brightness0To100  = 0.0f;
      float hue0To360 = circleSourceHue0To1 * 360.0f;
      float saturation0To100 = 0.0f;
      if( (distFromSourceXZ > radiusInner) &&
          (distFromSourceXZ <= radiusOuter)){

        float positionInBand0To1 =
          (distFromSourceXZ - radiusInner) / (radiusOuter - radiusInner);

        // Make it so if we're in the crest area the color is white.
        // If we are not in the crest area we fade from white, through
        // the color, to black.
        float sat0To1 = 0.0f;
        float bright0To1 = 1.0f;
        if(positionInBand0To1 > crest0To1){
          sat0To1 = 0.0f;
          bright0To1 = 1.0f;
        }else{
          float positonpRelCrest0To1 =
            1.0f - ((crest0To1 - positionInBand0To1) / crest0To1);
          sat0To1 = 1.0f - positonpRelCrest0To1;
          sat0To1 = (float)Math.pow(sat0To1, 1.0f/fade1To100);
          bright0To1 = positonpRelCrest0To1;
        }

        saturation0To100 = sat0To1 * 100.0f;
        brightness0To100 = bright0To1 * 100.0f;
      }

      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] =
        LX.hsb(hue0To360, saturation0To100, brightness0To100);
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class ExpandingCircles extends TSPattern

