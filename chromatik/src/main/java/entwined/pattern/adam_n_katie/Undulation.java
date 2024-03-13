package entwined.pattern.adam_n_katie;

import entwined.utils.SimplexNoise;
import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

/**
Undulating cloud-like patterns.
*/
public class Undulation extends AutographedPattern{
  // Constants
  static final float durationMaxS = 10.0f;
  static final float sizeMax = 0.5f;
  static final float rNoiseTimeOffset = 0.0f;
  static final float gNoiseTimeOffset = 37.3f;
  static final float bNoiseTimeOffset = 51.77f;
  static final float colorPower = 2.5f;


  // Parameters (to show up on UI).
  final CompoundParameter sizeParam =
    new CompoundParameter("size", 50, 0, 100);
  final CompoundParameter durationParam =
    new CompoundParameter("duration", 25, 0, 100);

  // Variables
  private float elapsedNoiseTimeS = 0.0f;

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if
  // you're using Parameters or oscillators
  public Undulation(LX lx){
    super(lx);
    addParameter("size", sizeParam);
    addParameter("duration", durationParam);

    // Reset the elapsed time.
    elapsedNoiseTimeS = 0.0f;
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
    if(duration0ToMaxS <= 0.001f){duration0ToMaxS = 0.001f;}
    float noiseRate = 1.0f / duration0ToMaxS;

    // Update the elapsed time then use that to update the
    // noise time.
    // Note, we do it this way so jogging the parameter doesn't
    // cause us to jump through time (like a simple multiple would).
    elapsedNoiseTimeS += noiseRate * deltaTimeS;

    float size0To1 = sizeParam.getValuef() / 100.0f;
    float size0ToMax = size0To1 * sizeMax;
    if(size0ToMax <= 0.001f){size0ToMax = 0.001f;}
    float noiseFrequency = 1.0f / size0ToMax;

    // Use a for loop here to set the cube colors
    for (LXPoint cube : model.points){

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      // Get the base noise sample position- we will move this
      // around to get three value samples.
      float noisePosX = cubePos0To1.x * noiseFrequency;
      float noisePosY = cubePos0To1.z * noiseFrequency;
      float noisePosZ = elapsedNoiseTimeS;

      // Get the noise val that controls red.
      float rNoisePosZ = elapsedNoiseTimeS + rNoiseTimeOffset;
      float rNoiseValNeg1To1 =
        (float)SimplexNoise.noise(noisePosX, noisePosY, rNoisePosZ);
      float rNoiseVal0To1 = 0.5f * (rNoiseValNeg1To1 + 1.0f);
      rNoiseVal0To1 = (float)Math.pow(rNoiseVal0To1, colorPower);

      // Get the noise val that controls green.
      float gNoisePosZ = elapsedNoiseTimeS + gNoiseTimeOffset;
      float gNoiseValNeg1To1 =
        (float)SimplexNoise.noise(noisePosX, noisePosY, gNoisePosZ);
      float gNoiseVal0To1 = 0.5f * (gNoiseValNeg1To1 + 1.0f);
      gNoiseVal0To1 = (float)Math.pow(gNoiseVal0To1, colorPower);

      // Get the noise val that controls blue.
      float bNoisePosZ = elapsedNoiseTimeS + bNoiseTimeOffset;
      float bNoiseValNeg1To1 =
        (float)SimplexNoise.noise(noisePosX, noisePosY, bNoisePosZ);
      float bNoiseVal0To1 = 0.5f * (bNoiseValNeg1To1 + 1.0f);
      bNoiseVal0To1 = (float)Math.pow(bNoiseVal0To1, colorPower);

      // Determine the color of this cube.
      int r0To255 = (int)(rNoiseVal0To1 * 255);
      int g0To255 = (int)(gNoiseVal0To1 * 255);
      int b0To255 = (int)(bNoiseVal0To1 * 255);
      int newColor = ColorRGB(r0To255, g0To255, b0To255);

      // Set the color of this cube.
      // The color of the cube is NOT stored in the cube data structure- instead
      // the color of each cube is stored over in the "colors" array.
      // The way we know which element of that array represents the particular cube
      // is via cube.index.
      // So, assign the color!
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = newColor;
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class Undulation extends TSPattern


