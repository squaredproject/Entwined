package entwined.pattern.adam_n_katie;

import entwined.utils.EntwinedUtils;
import entwined.utils.Vec3D;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

//----------------------------------------------------------------------------
//As the base class for other patterns we won't actually register this one
//explicitly.
public class AutographedPattern extends LXPattern {
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
  final BoundedParameter autographCubeSpeedParam =
   new BoundedParameter("SPD", 1.01, 20, 0.01);
  final BoundedParameter autographCubeWaveSlopeParam =
   new BoundedParameter("SLP", 1, 1, 720);
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
     out.sat0To100 = EntwinedUtils.lerp(A.sat0To100, B.sat0To100, t0To1);
     out.bri0To100 = EntwinedUtils.lerp(A.bri0To100, B.bri0To100, t0To1);

     return out;
   }
   public void FadeFullBrightToBlackThroughPureColor(float fadeAmount0To1){
     // Increasing overall saturation actually makes a color darken.
     sat0To100 = sat0To100 + (fadeAmount0To1 * (100.0f - sat0To100));

     // Once the saturation has been fully increased one may start
     // to decrease the brightness.
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
  public AutographedPattern(LX lx){
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
  // XXX - it is possible that the cubes here are only tree cubes - dunno. - CSW
  protected void FindCubesBoundsRawAndMaxSpanRaw(){
   // Get the axis-aligned bounds for the piece relative to the
   // central tree).
   for (LXPoint cube : model.points){
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
   for (LXModel component : model.children) {
     for (LXPoint cube : component.points){
         // Get the local radius of this cube in raw coordinates and
         // convert it into the global 0To1 space.
         float radiusLocalRaw = cube.r;

         // Check if this cube is a shrub cube.
         if (component.tags.contains("TREE")) {
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
       EntwinedUtils.lerp(oldColorS0To100, 100.0f, fadeAmountThisFrame0To1);
     float fadColorB0To100 =
       EntwinedUtils.lerp(oldColorB0To100, 0.0f, fadeAmountThisFrame0To1);

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
  // Helper functions for the autographCube.
  private int desiredAutographCubeShrubIdx = 19;
  private int desiredAutographCubeShrubSpokeIdx = 0;
  private int desiredAutographCubeShrubRadialIdx = 5;
  protected int FindTheDesiredAutographCubeColorArrIdx(){
   //return 127;
   // Iterate over all the cubes (shrub or not).
   int desiredCubeColorArrIdx = 0;
   // Just for Shrubs...
   int shrubIdx = 0;
   for (LXModel component : model.sub("SHRUB")) {
     int cubeIdx = 0;
     for (LXPoint cube : component.points){
       // Get the shrub cube, and access shrub specific properties.
       int shrubSpokeIdx = cubeIdx/12;  // cluster id
       int shrubRadialIdx = cubeIdx%5;  // rod id within cluster
       if( (shrubIdx == desiredAutographCubeShrubIdx) &&
           (shrubSpokeIdx == desiredAutographCubeShrubSpokeIdx) &&
           (shrubRadialIdx == desiredAutographCubeShrubRadialIdx)){
         desiredCubeColorArrIdx = cube.index;
       }
       cubeIdx++;
     }
     shrubIdx++;
   }// END for (LXPoint cube : component.baseCubes)

   return desiredCubeColorArrIdx;
  }
  protected void UpdateAndSetColorOfTheOneAutographCube(){
   // Allow for the overal effect to be faded completely
   // out and do not processing.
   if (getChannel().fader.getNormalized() == 0){return;}

   autographCubeWave.setPeriod(
     autographCubeSpeedParam.getValuef() * autographCubeSpeedMult);

   // Determine the hue to use.
   float hue0To360 =
     (autographCubeWave.getValuef() + autographCubeWaveSlopeParam.getValuef() ) % 360;

   // Set the color of tree to the cube.
   colors[theOneAutographCubeColorArrIdx] = LX.hsb( hue0To360, 100, 100);
  }

  //--------------------------------------------------------------------------
  // Main fun function.
  @Override
  public void run(double deltaMs){
   UpdateElapsedTimeS(deltaMs);
  }
}
