package entwined.pattern.adam_n_katie;

/*
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.lang.Math;

import heronarts.lx.LX;
import heronarts.lx.utils.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;

import entwined.utils.Vec2D;
import entwined.utils.Vec3D;
import entwined.utils.PerlinNoise;
import entwined.utils.SimplexNoise;
import entwined.utils.EntwinedUtils;
*/

//----------------------------------------------------------------------------
// Inspirations!
//
// SolidColor
// Candy Cloud
// SeeSwaw
// Pixels
// Pond
// Planes
// BeachBall
//----------------------------------------------------------------------------



//----------------------------------------------------------------------------

/**
Since this is an AutographedPattern it should make one magic cube twinkle...
*/
/*
class BasicMagic extends AutographedPattern{
  // Constructor and inital setup
  BasicMagic(LX lx){
    super(lx);
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END public void run(double deltaMs)
}// END class AdamsSimplest extends AutographedPattern
*/

//----------------------------------------------------------------------------
/**
Undulatings bands of light.
*/
/*
class UndulatingBands extends AutographedPattern{
  // Constants
  static final float durationMaxS = 10.0f;
  static final float sizeMax = 0.5f;

  // Parameters (to show up on UI).
  final BoundedParameter sizeParam = new BoundedParameter("size", 75, 0, 100);
  final BoundedParameter durationParam = new BoundedParameter("duration", 33, 0, 100);
  final BoundedParameter ribbonThresh = new BoundedParameter("ribbonThresh", 33, 0, 100);
  final BoundedParameter ribbonWidth = new BoundedParameter("ribbonWidth", 15, 0, 100);

  // Variables
  private float elapsedNoiseTimeS = 0.0f;

    // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or oscilators
  UndulatingBands(LX lx){
    super(lx);
    addParameter(sizeParam);
    addParameter(durationParam);
    addParameter(ribbonThresh);
    addParameter(ribbonWidth);

    // Reset the elapsed noise time.
    elapsedNoiseTimeS = 0.0f;
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);

    // Allow for the overall effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}

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

    // Get the ribbon start and end vals.
    float ribbonThresh0To1 = ribbonThresh.getValuef() / 100.0f;
    float ribbonWidth0To1 = ribbonWidth.getValuef() / 100.0f;
    float ribbonMin = ribbonThresh0To1 - (ribbonWidth0To1 / 2.0f);
    if(ribbonMin < 0.0f){ribbonMin = 0.0f;}
    float ribbonMax = ribbonThresh0To1 + (ribbonWidth0To1 / 2.0f);
    if(ribbonMax > 1.0f){ribbonMax = 1.0f;}

    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes){

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);

      float noisePosX = cubePos0To1.x * noiseFrequency;
      float noisePosY = cubePos0To1.z * noiseFrequency;
      float noisePosZ = elapsedNoiseTimeS;
      float noiseValNeg1To1 =
        (float)SimplexNoise.noise(noisePosX, noisePosY, noisePosZ);
      float noiseVal0To1 = 0.5f * (noiseValNeg1To1 + 1.0f);

      // Determine the ribbon brightness to use.
      float brightness0To1 = 0.0f;
      if( (noiseVal0To1 >= ribbonMin) &&  (noiseVal0To1 <= ribbonMax)){
        float dist0To1 =
          Math.abs(noiseVal0To1 - ribbonThresh0To1) /
          (ribbonWidth0To1 / 2.0f);
        brightness0To1 = 1.0f - dist0To1;
      }
      float brightness0To100 = brightness0To1 * 100.0f;

      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = lx.hsb( 0, 0, brightness0To100);
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class UndulatingBands extends TSPattern
*/

//----------------------------------------------------------------------------


//----------------------------------------------------------------------------

