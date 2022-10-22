import java.util.*;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.utils.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;

import entwined.pattern.anon.ColorEffect;



// Per shrub interactivity - HSV
//
// Goal is to have some nice basic obvious effects which overlay all other effects on a per-shrub basis
// Thus, we want to expose parameters that are per-shrub --- which means a LOT of them!
// Note: it seems we don't really need parameters and we don't need to expose them, because the easy
// thing is to expose setters on this function, instead of register all these parameters with known values
// and look them up.
//
// Current experiments show the "hue set" interaction to be a great middle ground between something more blatent,
// and something that's easy to see. Brightness and saturation are kinda fun too - brightness more than saturation.
//
// Parameters: 'Hue' --> 0 to 360 of the hue for a shrub
//           'HueVal' --> the number of "degrees" the hue is squashed to (see above), which means 0 for off,
//                  160 is a good amount for "on"
//          'HueShift' -> we experimented with this but didn't go to production - on some patterns its impossible to see
//          'Brightness', 'Saturation' -> instead of using "set to value" we went with "multiply" because it allows
//             the pattern to come through better.
//
// Note: performance is rather important. For each cube, we need to access the current parameter for that cube,
// so the lookup into the set of paramters will happen a lot. Use classic arrays because the number of shrubs
// won't change. For that reason, this should probably change to arrays of floats or arrays of atomic integers.
//
// Sorry about reusing too much of the math code. Just getting something working. Feel free to clean
// up later.
//
// There is another module which keeps track of whether we're connected and can reset the parameters.
// Count on that unit to do that.
// Author: Brian Bulkowski 2021 brian@bulkowsk.org

class InteractiveHSVEffect extends LXEffect {

  final BoundedParameter hueSet[];
  final BoundedParameter hueSetAmount[]; // need to expose this because 0 means none

  final BoundedParameter hueShift[];

  final BoundedParameter saturation[];

  final BoundedParameter brightness[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  InteractiveHSVEffect(LX lx) {
    super(lx);

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.children.length;
    this.nPieces = nPieces;
    int componentIdx = 0;
    for (LXModel component : model.children) {
      this.pieceIdMap[component] = componentIdx++;
    }


    hueSet = new BoundedParameter[nPieces];
    hueSetAmount = new BoundedParameter[nPieces];
    hueShift = new BoundedParameter[nPieces];
    saturation = new BoundedParameter[nPieces];
    brightness = new BoundedParameter[nPieces];
    for (int i=0;i<nPieces;i++) {
      String pieceId = model.pieceIds[i];  // XXX I have no name associated with this component. Metadata? Maybe.
      hueSet[i] = new BoundedParameter("Inter"+"HueSet"+pieceId, 0, 360);
      hueSetAmount[i] = new BoundedParameter("Inter"+"HueSetVal"+pieceId,0, 180);

      // hue shift
      hueShift[i] = new BoundedParameter("Inter"+"HueShift"+pieceId,0,360);
      //
      saturation[i] = new BoundedParameter("Inter"+"Sat"+pieceId,0,100);
      //
      brightness[i] = new BoundedParameter("Inter"+"Bri"+pieceId,0,100);
      // set initial values
      resetPiece(i);
    }
  }

  static float norm360(float i) {
    while (i < 0.0f) {
      i += 360.0f;
    }
    while (i > 360.0f) {
      i -= 360.0f;
    }
    return(i);
  }

  // distance between a and b in degrees, absolute
  static float absdist360(float a, float b) {
    float r = Math.abs( a - b );
    if (r < 180.0f) return(r);
    return( 360.0f - r );
  }

  // distance between a and b in degrees, negative means a is counterclockwise
  // so for example a = 0, b = 190, the distance is 170, but postive, because the short path is clockwise
  static float dist360(float a, float b) {
    float r = a - b;
    if (Math.abs(r) <= 180.0f) return(r);
    if (r < 0.0f) return( r + 360.0f );
    return( r - 360.0f );
  }


  // quick bit of math: interpolate on the hue circle
  // but do so with a limit. Make sure the color is never outside of
  // a certain number of degrees. There's some subtlety in when/how to clip the
  // edges, so will try a few things.
  // Interesting, having a limitdeg of 180 means no effect, that's the blend (basically)
  static float hueBlend(float src, float dst, float limitDeg) {
    float r;
    float dist = dist360(src,dst);
    r = dst + ( (dist / 180.0f) * limitDeg );
    r = norm360(r);

    // test: output should be within the limit distance of dst
    if ( absdist360(r, dst) > limitDeg ) {
      System.out.println("InteractiveHSVBlendFail: src "+src+" dst "+dst+" res "+r+" limit "+limitDeg);
    }

    return(r);
  }

  @Override
  protected void run(double deltaMs, double amount) {

    // iterate over Cubes not Colors because Cubes have index into colors, not the other way around
    int componentIdx = 0;
    for (LXModel component : model.children) {
      for (LXPoint cube : component.points) {

        float hueSetf = hueSet[componentIdx].getValuef();
        float hueSetAmountf = hueSetAmount[componentIdx].getValuef();
        float hueShiftf = hueShift[componentIdx].getValuef();
        float brightnessf = brightness[componentIdx].getValuef();
        float saturationf = saturation[componentIdx].getValuef();

        if ( hueShiftf > 0.0f || hueSetAmountf < 180.0f ||
             brightnessf != 50.0f || saturationf != 50.0f ) {

          int i = cube.index;
          float h = LXColor.h(colors[i]); // 0-360
          float s = LXColor.s(colors[i]); // 0-100
          float b = LXColor.b(colors[i]); // 0-100

          // first squash if set
          if (hueSetAmountf < 180.0f) {
            h = hueBlend(h, hueSetf, hueSetAmountf);
          }

          // shift if shifting
          if (hueShiftf > 0.0f) {
            h += hueShiftf;
            if (h > 360.0f) h -= 360.0f;
          }

          // brightness of 50 is same, > 50 is brighter, < 50 is dimmer
          if (brightnessf != 50.0f) {
            b = (brightnessf / 50.0f) * b;
            if (b > 100.0f) b = 100.0f;
          }

          // saturation of 50 is same, > 50 is brighter, < 50 is dimmer
          if (saturationf != 50.0f) {
            s = (saturationf / 50.0f) * s;
            if (s > 100.0f) s = 100.0f;
          }

          colors[i] = LX.hsb( h, s, b );
        }
      }
      componentIdx++;
    }
  }


  // set all parameters to values that say nothing
  public void resetAll() {
    System.out.println(" disable all pieces ");
    for (int i=0;i<hueSet.length;i++) {
      resetPiece(i);
    }
  }

  private int getPieceIndex(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) {
      // this is hit if the piece has no string
      //System.out.println(" pieceId not found in map2: "+pieceId);
      return(-1);
    }
    return( pieceIndex_o );
  }

  public void resetPiece(int pieceIndex) {
      hueSet[pieceIndex].setValue(0f);
      hueSetAmount[pieceIndex].setValue(180.0f);
      hueShift[pieceIndex].setValue(0f);
      brightness[pieceIndex].setValue(50.0f);
      saturation[pieceIndex].setValue(50.0f);
  }

  public void resetPiece(String pieceId) {

      int pieceIndex = getPieceIndex(pieceId);
      if (pieceIndex == -1) return;
      resetPiece(pieceIndex);
  }


  // Value is from 0 to 360.0, where 0 means no shift
  public void setPieceHueShift(String pieceId, float value) {

    //System.out.println("SetShrubHue: "+pieceId+" hue "+hue);

    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    hueShift[pieceIndex].setValue(value);
  }


  // Note: this needs a parallel to disable the hueSet alone,
  // but we don't have it wired in yet from Canopy. If we like "hueSet" we'll
  // wire it in
  public void setPieceHueSet(String pieceId, float hue) {
    //System.out.println("SetPieceIdHue: "+pieceId+" hue "+hue);
    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    hueSet[pieceIndex].setValue(hue);
    hueSetAmount[pieceIndex].setValue(30f); // a good angle to sqaush to
  }

  // Value is from 0 to 100, where 50 means no change
  public void setPieceBrightness(String pieceId, float value) {
    //System.out.println("SetPieceBrightness: "+pieceId+" hue "+hue);
    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    brightness[pieceIndex].setValue(value);
  }

  // Value is from 0 to 100, where 50 means no change
  public void setPieceSaturation(String pieceId, float value) {
    //System.out.println("SetPieceSaturation: "+pieceId+" hue "+hue);
    int pieceIndex = getPieceIndex(pieceId);
    if (pieceIndex == -1) return;

    saturation[pieceIndex].setValue(value);
  }

}


// Per shrub interactivity - OneShotTriggers - Fire
//
// Goal is to be able to have cool patterns like "fire" available as a one-shot-trigger on a per-shrub basis.
// while re-using the fire code would be better, and extending it to have a "global" and "per object" mode,
// I'm taking a shortcut and making a second copy of the pattern here.
//
// Given that we want to do a handful of patterns, it would be good to have an Interface for triggering a one-shot
// on a shrub. Hopefully will do that cleanup in a bit :-). This comes from Irene Zhou, you can see the starting point
// in the other file.
//
// Note: this really does need to be a filter, because if you just "replace", then too
// many shrubs are effected. If you "add", then the effect doesn't come through. So,
// filter is the only proper thing. You do want a triggerable from the sense of the framework,
// but it's not a triggerable pattern, it's a triggerable filter.
// Other than that, what I've really done is simply changed the class type to triggerable
// and put some trigger logic around it but other than that, it's all the same

class InteractiveFireEffect {

  // needs to be public because needs to be added by the engine.
  // would be nice to have an interface that allows generic iteration of all patterns
  // by the owner... or something....
  final public InteractiveFire pieceFires[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  // constructor
  InteractiveFireEffect(LX lx, LXModel model) {

    //System.out.println("InteractiveFireEffect constructor");

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.pieceIds.length;
    this.nPieces = nPieces;
    for (LXModel component : model.children) {
      this.pieceIdMap[component.name] = model.pieceIdMap;

    pieceFires = new InteractiveFire[nPieces];
    for (int pieceIndex=0 ; pieceIndex<nPieces ; pieceIndex++) {
      pieceFires[pieceIndex] = new InteractiveFire(lx, pieceIndex);
    }
  }

  LXEffect[] getEffects() {
    return ( pieceFires );
  }

  void onTriggeredPiece(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) return;
    pieceFires[pieceIndex_o ].onTriggered();
  }


  class InteractiveFire extends LXEffect {
    final BoundedParameter maxHeight = new BoundedParameter("HEIGHT", 0.8, 0.3, 1);
    final BoundedParameter flameSize = new BoundedParameter("SIZE", 30, 10, 75);
    final BoundedParameter flameCount = new BoundedParameter ("FLAMES", 75, 0, 75);
    private LinearEnvelope fireHeight = new LinearEnvelope(0,0,500);
    final float hue = 0.0f; // red, no need to change it

    private float height = 0;
    private int numFlames = 12;
    private List<Flame> flames;

    private final int pieceIndex;
    private boolean triggerableModeEnabled;
    private long triggerEndMillis; // when to un-enable if enabled

    private class Flame {
      public float flameHeight = 0;
      public float theta = EntwinedUtils.random(0, 360);
      public LinearEnvelope decay = new LinearEnvelope(0,0,0);

      public Flame(float maxHeight, boolean groundStart){
        float flameHeight;
        if (EntwinedUtils.random(1) > .2f) {
          flameHeight = EntwinedUtils.pow(EntwinedUtils.random(0, 1), 3) * maxHeight * 0.3f;
        } else {
          flameHeight = EntwinedUtils.pow(EntwinedUtils.random(0, 1), 3) * maxHeight;
        }
        decay.setRange(model.yMin, (model.yMax * 0.9f) * flameHeight, EntwinedUtils.min(EntwinedUtils.max(200, 900 * flameHeight), 800));
        if (!groundStart) {
          decay.setBasis(EntwinedUtils.random(0,.8f));
        }
        addModulator(decay).start();
      }
    }

    InteractiveFire(LX lx, int pieceIndex) {
      super(lx);

      this.pieceIndex = pieceIndex;
      this.triggerableModeEnabled = false;

      addParameter("maxHeight", maxHeight);
      addParameter("flameSize", flameSize);
      addParameter("flameCount", flameCount);
      addModulator(fireHeight);

      flames = new ArrayList<Flame>(numFlames);
      for (int i = 0; i < numFlames; ++i) {
        flames.add(new Flame(height, false));
      }
    }

    public void updateNumFlames(int numFlames) {
      for (int i = flames.size(); i < numFlames; ++i) {
        flames.add(new Flame(height, false));
      }
    }

    @Override
    public void run(double deltaMs, double amount) {
      //if (getChannel().getFader().getNormalized() == 0) return;

      //if (!triggered && flames.size() == 0) {
      //  setCallRun(false);
      //}
      if (triggerableModeEnabled == false) return;
      if (System.currentTimeMillis() > triggerEndMillis) onRelease();

      if (!triggerableModeEnabled) {
        height = maxHeight.getValuef();
        numFlames = (int) (flameCount.getValue() / 75 * 30); // Convert for backwards compatibility
      } else {
        height = fireHeight.getValuef();
      }

      if (flames.size() != numFlames) {
        updateNumFlames(numFlames);
      }
      for (int i = 0; i < flames.size(); ++i) {
        if (flames.get(i).decay.finished()) {
          removeModulator(flames.get(i).decay);
          if (flames.size() <= numFlames) {
            flames.set(i, new Flame(height, true));
          } else {
            flames.remove(i);
            i--;
          }
        }
      }

      for (LXPoint cube : model.points) {
        // warning: this is a string compare
        if (cube.pieceIndex == pieceIndex) {
          CubeData cdata = CubeManager.getCube(cube.index);
          float yn = (cdata.localY - model.yMin) / model.yMax;
          float cBrt = 0;
          float cHue = 0;
          float flameWidth = flameSize.getValuef() / 2;
          for (int i = 0; i < flames.size(); ++i) {
            if (EntwinedUtils.abs(flames.get(i).theta - cdata.localTheta) < (flameWidth * (1- yn))) {
              cBrt = EntwinedUtils.min(100, EntwinedUtils.max(0, EntwinedUtils.max(cBrt, (100 - 2 * EntwinedUtils.abs(cdata.localY - flames.get(i).decay.getValuef()) - flames.get(i).decay.getBasisf() * 25) * Utils.min(1, 2 * (1 - flames.get(i).decay.getBasisf())) )));
              cHue = EntwinedUtils.max(0,  (cHue + cBrt * 0.7f) * 0.5f);
            }
          }
          colors[cube.index] = LX.hsb(
            (cHue + hue) % 360,
            100,
            EntwinedUtils.min(100, cBrt + EntwinedUtils.pow(EntwinedUtils.max(0, (height - 0.3f) / 0.7f), 0.5f) * EntwinedUtils.pow(EntwinedUtils.max(0, 0.8f - yn), 2) * 75)
          );
        }
      }

    }

    public void onTriggered() {
      triggerableModeEnabled = true;
      triggerEndMillis = System.currentTimeMillis() + 3000;

      fireHeight.setRange(3,0.8f);
      fireHeight.reset().start();
    };

    public void onRelease() {
      triggerableModeEnabled = false;
      //decay.setRange(numFireflies, 0);
      //decay.reset().start();
    }

  }
}

// add color effects for Canopy use

class InteractiveCandyChaosEffect {
  final public InteractiveCandyChaos pieceEffects[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  // constructor
  InteractiveCandyChaosEffect(LX lx, LXModel model) {

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.pieceIds.length;
    this.nPieces = nPieces;
    this.pieceIdMap = model.pieceIdMap;

    pieceEffects = new InteractiveCandyChaos[nPieces];
    for (int pieceIndex=0 ; pieceIndex<nPieces ; pieceIndex++) {
      pieceEffects[pieceIndex] = new InteractiveCandyChaos(lx, pieceIndex);
    }
  }

  LXEffect[] getEffects() {
    return ( pieceEffects );
  }

  void onTriggeredPiece(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) return;
    pieceEffects[pieceIndex_o ].onTriggered();
  }


  class InteractiveCandyChaos extends CandyTextureEffect {  // XXX Kyle Fleming...
    private boolean triggered;
    private long triggerEndMillis; // when to un-enable if enabled

    InteractiveCandyChaos(LX lx, int pieceIndex) {
      super(lx);

      // turn the effect on 100%
      super.amount.setValue(1);

      this.pieceIndex = pieceIndex;
      this.triggered = false;
    }

    public void run(double deltaMs, double amount) {
      if (triggered == false) return;
      if (System.currentTimeMillis() > triggerEndMillis) onRelease();

      super.run(deltaMs, amount);
    }

    public void onTriggered() {
      triggered = true;
      triggerEndMillis = System.currentTimeMillis() + 3000;
    };

    public void onRelease() {
      triggered = false;
    }
  }
}


class InteractiveRainbowEffect {
  final public InteractiveRainbow pieceEffects[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  // constructor
  InteractiveRainbowEffect(LX lx, LXModel model) {

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.pieceIds.length;
    this.nPieces = nPieces;
    this.pieceIdMap = model.pieceIdMap;

    pieceEffects = new InteractiveRainbow[nPieces];
    for (int pieceIndex=0 ; pieceIndex<nPieces ; pieceIndex++) {
      pieceEffects[pieceIndex] = new InteractiveRainbow(lx, pieceIndex);
    }
  }

  LXEffect[] getEffects() {
    return ( pieceEffects );
  }

  void onTriggeredPiece(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) return;
    pieceEffects[pieceIndex_o ].onTriggered();
  }


  class InteractiveRainbow extends CandyCloudTextureEffect {
    private boolean triggered;
    private long triggerEndMillis; // when to un-enable if enabled

    InteractiveRainbow(LX lx, int pieceIndex) {
      super(lx);

      // turn the effect on 100%
      super.amount.setValue(1);

      this.pieceIndex = pieceIndex;
      this.triggered = false;
    }

    public void run(double deltaMs, double amount) {
      if (triggered == false) return;
      if (System.currentTimeMillis() > triggerEndMillis) onRelease();

      super.run(deltaMs);
    }

    public void onTriggered() {
      triggered = true;
      triggerEndMillis = System.currentTimeMillis() + 3000;
    };

    public void onRelease() {
      triggered = false;
    }
  }
}

class InteractiveDesaturationEffect {
  final public InteractiveDesaturation pieceEffects[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  // constructor
  InteractiveDesaturationEffect(LX lx, LXModel model) {

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.pieceIds.length;
    this.nPieces = nPieces;
    this.pieceIdMap = model.pieceIdMap;

    pieceEffects = new InteractiveDesaturation[nPieces];
    for (int pieceIndex=0 ; pieceIndex<nPieces ; pieceIndex++) {
      pieceEffects[pieceIndex] = new InteractiveDesaturation(lx, pieceIndex);
    }
  }

  LXEffect[] getEffects() {
    return ( pieceEffects );
  }

  void onTriggeredPiece(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) return;
    pieceEffects[pieceIndex_o ].onTriggered();
  }


  class InteractiveDesaturation extends ColorEffect {
    private boolean triggered;
    private long triggerEndMillis; // when to un-enable if enabled

    InteractiveDesaturation(LX lx, int pieceIndex) {
      super(lx);

      // turn the effect on 100%
      super.desaturation.setValue(1);

      this.pieceIndex = pieceIndex;
      this.triggered = false;
    }

    public void run(double deltaMs) {
      if (triggered == false) return;
      if (System.currentTimeMillis() > triggerEndMillis) onRelease();

      super.run(deltaMs);
    }

    public void onTriggered() {
      triggered = true;
      triggerEndMillis = System.currentTimeMillis() + 3000;
    };

    public void onRelease() {
      triggered = false;
    }
  }
}
