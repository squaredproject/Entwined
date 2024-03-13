package entwined.pattern.adam_n_katie;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

//----------------------------------------------------------------------------
/**
Little sparks popping on the cubes.
*/
public class Sparks extends AutographedPattern{
  // Constants
  static final float maxSparFadePerS = 30.0f;


  // Parameters (to show up on UI).
  final CompoundParameter sparkProbabilityPercentagePerSParam =
    new CompoundParameter("SPS", 3, 1, 100);
  final CompoundParameter sparkFadePercentagePerSParam  =
    new CompoundParameter("SFD", 25, 1, 100);

  public Sparks(LX lx){
    super(lx);
    addParameter("sparkProbability", sparkProbabilityPercentagePerSParam);
    addParameter("sparkFade", sparkFadePercentagePerSParam);
  }

  @Override
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    // Read the parameter knobs.
    float sparkProbability0To1PerS =
      (float)sparkProbabilityPercentagePerSParam.getValue() / 100.0f;
    float sparkFade0To1PerS =
      sparkFadePercentagePerSParam.getValuef() / 100.0f;
    sparkFade0To1PerS *= maxSparFadePerS;

    // Determine the amount of fading to do this frame.
    float sparkProbability0To1ThisFrame0To1 = deltaTimeS * sparkProbability0To1PerS;
    sparkProbability0To1ThisFrame0To1 =
      EntwinedUtils.constrain(sparkProbability0To1ThisFrame0To1, 0.0f, 1.0f);
    float sparkFadeAmountThisFrame0To1 = deltaTimeS * sparkFade0To1PerS;
    sparkFadeAmountThisFrame0To1 =
      EntwinedUtils.constrain(sparkFadeAmountThisFrame0To1, 0.0f, 1.0f);

    // Iterate over all the cubes (shrub or not).
    for (LXPoint cube : model.points){
      // Get the index of the color for this cube.
      int colorArrIdx = cube.index;

      // Determine if this cube should light up this frame.
      float sparkVal0To1ForThisCubeThisFrame =
        (float)Math.random();
      if( sparkVal0To1ForThisCubeThisFrame <=
          sparkProbability0To1ThisFrame0To1){
        // Pick a color for this spark.
        float sparkColorHue0To1 = (float)Math.random();

        hsbArray[colorArrIdx].hue0To360 = sparkColorHue0To1 * 360.0f;
        hsbArray[colorArrIdx].sat0To100 = 0.0f;
        hsbArray[colorArrIdx].bri0To100 = 100.0f;
        int sparkColorToShow =
          hsbArray[colorArrIdx].GetPackedColor();

        // Set the color of the cube.
        colors[colorArrIdx] = sparkColorToShow;
      }else{
        // Fade the old color toward black.
        hsbArray[colorArrIdx].FadeFullBrightToBlackThroughPureColor(sparkFadeAmountThisFrame0To1);

        int fadedColorToShow =
          hsbArray[colorArrIdx].GetPackedColor();

        // Set the color of the cube.
        colors[colorArrIdx] = fadedColorToShow;
      }
    }// END for (BaseCube cube : model.baseCubes)

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();

  }// END public void run(double deltaMs)
}// class Sparks extends TSPattern


