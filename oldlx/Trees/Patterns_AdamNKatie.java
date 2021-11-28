import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.lang.Math;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import org.apache.commons.lang3.ArrayUtils;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;

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
// As the base class for other patterns we won't actually register this one
// explicitly.
class AutographedPattern extends TSPattern {
  // Constants
  
  // Tree constants.
  static final int treeIdxMin = 0;
  static final int treeIdxMax = 2;
  static final int treeIdxCnt = (treeIdxMax - treeIdxMin) + 1;

  // Shrub Constants
  // ShrubIdx (index of the sculpture)
  //    [0,19]
  // ClusterIdx (index of the cluster -spokes of shrubs, branches of trees-
  //   on the sculpture).
  //    [0,11]
  // RodIdx (index on a cluster- basically radius from base of spoke)
  //    [1,5]
  static final int shrubIdxMin = 0;
  static final int shrubIdxMax = 19;
  static final int shrubIdxCnt =
    (shrubIdxMax - shrubIdxMin) + 1;
  static final int shrubSpokeIdxMin = 0; 
  static final int shrubSpokeIdxMax = 11;
  static final int shrubSpokeIdxCnt =
    (shrubSpokeIdxMax - shrubSpokeIdxMin) + 1;
  static final int shrubRadialIdxMin = 1;// Why?!?!?
  static final int shrubRadialIdxMax = 5;
  static final int shrubRadialIdxCnt =
    (shrubRadialIdxMax - shrubRadialIdxMin) + 1;

  // Other constants.
  static final int black = LXColor.hsb(0, 100, 0);
  static final float autographCubeSpeedMult = 1000;

  // Parameters (to show up on UI).
  final BasicParameter autographCubeSpeedParam =
    new BasicParameter("SPD", 1.01, 20, 0.01);
  final BasicParameter autographCubeWaveSlopeParam =
    new BasicParameter("SLP", 1, 1, 720);
  final SawLFO autographCubeWave =
    new SawLFO(0, 360, autographCubeSpeedParam.getValuef() * autographCubeSpeedMult);
  
  // Types
  public static class ColorHSB {
    public float hue0To360;
    public float sat0To100;
    public float bri0To100;
    public void InitToRandom(
      float hueMin0To360,
      float hueMax0To360,
      float satMin0To100,
      float satMax0To100,
      float briMin0To100,
      float briMax0To100){
      hue0To360 = hueMin0To360 + ((float)Math.random() * (hueMax0To360 - hueMin0To360));
      sat0To100 = satMin0To100 + ((float)Math.random() * (satMax0To100 - satMin0To100));
      bri0To100 = briMin0To100 + ((float)Math.random() * (briMax0To100 - briMax0To100));
    }
    public void SetAsPackedColot(int col){
        hue0To360 = LXColor.h(col); 
        sat0To100 = LXColor.s(col); 
        bri0To100 = LXColor.b(col); 
    }
    public int GetPackedColor(){
      int packedColor = LXColor.hsb(hue0To360, sat0To100, bri0To100); 
      return packedColor;
    }
    public static ColorHSB Lerp(
      ColorHSB A,
      ColorHSB B,
      float t0To1){

      ColorHSB out = new ColorHSB();

      // Interpolate the hue in the shortest direction!
      float signedHueDeltaDegs =
        B.hue0To360 - A.hue0To360;
      if(signedHueDeltaDegs > 180.0f){
        signedHueDeltaDegs -= 360.0f;
      }else if(signedHueDeltaDegs < -180.0f){
        signedHueDeltaDegs += 360.0f;
      }
      out.hue0To360 = A.hue0To360 + (t0To1 * signedHueDeltaDegs);

      // Interpolate the sat and bri simply.
      out.sat0To100 = Utils.lerp(A.sat0To100, B.sat0To100, t0To1);
      out.bri0To100 = Utils.lerp(A.bri0To100, B.bri0To100, t0To1);

      return out;
    }
    public void FadeFullBrightToBlackThroughPureColor(float fadeAmount0To1){
      // Increasing overal saturation actually makes a color darken.
      sat0To100 = sat0To100 + (fadeAmount0To1 * (100.0f - sat0To100));
      
      // Once the saturation has been fully increased one may start
      // to decrase the brightness.
      if(sat0To100 >= 99.0f){
        bri0To100 = bri0To100 - (fadeAmount0To1 * bri0To100);
      }
    }
  }
  public static class ColorPalette {
    public ColorHSB[] hsbColors;
    ColorPalette(int size){
      // Init the HSB array.
      hsbColors = new ColorHSB[size];
      for(int i = 0; i < size; ++i){
        hsbColors[i] = new ColorHSB();
        hsbColors[i].hue0To360 = 0.0f;
        hsbColors[i].sat0To100 = 0.0f;
        hsbColors[i].bri0To100 = 0.0f;
      }
    }
    public void InitToRandom(
      float hueMin0To360,
      float hueMax0To360,
      float satMin0To100,
      float satMax0To100,
      float briMin0To100,
      float briMax0To100){
      for(int i = 0; i < hsbColors.length; ++i){
        hsbColors[i].InitToRandom(
          hueMin0To360,
          hueMax0To360,
          satMin0To100,
          satMax0To100,
          briMin0To100,
          briMax0To100);
      }
    }
  }  
  
  // Variables (set once on construct)
  ColorHSB[] hsbArray;
  static float treeRadiusMinRaw =
    Float.MAX_VALUE;// Initial sentinel value- will be updated below.
  static float treeRadiusMaxRaw =
    Float.MIN_VALUE;// Initial sentinel value- will be updated below.
  static float treeRadiusSpanRaw =
    0.0f;// Initial sentinel value- will be updated below.
  static float shrubRadiusMinRaw =
    Float.MAX_VALUE;// Initial sentinel value- will be updated below.
  static float shrubRadiusMaxRaw =
    Float.MIN_VALUE;// Initial sentinel value- will be updated below.
  static float shrubRadiusSpanRaw =
    0.0f;// Initial sentinel value- will be updated below.
  static Vec3D cubesBoundsMinRaw =
    new Vec3D(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
  static Vec3D cubesBoundsMaxRaw =
    new Vec3D(Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE);
  static float cubesBoundsMaxAxisSpanRaw = 0.0f;
  static int theOneAutographCubeColorArrIdx = 0;
  
  // Variables
  protected float deltaTimeS = 0.0f;
  protected float elapsedTimeS = 0.0f;

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters
  // or oscilators
  AutographedPattern(LX lx){
    super(lx);
    addModulator(autographCubeWave).start();
    //addParameter(autographCubeWaveSlopeParam);
    //addParameter(autographCubeSpeedParam);
    
    // Init the HSB array.
    hsbArray = new ColorHSB[colors.length];
    for(int i = 0; i < colors.length; ++i){
      hsbArray[i] = new ColorHSB();
      hsbArray[i].hue0To360 = 0.0f;
      hsbArray[i].sat0To100 = 0.0f;
      hsbArray[i].bri0To100 = 0.0f;
    }

    // Init the time variables.
    deltaTimeS = 0.0f;
    elapsedTimeS = 0.0f;

    // Make it easy to convert into 0To1 space.
    FindCubesBoundsRawAndMaxSpanRaw();
    
    // Find shrub cube min and max radius from shrub center in the 
    // global 0To1 space.
    FindShrubRadiusMinMaxRawAndSpanRaw();
    
    // Get the index of the magic cube.
    theOneAutographCubeColorArrIdx =
      FindTheDesiredAutographCubeColorArrIdx();
  }

  //--------------------------------------------------------------------------
  // Helper functions to convert from raw coordinates into 0To1 space.
  protected void FindCubesBoundsRawAndMaxSpanRaw(){
    // Get the axis-aligned bounds for the piece relative to the
    // central tree).
    for (Cube cube : model.cubes){
      if (cube.x < cubesBoundsMinRaw.x){cubesBoundsMinRaw.x = cube.x;}
      if (cube.x > cubesBoundsMaxRaw.x){cubesBoundsMaxRaw.x = cube.x;}
      if (cube.y < cubesBoundsMinRaw.y){cubesBoundsMinRaw.y = cube.y;}
      if (cube.y > cubesBoundsMaxRaw.y){cubesBoundsMaxRaw.y = cube.y;}
      if (cube.z < cubesBoundsMinRaw.z){cubesBoundsMinRaw.z = cube.z;}
      if (cube.z > cubesBoundsMaxRaw.z){cubesBoundsMaxRaw.z = cube.z;}
    }
    
    // Find out which axis is biggest.
    // This is used when converting from raw coordinates into 0To1 space.
    Vec3D cubesBoundsSpanRaw =
      new Vec3D((cubesBoundsMaxRaw.x - cubesBoundsMinRaw.x),
                (cubesBoundsMaxRaw.y - cubesBoundsMinRaw.y),
                (cubesBoundsMaxRaw.z - cubesBoundsMinRaw.z));
    cubesBoundsMaxAxisSpanRaw =
      Float.max(cubesBoundsSpanRaw.x,
        Float.max(cubesBoundsSpanRaw.x, cubesBoundsSpanRaw.z)); 
  }
  protected Vec3D PosRawToPos0To1(
    float rawCoordX,
    float rawCoordY,
    float rawCoordZ){
     return new Vec3D(
       ((rawCoordX - cubesBoundsMinRaw.x) / cubesBoundsMaxAxisSpanRaw),
       ((rawCoordY - cubesBoundsMinRaw.y) / cubesBoundsMaxAxisSpanRaw),
       ((rawCoordZ - cubesBoundsMinRaw.z) / cubesBoundsMaxAxisSpanRaw));
  }
  
  //--------------------------------------------------------------------------
  // Helper functions to convert shrub radius raw to radius portion.
  protected void FindShrubRadiusMinMaxRawAndSpanRaw(){
    // Iterate over all the cubes (shrub or not).
    for (BaseCube cube : model.baseCubes){
      // Get the local radius of this cube in raw coordinates and
      // convert it into the global 0To1 space.
      float radiusLocalRaw = cube.r;
        
      // Check if this cube is a shrub cube.
      if (cube.pieceType == PieceType.TREE){
        // Update the tree min and max.
        if(radiusLocalRaw < treeRadiusMinRaw){
          treeRadiusMinRaw = radiusLocalRaw;
        }
        if(radiusLocalRaw > treeRadiusMaxRaw){
          treeRadiusMaxRaw = radiusLocalRaw;
        }
      }else{
        // Update the shrub min and max.
        if(radiusLocalRaw < shrubRadiusMinRaw){
          shrubRadiusMinRaw = radiusLocalRaw;
        }
        if(radiusLocalRaw > shrubRadiusMaxRaw){
          shrubRadiusMaxRaw = radiusLocalRaw;
        }
      }
    }// END for (BaseCube cube : model.baseCubes) 
    
    // Calculate the span to make turning raw radius values into
    // portions easier.
    treeRadiusSpanRaw = treeRadiusMaxRaw - treeRadiusMinRaw;
    shrubRadiusSpanRaw = shrubRadiusMaxRaw - shrubRadiusMinRaw;
  }
  protected float TreeRadiusRawToRadiusPortion(
    float treeRadiusRaw){
     return ((treeRadiusRaw - treeRadiusMinRaw) / treeRadiusSpanRaw);
  }
  protected float ShrubRadiusRawToRadiusPortion(
    float shrubRadiusRaw){
     return ((shrubRadiusRaw - shrubRadiusMinRaw) / shrubRadiusSpanRaw);
  }
  
  //--------------------------------------------------------------------------
  // Helper functions for colors.
  static int ColorRGB(
    int r0To255,
    int g0To255,
    int b0To255){
    int newColor =
      (255 << 24) | // Alpha value.
      (r0To255 << 16) |
      (g0To255 << 8) |
      (b0To255 << 0);
    return newColor;
  }
  static int FadeColorTowardBlack(int oldColor, float fadeAmountThisFrame0To1){
      float oldColorH0To360 = LXColor.h(oldColor);
      float oldColorS0To100 = LXColor.s(oldColor);
      float oldColorB0To100 = LXColor.b(oldColor);
      
      float fadColorS0To100 =
        Utils.lerp(oldColorS0To100, 100.0f, fadeAmountThisFrame0To1);
      float fadColorB0To100 =
        Utils.lerp(oldColorB0To100, 0.0f, fadeAmountThisFrame0To1);
        
      //int fadedOldColor =
      //  LXColor.lerp(oldColor, lx.hsb(0, 0, 0), sparkFadeAmountThisFrame0To1);
      int fadedOldColor =
        LXColor.hsb(oldColorH0To360, fadColorS0To100, fadColorB0To100);
      return fadedOldColor;
  }

  //--------------------------------------------------------------------------
  // Helper functions to update the elapsed time.
  protected void UpdateElapsedTimeS(
    double deltaMs){
    deltaTimeS = (float)(deltaMs / 1000.0);
    elapsedTimeS += deltaTimeS; 
  }
  protected void ResetElapsedTime(){
    elapsedTimeS = 0.0f;    
  }
  
  //--------------------------------------------------------------------------
  // Helper fuctnions for the autographCube.
  private int desiredAutographCubeShrubIdx = 19;
  private int desiredAutographCubeShrubSpokeIdx = 0;
  private int desiredAutographCubeShrubRadialIdx = 5;
  protected int FindTheDesiredAutographCubeColorArrIdx(){
    //return 127;
    // Iterate over all the cubes (shrub or not).
    int desiredCubeColorArrIdx = 0;
    for (BaseCube cube : model.baseCubes){
      // Check if this cube is a shrub cube.
      if (cube.pieceType == PieceType.SHRUB){
        // Get the shrub cube, and access shrub specific properties.
        ShrubCube shrubCube = (ShrubCube) cube;
        int shrubIdx = shrubCube.config.shrubIndex;
        int shrubSpokeIdx = shrubCube.config.clusterIndex;
        int shrubRadialIdx = shrubCube.config.rodIndex;
        if( (shrubIdx == desiredAutographCubeShrubIdx) &&
            (shrubSpokeIdx == desiredAutographCubeShrubSpokeIdx) &&
            (shrubRadialIdx == desiredAutographCubeShrubRadialIdx)){
          desiredCubeColorArrIdx = cube.index; 
        }
      }
    }// END for (BaseCube cube : model.baseCubes)
    
    return desiredCubeColorArrIdx;
  }
  protected void UpdateAndSetColorOfTheOneAutographCube(){
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}    
    
    autographCubeWave.setPeriod(
      autographCubeSpeedParam.getValuef() * autographCubeSpeedMult);
    
    // Determine the hue to use.
    float hue0To360 =
      (float)( (autographCubeWave.getValuef() + autographCubeWaveSlopeParam.getValuef() ) % 360);
    
    // Set the color of tree to the cube.
    colors[theOneAutographCubeColorArrIdx] = lx.hsb( hue0To360, 100, 100);
  }
  
  //--------------------------------------------------------------------------
  // Main fun function.
  public void run(double deltaMs){
    UpdateElapsedTimeS(deltaMs);
  }
}


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
Bright expanding circles starting at random locations.
*/
class ExpandingCircles extends AutographedPattern{
  // Constants
  static final float durationMaxS = 30.0f;
  
  // Parameters (to show up on UI).
  final BasicParameter durationParam =
    new BasicParameter("Duration", 30, 0, 100);
  final BasicParameter widthParam =
    new BasicParameter("Width", 50, 0, 100);
  final BasicParameter crestParam =
    new BasicParameter("Crest", 3, 0, 100);
  final BasicParameter fadeParam =
    new BasicParameter("Fade", 8, 1, 100);
  
  // Varaibles.
  private float circleSourcePosX0To1 = 0.5f;
  private float circleSourcePosZ0To1 = 0.5f;
  private float circleSourceHue0To1 = 0.0f;

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if
  // you're using Parameters or oscilators
  ExpandingCircles(LX lx){
    super(lx);
    addParameter(durationParam);
    addParameter(widthParam);
    addParameter(crestParam);
    addParameter(fadeParam);
    
     // Pick an intial circle source location and color.
     circleSourcePosX0To1 = (float)Math.random();
     circleSourcePosZ0To1 = (float)Math.random();
     circleSourceHue0To1 = (float)Math.random(); 
  }
  
  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}

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
    for (BaseCube cube : model.baseCubes){
      
      // Get the position on 0 to 1 of the cube.
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
        lx.hsb(hue0To360, saturation0To100, brightness0To100);
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class ExpandingCircles extends TSPattern


//----------------------------------------------------------------------------
/**
Radiating arms that can be spiralled.
*/

// NOTE! This pattern depends on there being a tree at position 0
// which is not true with entwined.
// It has thus been removed from the main pattern list until repaired.


class SpiralArms extends AutographedPattern{
  // Constants
  static final float maxNumArms = 20;
  static final float maxNumTwists = 3;
  static final float maxSpinsPerS = 2;
  
  // Parameters (to show up on UI).
  final BasicParameter numArmsParam = 
    new BasicParameter("Num", 15, 1, 100);
  final BasicParameter armThicknessParam =
    new BasicParameter("THC", 30, 1, 100);
  final BasicParameter twistParam =
    new BasicParameter("TWS", 10, 0, 100);
  final BasicParameter rotSpeedParam =
    new BasicParameter("SPD", 3, 1, 100);

  // Variables
  private Tree theMainTree;
  private Vec3D theMainTreePos0To1;
  private float distFromMainTreeXZMax = -Float.MAX_VALUE; 
  
  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using
  //  Parameters or oscilators
  SpiralArms(LX lx){
    super(lx);
    addParameter(numArmsParam);
    addParameter(armThicknessParam);
    addParameter(twistParam);
    addParameter(rotSpeedParam);

    // Get the Main tree's position.
    theMainTree = model.trees.get(0);// Trees have no Y position.
    theMainTreePos0To1 =
      PosRawToPos0To1(theMainTree.x, 0.0f, theMainTree.z);
    
    // Get the maximum distance from the Main tree.
    for (BaseCube cube : model.baseCubes){
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
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}
    
     // Read the parameter knobs.
    float numArms0To1Desired =
      (float)numArmsParam.getValue() / 100.0f;
    float armThickness0To1Desired =
      (float)armThicknessParam.getValue() / 100.0f;
    float twist0To1Desired=
      (float)twistParam.getValue() / 100.0f;
    float rotSpeed0To1Desired =
      (float)rotSpeedParam.getValuef() / 100.0f;
      
    // Get the parameter values into the ranges we want.
    float numArms = Math.max(1,(int)(numArms0To1Desired * maxNumArms));
    float twistRadsPerUnit = twist0To1Desired * maxNumTwists * Utils.TWO_PI;
    float rotRadsPerS = rotSpeed0To1Desired * maxSpinsPerS * Utils.TWO_PI;
    
    // Derived params from the above.
    float angStep0ToPi = Utils.TWO_PI / (float)(1 + numArms);

    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes){
      
      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);
      
      // Get the relative postion from the main tree in XZ.
      Vec3D posRelToMainTreeXZ = cubePos0To1.sub(theMainTreePos0To1);
      posRelToMainTreeXZ.y = 0.0f;
      
      // Get it's proportional distance as compared against
      // the max distance.
      float distFromMainTreeXZ =
        posRelToMainTreeXZ.magnitude();
      float distFromMainTreeXZPortion =
        distFromMainTreeXZ / distFromMainTreeXZMax;
        
      // Get the base anble and distance.
      float baseR = distFromMainTreeXZPortion;
      
      // Get the angle to use.
      float baseAngXZRads =
        (float)Math.atan2(posRelToMainTreeXZ.z, posRelToMainTreeXZ.x);
      float currAngRads =
        baseAngXZRads +
        (baseR * twistRadsPerUnit) +
        (elapsedTimeS * rotRadsPerS);
        
      float ang0To2Pi = currAngRads % Utils.TWO_PI;
      boolean onAnArm = (((int)(ang0To2Pi / angStep0ToPi)) % 2 == 0);
      float armArea0To1 = (ang0To2Pi % angStep0ToPi) / angStep0ToPi;
      boolean inFilledPartOfArm = (armArea0To1 < armThickness0To1Desired);
      float brightness0to1 = inFilledPartOfArm?1.0f:0.0f;
      float hue0To1 = armArea0To1 / armThickness0To1Desired;
      
      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = lx.hsb(hue0To1 * 360, 100, brightness0to1 * 100);
    }
    
    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class SpiralArms extends TSPattern


//----------------------------------------------------------------------------
/**
Little sparks popping on the cubes.
*/
class Sparks extends AutographedPattern{
  // Constants
  static final float maxSparFadePerS = 30.0f;
  

  // Parameters (to show up on UI).
  final BasicParameter sparkProbabilityPercentagePerSParam =
    new BasicParameter("SPS", 3, 1, 100);
  final BasicParameter sparkFadePercentagePerSParam  =
    new BasicParameter("SFD", 25, 1, 100);   

  Sparks(LX lx){
    super(lx);
    addParameter(sparkProbabilityPercentagePerSParam);
    addParameter(sparkFadePercentagePerSParam);
  }
    
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
  
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}
    
    // Read the parameter knobs.
    float sparkProbability0To1PerS =
      (float)sparkProbabilityPercentagePerSParam.getValue() / 100.0f;
    float sparkFade0To1PerS =
      (float)sparkFadePercentagePerSParam.getValuef() / 100.0f;
    sparkFade0To1PerS *= maxSparFadePerS;

    // Determine the amount of fading to do this frame.
    float sparkProbability0To1ThisFrame0To1 = deltaTimeS * sparkProbability0To1PerS;
    sparkProbability0To1ThisFrame0To1 =
      Utils.constrain(sparkProbability0To1ThisFrame0To1, 0.0f, 1.0f);
    float sparkFadeAmountThisFrame0To1 = deltaTimeS * sparkFade0To1PerS;
    sparkFadeAmountThisFrame0To1 =
      Utils.constrain(sparkFadeAmountThisFrame0To1, 0.0f, 1.0f);
      
    // Iterate over all the cubes (shrub or not).
    for (BaseCube cube : model.baseCubes){
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


//----------------------------------------------------------------------------
/**
Do blooms of lights jumping from sculpture to sculpture.
*/
class Blooms extends AutographedPattern{
  static final float maxBloomRadiusProportionPerS = 60.0f;
  static final float maxBloomRadiusParamPower = 3.0f;

  // Parameters (to show up on UI).
  final BasicParameter shrubBloomsPerTreeBloomParam =
    new BasicParameter("SPT", 2.5 * shrubIdxCnt, 1, 10 * shrubIdxCnt);
  final BasicParameter treeBloomRadiusSpeedParam =
    new BasicParameter("TSP", 25, 1, 100);
  final BasicParameter treeFadePercentagePerSParam  =
    new BasicParameter("TFD", 25, 1, 100);
  final BasicParameter shrubBloomRadiusSpeedParam =
    new BasicParameter("SSP", 40, 1, 100);
  final BasicParameter shrubFadePercentagePerSParam  =
    new BasicParameter("SFD", 50, 1, 100);
  
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

  Blooms(LX lx){
    super(lx);
    addParameter(shrubBloomsPerTreeBloomParam);
    addParameter(treeBloomRadiusSpeedParam);
    addParameter(treeFadePercentagePerSParam);
    addParameter(shrubBloomRadiusSpeedParam);
    addParameter(shrubFadePercentagePerSParam);
  }
    
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
  
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}
    
    // Read the parameter knobs.
    int shrubBloomsPerTreeBloom =
      (int)shrubBloomsPerTreeBloomParam.getValuef();
    float desiredCurrTreeBloomRadiusProportionPerS =
      maxBloomRadiusProportionPerS *
      (float)Math.pow(((float)treeBloomRadiusSpeedParam.getValue() / 100.0f), maxBloomRadiusParamPower);
    float treeFade0To1PerS =
      (float)treeFadePercentagePerSParam.getValuef() / 100.0f;
    float desiredCurrShrubBloomRadiusProportionPerS =
      maxBloomRadiusProportionPerS *
      (float)Math.pow(((float)shrubBloomRadiusSpeedParam.getValue() / 100.0f), maxBloomRadiusParamPower);
    float shrubFade0To1PerS =
      (float)shrubFadePercentagePerSParam.getValuef() / 100.0f;

    // Determine the amount of fading to do this frame.
    float treeFadeAmountThisFrame0To1 = deltaTimeS * treeFade0To1PerS;
    treeFadeAmountThisFrame0To1 =
      Utils.constrain(treeFadeAmountThisFrame0To1, 0.0f, 1.0f);
    float shrubFadeAmountThisFrame0To1 = deltaTimeS * shrubFade0To1PerS;
    shrubFadeAmountThisFrame0To1 =
      Utils.constrain(shrubFadeAmountThisFrame0To1, 0.0f, 1.0f);
     
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
    for (BaseCube cube : model.baseCubes){
      // Set the fade rate based on type.
      float thisCubeFadeAmountThisFrame0To1 =
        (cube.pieceType == PieceType.TREE)?
          treeFadeAmountThisFrame0To1 :
          shrubFadeAmountThisFrame0To1;

      // Get the sculpture local position of this cube.
      float cubeAngRads0To2Pi = (Utils.TWO_PI / 360.f) * cube.theta;
      float cubeRadiusPortion = 0.0f;
      if (cube.pieceType == PieceType.TREE){
        cubeRadiusPortion = TreeRadiusRawToRadiusPortion(cube.r);
      }else{
        cubeRadiusPortion = ShrubRadiusRawToRadiusPortion(cube.r);
      }
      
      // See if we should light up this cube (i.e. is it
      // on the active part of the active scuplture to bloom).
      // First, check if this cube is a shrub cube.
      boolean cubeIsOnDesiredSculpture = false;
      if (cube.pieceType == PieceType.TREE){
        // Get the tree cube, and access tree specific properties.
        Cube treeCube = (Cube) cube;
        int treeIdx = treeCube.config.treeIndex;
        
        // Is this cube on the current scuplture?
        cubeIsOnDesiredSculpture =
          (desiredSculptureType == PieceType.TREE) && 
          (treeIdx == desiredCurrTreeIdx);
      } 
      else if (cube.pieceType == PieceType.SHRUB) {
        // Get the shrub cube, and access shrub specific properties.
        ShrubCube shrubCube = (ShrubCube) cube;
        int shrubIdx = shrubCube.config.shrubIndex;
        
        // Is this cube on the current scuplture?
        cubeIsOnDesiredSculpture =
          (desiredSculptureType == PieceType.SHRUB) && 
          (shrubIdx == desiredCurrShrubIdx);
      }
      // TODO: we have a new type, fairyCircle....
      
      // Get the index of the color for this cube.
      int colorArrIdx = cube.index;
      
      // If this cube is on the correct scuplture and
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
    }// END for (BaseCube cube : model.baseCubes) 
    
    // Update which sculpture we're interested in if approriate.
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
          
          // Incriment the number of shrub blooms.
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
    
  }// END public void run(double deltaMs) 
}// class Blooms extends TSPattern


//----------------------------------------------------------------------------
/**
A moving point source farling outward.
*/
class MovingPoint extends AutographedPattern{
  // Constants
  static final float durationMaxS = 10.0f;
  static final float speedMaxPerS = 5.0f;
  static final float sizeMax = 2.0f;
  static final int paletteSize = 6;
  
  // Parameters (to show up on UI).
  final BasicParameter durationParam = new BasicParameter("Duration", 25, 0, 100);
  final BasicParameter sizeParam = new BasicParameter("Size", 60, 0, 100);
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 0, 100);

  // Variables
  private float sourcePosX0To1 = 0.5f;
  private float sourcePosZ0To1 = 0.5f;
  private float sourceDirX0To1 = 0.5f;
  private float sourceDirZ0To1 = 0.5f;
  private ColorPalette palette;

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or oscilators
  MovingPoint(LX lx){
    super(lx);
    addParameter(durationParam);
    addParameter(sizeParam);
    addParameter(speedParam);
    
    // Pick a new source location.
    sourcePosX0To1 = (float)Math.random();
    sourcePosZ0To1 = (float)Math.random();
     
    // Pick a new source dir.
    float sourceDirRads0ToTwoPi = (float)Math.random() * Utils.TWO_PI;
    sourceDirX0To1 = (float)Math.cos(sourceDirRads0ToTwoPi);
    sourceDirZ0To1 = (float)Math.sin(sourceDirRads0ToTwoPi);
    
    // Set up a random paleete of colors.
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
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}
    
    float duration0To1 = durationParam.getValuef() / 100.0f;
    float duration0ToMaxS = duration0To1 * durationMaxS;
    if(elapsedTimeS > duration0ToMaxS){
      // Pick a new source location.
      //sourcePosX0To1 = (float)Math.random();
      //sourcePosZ0To1 = (float)Math.random();
     
      // Pick a new source dir.
      float sourceDirRads0ToTwoPi = (float)Math.random() * Utils.TWO_PI;
      sourceDirX0To1 = (float)Math.cos(sourceDirRads0ToTwoPi);
      sourceDirZ0To1 = (float)Math.sin(sourceDirRads0ToTwoPi);
      
      // Pick a new pallete.
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
    for (BaseCube cube : model.baseCubes){
      
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
        int chosenPaletteIdx = (int)(value0To1 * (float)paletteSize);
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
}// END class MovingPoint extends TSPattern


//----------------------------------------------------------------------------
/**
Circular hue cycle waves starting far from the main tree and moving toward it.
*/

// NOTE! This pattern depends on there being a tree at position 0
// which is not true with entwined.
// It has thus been removed from the main pattern list until repaired.

class WavesToMainTree extends AutographedPattern{
  // Constants
  static final float speedMult = 1000;
  
  // Parameters (to show up on UI).
  final BasicParameter speedParam =
    new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope =
    new BasicParameter("waveSlope", 360, 1, 720);
  final SawLFO wave =
    new SawLFO(0, 360, speedParam.getValuef() * speedMult);

  // Variables
  private Tree theMainTree;
  private Vec3D theMainTreePos0To1;
  private float distFromMainTreeXZMax = -Float.MAX_VALUE; 
  
  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using
  //  Parameters or oscilators
  WavesToMainTree(LX lx){
    super(lx);
    addModulator(wave).start();
    addParameter(waveSlope);
    addParameter(speedParam);

    // Get the Main tree's position.
    theMainTree = model.trees.get(0);// Trees have no Y position.
    theMainTreePos0To1 =
      PosRawToPos0To1(theMainTree.x, 0.0f, theMainTree.z);
    
    // Get the maximum distance from the Main tree.
    for (BaseCube cube : model.baseCubes){
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
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}
    
    // uPdate the wave speed.
    wave.setPeriod(speedParam.getValuef() * speedMult);

    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes){
      
      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);
      
      // Get the relative postion from the main tree in XZ.
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
        (float)( (wave.getValuef() + waveSlope.getValuef() * val0To1 ) % 360);
      
      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = lx.hsb( hue0To360, 100, 100);
    }
    
    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class WavesToMainTree extends TSPattern


//----------------------------------------------------------------------------
/**
Undulating cloud-like patterns.
*/
class Undulation extends AutographedPattern{
  // Constants
  static final float durationMaxS = 10.0f;
  static final float sizeMax = 0.5f;
  static final float rNoiseTimeOffset = 0.0f;
  static final float gNoiseTimeOffset = 37.3f;
  static final float bNoiseTimeOffset = 51.77f;
  static final float colorPower = 2.5f;
  

  // Parameters (to show up on UI).
  final BasicParameter sizeParam =
    new BasicParameter("size", 50, 0, 100);
  final BasicParameter durationParam =
    new BasicParameter("duration", 25, 0, 100);

  // Variables
  private float elapsedNoiseTimeS = 0.0f;

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if
  // you're using Parameters or oscilators
  Undulation(LX lx){
    super(lx);
    addParameter(sizeParam);
    addParameter(durationParam);

    // Reset the elapsed time.
    elapsedNoiseTimeS = 0.0f;
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
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
    
    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes){
      
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
      // The way we know which element of that array represents the particulear cube
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


//----------------------------------------------------------------------------
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
  final BasicParameter sizeParam =
    new BasicParameter("SIZ", 80, 0, 100);
  final BasicParameter durationParam =
    new BasicParameter("DUR", 47, 0, 100);
  final BasicParameter ribbonThresh =
    new BasicParameter("TSH", 48, 0, 100);
  final BasicParameter ribbonWidth =
    new BasicParameter("WID", 27, 0, 100);
  final BasicParameter colorsSizeParam =
    new BasicParameter("CSZ", 48, 0, 100);

  // Variables
  private float elapsedNoiseTimeS = 0.0f;  

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if
  // you're using Parameters or oscilators
  HueRibbons(LX lx){
    super(lx);
    addParameter(sizeParam);
    addParameter(durationParam);
    addParameter(ribbonThresh);
    addParameter(ribbonWidth);
    addParameter(colorsSizeParam);

    // Reset the elapsed time.
    elapsedNoiseTimeS = 0.0f;
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
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
    for (BaseCube cube : model.baseCubes){
      
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
      colors[colorArrIdx] = lx.hsb(hue0To360, 100, ribbonBrightness0To100);
    }
    
    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class HueRibbons extends TSPattern



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
  final BasicParameter sizeParam = new BasicParameter("size", 75, 0, 100);
  final BasicParameter durationParam = new BasicParameter("duration", 33, 0, 100);
  final BasicParameter ribbonThresh = new BasicParameter("ribbonThresh", 33, 0, 100);
  final BasicParameter ribbonWidth = new BasicParameter("ribbonWidth", 15, 0, 100);
  
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

    // Allow for the overal effect to be faded completely
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
/**
Vertical hue cycle waves starting at the ground and moving upward.
*/
class VerticalColorWaves extends AutographedPattern{
  // Constants
  static final float speedMult = 1000;

  // Parameters (to show up on UI).
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final SawLFO wave = new SawLFO(0, 360, speedParam.getValuef() * speedMult);

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or oscilators
  VerticalColorWaves(LX lx){
    super(lx);
    addModulator(wave).start();
    addParameter(waveSlope);
    addParameter(speedParam);
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}
    
    wave.setPeriod(speedParam.getValuef() * speedMult);

    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes){
      
      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);
      
      // Determine the hue to use.
      float val0To1 = 1.0f - cubePos0To1.y;
      float hue0To360 = (float)( (wave.getValuef() + waveSlope.getValuef() * val0To1 ) % 360);
      
      // Set the color of the cube.
      int colorArrIdx = cube.index;
      colors[colorArrIdx] = lx.hsb( hue0To360, 100, 100);
    }
    
    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
}// END class VerticalColorWaves extends TSPattern


//----------------------------------------------------------------------------
/**
A flocking set of points.
*/
class FlockingPoints extends AutographedPattern{
  // Constants
  static final int numBoids = 50;
  static final float boidWorldSize = 640;
  
  // Parameters (to show up on UI).
  final BasicParameter bri = new BasicParameter("bri", 25, 0, 100);
  final BasicParameter rad = new BasicParameter("rad", 10, 0, 100);
  final BasicParameter fad = new BasicParameter("fad", 75, 0, 100);  
  final BasicParameter bodR = new BasicParameter("bodR", 2, 0, 25);
  final BasicParameter maxF0To100 = new BasicParameter("maxF", 25, 0, 100);
  final BasicParameter maxS = new BasicParameter("fad", 16, 0, 100);
  
  // Variables
  ArrayList<Boid> boids; // An ArrayList for the flock of boids.
  
  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or oscilators
  FlockingPoints(LX lx){
    super(lx);
    addParameter(bri);
    addParameter(rad);
    addParameter(fad);
    addParameter(bodR);
    addParameter(maxF0To100);
    addParameter(maxS);
    
    // Make the flock of boids.
    float worldH = boidWorldSize;
    float worldW = boidWorldSize;
    createFlock(numBoids, worldH, worldW); 
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs){
    // Update time related variables.
    super.run(deltaMs);
    
    // Allow for the overal effect to be faded completely
    // out and do not processing.
    if (getChannel().getFader().getNormalized() == 0){return;}
    
    float bri0To100 = bri.getValuef();
    float rad0To1 = rad.getValuef() / 100.0f;
    float fFade0To1PerS = fad.getValuef() / 100.0f;
    
    float fadeAmountThisFrame0To1 = deltaTimeS * fFade0To1PerS;
    fadeAmountThisFrame0To1 = Utils.constrain(fadeAmountThisFrame0To1, 0.0f, 1.0f);
    //System.out.println("fadeAmountThisFrame0To1 = " + fadeAmountThisFrame0To1);
    
    // Do boid control.
    float bodRad = bodR.getValuef();
    float maxF0To1 = maxF0To100.getValuef() / 100.0f;
    float maxFor = maxF0To1 * 4.0f;
    float maxSpd = maxS.getValuef();
    setFlockBodies(bodRad, maxFor, maxSpd);
    
    // Update the flock.
    updateFlock(deltaTimeS);
   
    // Use a for loop here to set the cube colors
    for (BaseCube cube : model.baseCubes){
      // Get the color of the cube.
      int colorArrIdx = cube.index;
      
      // Fade the old color toward black.
      int oldColor = colors[colorArrIdx];
      int fadedOldColor =
        FadeColorTowardBlack(oldColor, fadeAmountThisFrame0To1);

      // Get the position on 0 to 1 of the cube.
      Vec3D cubePos0To1 = PosRawToPos0To1(cube.x, cube.y, cube.z);
      
      // Determine the brightness to use.
      int summedNewColors = lx.hsb(0, 0, 0);
      boolean hasNewColor = false;
      for (Boid b : boids){
        float boidPosX0To1 = b.position.x / boidWorldSize;
        float boidPosZ0To1 = b.position.y / boidWorldSize;
        float boidHue0To360 = b.colorH0To1 * 360.0f;
        int boidColor = lx.hsb(boidHue0To360, 100, bri0To100);

        float posRelToBoidX = cubePos0To1.x - boidPosX0To1; 
        float posRelToBoidZ = cubePos0To1.z - boidPosZ0To1;
        float distFromBoid =
          (float)Math.sqrt(
            (posRelToBoidX * posRelToBoidX) +
            (posRelToBoidZ * posRelToBoidZ));
        if(distFromBoid <= rad0To1){
            summedNewColors = LXColor.blend(summedNewColors, boidColor, LXColor.Blend.ADD);
            hasNewColor = true;
        }
      }
      
      // Set the color of the cube.
      if(hasNewColor){
        colors[colorArrIdx] =
          LXColor.lightest(
            fadedOldColor,
            summedNewColors);
      }else{
        colors[colorArrIdx] = fadedOldColor;
      }                            
    }

    // Make sure to show off the magic cube so we know
    // this is one of our patterns.
    UpdateAndSetColorOfTheOneAutographCube();
  }// END run()
  
  // Make the flock of boids.
  private void createFlock(int numBoidsToCreate, float worldH, float worldW){

    // Initialize the ArrayList
    // Add an initial set of boids into the system
    boids = new ArrayList<Boid>(); 
    for (int i = 0; i < numBoidsToCreate; i++){
      float colorH0To1 = (float)Math.random();
      
      float startPosX = (float)Math.random() * worldH;
      float startPosZ = (float)Math.random() * worldW;
      Boid b = new Boid(startPosX, startPosZ, colorH0To1);
      b.setBorders(worldH, worldW);
      boids.add(b);
    }
  }
  private void setFlockBodies(float bodR, float maxF, float maxS){
        // Note that since each boid must know about the flock we
    // pass the whole flock to each boid for its update.
    for (Boid b : boids){
      b.setBody(bodR, maxF, maxS);
    } 
  }
  // Update the flock of boids.
  private void updateFlock(float deltaTimeS){
    // Note that since each boid must know about the flock we
    // pass the whole flock to each boid for its update.
    for (Boid b : boids){
      b.update(boids);
    } 
  }
}// END class FlockingPoints extends TSPattern



//----------------------------------------------------------------------------
/**
Boids are agents that move like flocking, herding, schooling, swarming
creatures.
Boids are often drawn as a triangle rotated in the direction of the boid's
velocity.
*/
class Boid {
  // Flocking control.
  static final float neighbordist = 50;
  static final float desiredseparation = 25.0f;
  static final float sepWeight = 1.5f;// Separation
  static final float aliWeight = 1.0f;// Alignment
  static final float cohWeight = 1.0f;// Cohesion
   
  // Boid movement control.
  float bodRadius = 2.0f;
  float maxforce = 0.24f;   // Maximum steering force
  float maxspeed = 16.0f;    // Maximum speed
  
  // World control.
  float worldWidth = 640.0f;
  float worldHeight = 640.0f;
  
  // Variables
  float colorH0To1;
  Vec2D position;
  Vec2D vel;
  Vec2D acceleration;

  Boid(float xPos, float yPos, float hueTo1_in){
    position = new Vec2D(xPos, yPos);
    colorH0To1 = hueTo1_in;

    acceleration = new Vec2D(0, 0);
    float dirRads0ToTwoPi = (float)Math.random() * Utils.TWO_PI;
    vel = new Vec2D((float)Math.cos(dirRads0ToTwoPi), (float)Math.sin(dirRads0ToTwoPi));
  }
  
  void setBody(float bodR, float maxF, float maxS)
  {
    bodRadius = bodR;
    maxforce = maxF;
    maxspeed = maxS;
  }
  
  void setBorders(float worldW, float worldH){
    worldWidth = worldW;
    worldHeight = worldH;
  }

  void update(ArrayList<Boid> boids){
    // Reset accelertion to 0 each cycle
    acceleration.scaleSelf(0);
    
    // Update the acceleration.
    Vec2D steer = getFlockingForces(boids);
    steer.limit(maxforce);  // Limit to maximum steering force
    acceleration.addSelf(steer);

    // Update vel, but limit the speed.
    vel.addSelf(acceleration);
    vel.limit(maxspeed);
    
    // Update postion with wraparound the space.
    position.addSelf(vel);
    if (position.x < -bodRadius) position.x = worldWidth+bodRadius;
    if (position.y < -bodRadius) position.y = worldHeight+bodRadius;
    if (position.x > worldWidth+bodRadius) position.x = -bodRadius;
    if (position.y > worldHeight+bodRadius) position.y = -bodRadius;
  }

  // We accumulate a new acceleration each time based on three
  // rules (described below).
  // To know how to move means knowing about all the other boids.
  Vec2D getFlockingForces(ArrayList<Boid> boids){
    // Determine the flocking forces.
    Vec2D sep = separate(boids);   // Separation
    Vec2D ali = align(boids);      // Alignment
    Vec2D coh = cohesion(boids);   // Cohesion
    
    // Arbitrarily weight these forces
    sep.scaleSelf(sepWeight);
    ali.scaleSelf(aliWeight);
    coh.scaleSelf(cohWeight);
    
    // Add the force vectors to acceleration
    // We could add mass here if we want A = F / M
    Vec2D steer = new Vec2D(0,0);
    steer.addSelf(sep);
    steer.addSelf(ali);
    steer.addSelf(coh);
    
    return steer;   
  }

  // A method that calculates and applies a steering force towards a target.
  Vec2D seek(Vec2D target){
    // A vector pointing from the position to the target.
    Vec2D desiredPosDelta= target.sub(position);  
    
    // Scale to maximum speed.
    Vec2D desiredVel = new Vec2D(desiredPosDelta);
    desiredVel.normalize();
    desiredVel.scaleSelf(maxspeed);

    // Implement Reynolds: Steering = Desired - Vel
    Vec2D steer = desiredVel.sub(vel);
    return steer;
  }
  
  // Separation
  // Method checks for nearby boids and steers away
  Vec2D separate (ArrayList<Boid> boids){
    Vec2D steer = new Vec2D(0, 0);
    int count = 0;
    // For every boid in the system, check if it's too close
    for (Boid other : boids){
      float d = position.distanceTo(other.position);
      // If the distance is greater than 0 and less than an arbitrary amount
      // (0 when you are yourself)
      if ((d > 0) && (d < desiredseparation)){
        // Calculate vector pointing away from neighbor
        Vec2D diff = position.sub(other.position);
        diff.normalize();
        diff.scaleSelf(1.0f/d);        // Weight by distance
        steer.addSelf(diff);
        count++;            // Keep track of how many
      }
    }
    // Average -- divide by how many
    if (count > 0){
      steer.scaleSelf(1.0f/(float)count);
    }

    // As long as the vector is greater than 0
    if (steer.magnitude() > 0){
      // Implement Reynolds: Steering = Desired - Vel
      steer.normalize();
      steer.scaleSelf(maxspeed);
      steer.subSelf(vel);
    }
    return steer;
  }
      
  // Alignment
  // For every nearby boid in the system, calculate the average vel
  Vec2D align (ArrayList<Boid> boids){
    
    Vec2D averageVel = new Vec2D(0, 0);
    int count = 0;
    for (Boid other : boids){
      float d = position.distanceTo(other.position);
      if (d < neighbordist){
        averageVel.addSelf(other.vel);
        count++;
      }
    }
    if (count <= 0){
      return new Vec2D(0, 0);// Return a zero steering.
    }
    averageVel.scaleSelf(1.0f/(float)count);
      
    // Implement Reynolds: Steering = DesiredVel - Vel
    Vec2D desiredVel = new Vec2D(averageVel);
    desiredVel.normalize();
    desiredVel.scaleSelf(maxspeed);
    Vec2D steer = desiredVel.sub(vel);
    return steer;
  }

  // Cohesion
  // For the average position (i.e. center) of all nearby boids, calculate steering vector towards that position
  Vec2D cohesion (ArrayList<Boid> boids){
    Vec2D averagePos = new Vec2D(0, 0);   // Start with empty vector to accumulate all positions
    int count = 0;
    for (Boid other : boids){
      float d = position.distanceTo(other.position);
      if (d < neighbordist){
        averagePos.addSelf(other.position); // Add position
        count++;
      }
    }
    if (count <= 0){
      return new Vec2D(0, 0);// Return a zero steering.
    }
    averagePos.scaleSelf(1.0f/(float)count);
    
    // Steer towards the average position.
    return seek(averagePos);  
  }
}
