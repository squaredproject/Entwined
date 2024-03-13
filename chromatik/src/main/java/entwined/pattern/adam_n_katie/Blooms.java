package entwined.pattern.adam_n_katie;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.ModelBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;

/**
Do blooms of lights jumping from sculpture to sculpture.
*/
public class Blooms extends AutographedPattern{
  static final float maxBloomRadiusProportionPerS = 60.0f;
  static final float maxBloomRadiusParamPower = 3.0f;

  enum PieceType {
    TREE,
    SHRUB,
    CIRCLE
  };

  // Parameters (to show up on UI).
  final CompoundParameter shrubBloomsPerTreeBloomParam =
    new CompoundParameter("SPT", 2.5 * shrubIdxCnt, 1, 10 * shrubIdxCnt);
  final CompoundParameter treeBloomRadiusSpeedParam =
    new CompoundParameter("TSP", 25, 1, 100);
  final CompoundParameter treeFadePercentagePerSParam  =
    new CompoundParameter("TFD", 25, 1, 100);
  final CompoundParameter shrubBloomRadiusSpeedParam =
    new CompoundParameter("SSP", 40, 1, 100);
  final CompoundParameter shrubFadePercentagePerSParam  =
    new CompoundParameter("SFD", 50, 1, 100);

  // Variables
  private PieceType desiredSculptureType = PieceType.TREE;
  private int desiredCurrTreeIdx = treeIdxMin;
  private int desiredCurrShrubIdx = shrubIdxMin;
  private int numShrubBlooms = 0;
  private float bloomPrevRadiusPortion = 0.0f;
  private float bloomCurrRadiusPortion = 0.0f;
  /*
  private int bloomLitColor =
    lx.hsb(135, 100, 100);// Random start color- will change!
  */
  private ColorHSB bloomLitColorHSB = new ColorHSB();

  private final ModelBuffer myBuffer = new ModelBuffer(lx, LXColor.BLACK);

  public Blooms(LX lx){
    super(lx);
    addParameter("shrubBloomsPerTree", shrubBloomsPerTreeBloomParam);
    addParameter("treeBloomRadius", treeBloomRadiusSpeedParam);
    addParameter("treeFadePercentage", treeFadePercentagePerSParam);
    addParameter("shrubBloomRadius", shrubBloomRadiusSpeedParam);
    addParameter("shrubFadePercentage", shrubFadePercentagePerSParam);
  }

  @Override
  public void run(double deltaMs){
    // Restore the previous frame content before updating pixels
    this.myBuffer.copyTo(getBuffer());

    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    // Read the parameter knobs.
    int shrubBloomsPerTreeBloom =
      (int)shrubBloomsPerTreeBloomParam.getValuef();
    float desiredCurrTreeBloomRadiusProportionPerS =
      maxBloomRadiusProportionPerS *
      (float)Math.pow(((float)treeBloomRadiusSpeedParam.getValue() / 100.0f), maxBloomRadiusParamPower);
    float treeFade0To1PerS =
      treeFadePercentagePerSParam.getValuef() / 100.0f;
    float desiredCurrShrubBloomRadiusProportionPerS =
      maxBloomRadiusProportionPerS *
      (float)Math.pow(((float)shrubBloomRadiusSpeedParam.getValue() / 100.0f), maxBloomRadiusParamPower);
    float shrubFade0To1PerS =
      shrubFadePercentagePerSParam.getValuef() / 100.0f;

    // Determine the amount of fading to do this frame.
    float treeFadeAmountThisFrame0To1 = deltaTimeS * treeFade0To1PerS;
    treeFadeAmountThisFrame0To1 =
      EntwinedUtils.constrain(treeFadeAmountThisFrame0To1, 0.0f, 1.0f);
    float shrubFadeAmountThisFrame0To1 = deltaTimeS * shrubFade0To1PerS;
    shrubFadeAmountThisFrame0To1 =
      EntwinedUtils.constrain(shrubFadeAmountThisFrame0To1, 0.0f, 1.0f);

    // Update the desired radius range to light up.
    bloomPrevRadiusPortion = bloomCurrRadiusPortion;
    if(desiredSculptureType == PieceType.TREE){
      bloomCurrRadiusPortion +=
        (desiredCurrTreeBloomRadiusProportionPerS * deltaTimeS);
    }else{
      bloomCurrRadiusPortion +=
        (desiredCurrShrubBloomRadiusProportionPerS * deltaTimeS);
    }

    // Iterate over all the cubes (shrub or not).
    int treeIdx = 0;
    int shrubIdx = 0;
    for (LXModel component : model.children) {
      boolean isTree = false;
      boolean isShrub = false;
      boolean isCircle = false;
      if (component.tags.contains("TREE")) {
        isTree = true;
        treeIdx++;
      } else if (component.tags.contains("SHRUB")) {
        isShrub = true;
        shrubIdx++;
      } else if (component.tags.contains("FAIRY_CIRCLE")) {
        isCircle = true;
      }
      for (LXPoint cube : component.points){
        // Set the fade rate based on type.
        float thisCubeFadeAmountThisFrame0To1 =
          (isTree?
            treeFadeAmountThisFrame0To1 :
            shrubFadeAmountThisFrame0To1);

        // Get the sculpture local position of this cube.
        float cubeAngRads0To2Pi = (LX.TWO_PIf / 360.f) * cube.theta;
        float cubeRadiusPortion = 0.0f;
        if (isTree){
          cubeRadiusPortion = TreeRadiusRawToRadiusPortion(cube.r);
        }else{
          cubeRadiusPortion = ShrubRadiusRawToRadiusPortion(cube.r);
        }

        // See if we should light up this cube (i.e. is it
        // on the active part of the active sculpture to bloom).
        // First, check if this cube is a shrub cube.
        boolean cubeIsOnDesiredSculpture = false;
        if (isTree){
          // Is this cube on the current sculpture component?
          cubeIsOnDesiredSculpture =
            (desiredSculptureType == PieceType.TREE) &&
            (treeIdx == desiredCurrTreeIdx);
        }
        else if (isShrub) {
          // Is this cube on the current sculpture component?
          cubeIsOnDesiredSculpture =
            (desiredSculptureType == PieceType.SHRUB) &&
            (shrubIdx == desiredCurrShrubIdx);
        }
        // TODO: we have a new type, fairyCircle....

        // Get the index of the color for this cube.
        int colorArrIdx = cube.index;

        // If this cube is on the correct sculpture and
        // at the the correct radius range then light it up.
        if( cubeIsOnDesiredSculpture &&
            (cubeRadiusPortion >= bloomPrevRadiusPortion) &&
            (cubeRadiusPortion <= bloomCurrRadiusPortion)){
          /*
          // Set the color of the cube.
          colors[colorArrIdx] = bloomLitColor;
          */
          hsbArray[colorArrIdx].hue0To360 = bloomLitColorHSB.hue0To360;
          hsbArray[colorArrIdx].sat0To100 = bloomLitColorHSB.sat0To100;
          hsbArray[colorArrIdx].bri0To100 = bloomLitColorHSB.bri0To100;

          int sparkColorToShow =
            hsbArray[colorArrIdx].GetPackedColor();

          // Set the color of the cube.
          colors[colorArrIdx] = sparkColorToShow;
        }else{
          /*
          // Fade the old color toward black.
          int oldColor = colors[colorArrIdx];
          int fadedOldColor =
            FadeColorTowardBlack(oldColor, thisCubeFadeAmountThisFrame0To1);

          // Set the color of the cube.
          colors[colorArrIdx] = fadedOldColor;
          */
          // Fade the old color toward black.
          hsbArray[colorArrIdx].FadeFullBrightToBlackThroughPureColor(thisCubeFadeAmountThisFrame0To1);

          int fadedColorToShow =
            hsbArray[colorArrIdx].GetPackedColor();

          // Set the color of the cube.
          colors[colorArrIdx] = fadedColorToShow;
        }
      }
    }// END for (LXMode component : model.children)

    // Update which sculpture we're interested in if appropriate.
    boolean shouldGoToDifferentSculpture =
      (bloomPrevRadiusPortion > 1.0f) &&
      (bloomCurrRadiusPortion > 1.0f);
    if(shouldGoToDifferentSculpture){

      if(desiredSculptureType == PieceType.TREE){
        // Jump immediately to doing shrubs!
        desiredSculptureType = PieceType.SHRUB;
        numShrubBlooms = 0;
      }

      if(desiredSculptureType == PieceType.SHRUB){
        if(numShrubBlooms >= shrubBloomsPerTreeBloom){
          // Jump immediately to doing a tree!
          // And Pick a different random tree!
          desiredSculptureType = PieceType.TREE;
          int oldTreeIdx = desiredCurrTreeIdx;
          do{
              desiredCurrTreeIdx =
                (int)(Math.random() * ((treeIdxMax - treeIdxMin) + 1)) + treeIdxMin;
          }while(desiredCurrTreeIdx == oldTreeIdx);

          // Reset the number of shrub blooms.
          numShrubBlooms = 0;
        }else{
          // Pick a different random shrub!
          int oldShrubIdx = desiredCurrShrubIdx;
          do{
              desiredCurrShrubIdx =
                (int)(Math.random() * ((shrubIdxMax - shrubIdxMin) + 1)) + shrubIdxMin;
          }while(desiredCurrShrubIdx == oldShrubIdx);

          // Increment the number of shrub blooms.
          ++numShrubBlooms;
        }
      }

      // Reset the bloom back to the center.
      bloomPrevRadiusPortion = 0.0f;
      bloomCurrRadiusPortion = 0.0f;

      // Update the color so that this bloom is different!
      float bloomLitColorHue0To1 = (float)Math.random();
      /*
      bloomLitColor = lx.hsb(bloomLitColorHue0To1 * 360.0f, 100, 100);
      */
      bloomLitColorHSB.hue0To360 = bloomLitColorHue0To1 * 360.0f;
      bloomLitColorHSB.sat0To100 = 0.0f;
      bloomLitColorHSB.bri0To100 = 100.0f;
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();

    // Keep a copy of our rendered state around
    this.myBuffer.copyFrom(getBuffer());

  }// END public void run(double deltaMs)
}// class Blooms extends TSPattern


