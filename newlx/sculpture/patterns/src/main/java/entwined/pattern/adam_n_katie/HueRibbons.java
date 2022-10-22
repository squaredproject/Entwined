package entwined.pattern.adam_n_katie;

import entwined.utils.SimplexNoise;
import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

/**
Colorful undulating ribbons of color moving across the whole space.
*/
class HueRibbons extends AutographedPattern{
  // Constants
  static final float durationMaxS = 10.0f;
  static final float sizeMax = 0.5f;
  static final float rNoiseTimeOffset = 0.0f;
  static final float gNoiseTimeOffset = 37.3f;
  static final float bNoiseTimeOffset = 51.77f;
  static final float colorPower = 2.5f;

  // Parameters (to show up on UI).
  final BoundedParameter sizeParam =
    new BoundedParameter("SIZ", 80, 0, 100);
  final BoundedParameter durationParam =
    new BoundedParameter("DUR", 47, 0, 100);
  final BoundedParameter ribbonThresh =
    new BoundedParameter("TSH", 48, 0, 100);
  final BoundedParameter ribbonWidth =
    new BoundedParameter("WID", 27, 0, 100);
  final BoundedParameter colorsSizeParam =
    new BoundedParameter("CSZ", 48, 0, 100);

  // Variables
  private float elapsedNoiseTimeS = 0.0f;

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if
  // you're using Parameters or oscillatorss
  public HueRibbons(LX lx){
    super(lx);
    addParameter("size", sizeParam);
    addParameter("duration", durationParam);
    addParameter("ribbonThresh", ribbonThresh);
    addParameter("ribbonWidth", ribbonWidth);
    addParameter("colorSize", colorsSizeParam);

    // Reset the elapsed time.
    elapsedNoiseTimeS = 0.0f;
  }

  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().fader.getNormalized() == 0){return;}

    float duration0To1 = durationParam.getValuef() / 100.0f;
    float duration0ToMaxS = duration0To1 * durationMaxS;
    if(duration0ToMaxS <= 0.001f){duration0ToMaxS = 0.001f;}
    float noiseRate = 1.0f / duration0ToMaxS;

    // Update the elapsed time then use that to update the
    // noise time.
    // Note, we do it this way so jogging the parameter doesn't
    // cause us to jump through time (like a simple multiple would).
    elapsedNoiseTimeS += noiseRate * deltaTimeS;

    // Determine the feature size for the ribbons.
    float size0To1 = sizeParam.getValuef() / 100.0f;
    float size0ToMax = size0To1 * sizeMax;
    if(size0ToMax <= 0.001f){size0ToMax = 0.001f;}
    float noiseFrequency = 1.0f / size0ToMax;

    // Get the ribbon start and end vals.
    float ribbonThresh0To1 = ribbonThresh.getValuef() / 100.0f;
    float ribbonWidth0To1 = ribbonWidth.getValuef() / 100.0f;
    float ribbonMin = ribbonThresh0To1 - (ribbonWidth0To1 / 2.0f);
    if(ribbonMin < 0.0f){ribbonMin = 0.0f;}
    float ribbonMax = ribbonThresh0To1 + (ribbonWidth0To1 / 2.0f);
    if(ribbonMax > 1.0f){ribbonMax = 1.0f;}

    // Determine the feature size for the color blobs.
    float colorSize0To1 = colorsSizeParam.getValuef() / 100.0f;
    float colorSize0ToMax = colorSize0To1 * sizeMax;
    if(colorSize0ToMax <= 0.001f){colorSize0ToMax = 0.001f;}
    float colorNoiseFrequency = 1.0f / colorSize0ToMax;

    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points){

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      // Get the base noise sample position- we will move this
      // around to get three value samples.
      float noisePosX = cubePos0To1.x * noiseFrequency;
      float noisePosY = cubePos0To1.z * noiseFrequency;
      float noisePosZ = elapsedNoiseTimeS;

      // Get the space noise value to create the ribbon from.
      float noiseValNeg1To1 =
        (float)SimplexNoise.noise(noisePosX, noisePosY, noisePosZ);
      float noiseVal0To1 = 0.5f * (noiseValNeg1To1 + 1.0f);

      // Determine the ribbon brightness to use.
      float ribbonBrightness0To1 = 0.0f;
      if( (noiseVal0To1 >= ribbonMin) &&  (noiseVal0To1 <= ribbonMax)){
        float dist0To1 =
          Math.abs(noiseVal0To1 - ribbonThresh0To1) /
          (ribbonWidth0To1 / 2.0f);
        ribbonBrightness0To1 = 1.0f - dist0To1;
      }
      float ribbonBrightness0To100 = ribbonBrightness0To1 * 100.0f;

      // Get the x,y sample position for colors.
      float cNoisePosX = cubePos0To1.x * colorNoiseFrequency;
      float cNoisePosY = cubePos0To1.z * colorNoiseFrequency;

      // Get the noise val that controls red.
      float rNoisePosZ = elapsedNoiseTimeS + rNoiseTimeOffset;
      float rNoiseValNeg1To1 =
        (float)SimplexNoise.noise(cNoisePosX, cNoisePosY, rNoisePosZ);
      float rNoiseVal0To1 = 0.5f * (rNoiseValNeg1To1 + 1.0f);
      rNoiseVal0To1 = (float)Math.pow(rNoiseVal0To1, colorPower);

      // Get the noise val that controls green.
      float gNoisePosZ = elapsedNoiseTimeS + gNoiseTimeOffset;
      float gNoiseValNeg1To1 =
        (float)SimplexNoise.noise(cNoisePosX, cNoisePosY, gNoisePosZ);
      float gNoiseVal0To1 = 0.5f * (gNoiseValNeg1To1 + 1.0f);
      gNoiseVal0To1 = (float)Math.pow(gNoiseVal0To1, colorPower);

      // Get the noise val that controls blue.
      float bNoisePosZ = elapsedNoiseTimeS + bNoiseTimeOffset;
      float bNoiseValNeg1To1 =
        (float)SimplexNoise.noise(cNoisePosX, cNoisePosY, bNoisePosZ);
      float bNoiseVal0To1 = 0.5f * (bNoiseValNeg1To1 + 1.0f);
      bNoiseVal0To1 = (float)Math.pow(bNoiseVal0To1, colorPower);

      // Determine the color of this cube.
      int r0To255 = (int)(rNoiseVal0To1 * 255);
      int g0To255 = (int)(gNoiseVal0To1 * 255);
      int b0To255 = (int)(bNoiseVal0To1 * 255);
      int newColor = ColorRGB(r0To255, g0To255, b0To255);
      float hue0To360 = LXColor.h(newColor);

      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = LX.hsb(hue0To360, 100, ribbonBrightness0To100);
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class HueRibbons extends TSPattern
