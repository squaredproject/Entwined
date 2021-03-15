import toxi.geom.Vec2D;

import java.util.*;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;



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

class InteractiveHSVEffect extends Effect {
  
  final BasicParameter hueSet[];
  final BasicParameter hueSetAmount[]; // need to expose this because 0 means none

  final BasicParameter hueShift[];

  final BasicParameter saturation[];

  final BasicParameter brightness[];

  // how many shrubs do we have? Hardcode and figure out later
  final int nShrubs = 20;
  
  InteractiveHSVEffect(LX lx) {
    super(lx);

    System.out.println("InteractiveHSVEffect constructor");

    hueSet = new BasicParameter[nShrubs];
    hueSetAmount = new BasicParameter[nShrubs];
    hueShift = new BasicParameter[nShrubs];
    saturation = new BasicParameter[nShrubs];
    brightness = new BasicParameter[nShrubs];
    for (int i=0;i<nShrubs;i++) {
      String shrubIdStr = String.valueOf(i);
      hueSet[i] = new BasicParameter("Inter"+"HueSet"+shrubIdStr, 0, 360);
      hueSetAmount[i] = new BasicParameter("Inter"+"HueSetVal"+shrubIdStr,0, 180);

      // hue shift
      hueShift[i] = new BasicParameter("Inter"+"HueShift"+shrubIdStr,0,360);
      // 
      saturation[i] = new BasicParameter("Inter"+"Sat"+shrubIdStr,0,100);
      // 
      brightness[i] = new BasicParameter("Inter"+"Bri"+shrubIdStr,0,100);
      // set initial values
      resetShrub(i);
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
  // a certain number of degress. There's some sublty in when/how to clip the
  // edges, so will try a few things.
  // Interesting, having a limitdeg of 180 means no effect, that's the blend (basically)
  static float hueBlend(float src, float dst, float limitDeg) {
    float r;
    float dist = dist360(src,dst);
    r = dst + ( (dist / 180.0f) * limitDeg );
    r = norm360(r);

    // test: output should be within the limit distance of dst
    if ( absdist360(r, dst) > limitDeg ) {
      System.out.println("hueBlendFail: src "+src+" dst "+dst+" res "+r+" limit "+limitDeg);
    }

    return(r);
  }

  protected void run(double deltaMs) {

    // iterate over Cubes not Colors because Cubes have index into colors, not the other way around
    for (BaseCube cube : model.baseCubes) {
      // only the shrubs
      if (cube.treeOrShrub == TreeOrShrub.SHRUB) {
        int shrubId = cube.sculptureIndex;
        float hueSetf = hueSet[shrubId].getValuef();
        float hueSetAmountf = hueSetAmount[shrubId].getValuef();
        float hueShiftf = hueShift[shrubId].getValuef();
        float brightnessf = brightness[shrubId].getValuef();
        float saturationf = saturation[shrubId].getValuef();

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
 
          colors[i] = lx.hsb( h, s, b );
        }
      }
    }
  }


  // set all parameters to values that say nothing 
  public void resetAll() {
    System.out.println(" disable all shrub ");
    for (int i=0;i<hueSet.length;i++) {
      resetShrub(i);
    }
  }

  public void resetShrub(int shrubId) {
      //System.out.println(" disable shrub: "+shrubId);
      if (shrubId >= nShrubs) {
        System.out.println(" disable shrub: can't too large shrubId "+shrubId);
        return;
      }
      hueSet[shrubId].setValue(0f);
      hueSetAmount[shrubId].setValue(180.0f);
      hueShift[shrubId].setValue(0f);
      brightness[shrubId].setValue(50.0f);
      saturation[shrubId].setValue(50.0f);
  }


  // Value is from 0 to 360.0, where 0 means no shift
  public void setShrubHueShift(int shrubId, float value) {
    //System.out.println("SetShrubHue: "+shrubId+" hue "+hue);
    if (shrubId >= nShrubs) {
      System.out.println(" can't set shrub: too large shrubId "+shrubId);
      return;
    }
    hueShift[shrubId].setValue(value);
  }


  // Note: this needs a parallel to disable the hueSet alone,
  // but we don't have it wired in yet from Canopy. If we like "hueSet" we'll
  // wire it in
  public void setShrubHueSet(int shrubId, float hue) {
    //System.out.println("SetShrubHue: "+shrubId+" hue "+hue);
    if (shrubId >= nShrubs) {
      System.out.println(" can't set shrub: too large shrubId "+shrubId);
      return;
    }
    hueSet[shrubId].setValue(hue);
    hueSetAmount[shrubId].setValue(30f); // a good angle to sqaush to 
  }
  
  // Value is from 0 to 100, where 50 means no change
  public void setShrubBrightness(int shrubId, float value) {
    //System.out.println("SetShrubHue: "+shrubId+" hue "+hue);
    if (shrubId >= nShrubs) {
      System.out.println(" can't set shrub: too large shrubId "+shrubId);
      return;
    }
    brightness[shrubId].setValue(value);
  }

  // Value is from 0 to 100, where 50 means no change
  public void setShrubSaturation(int shrubId, float value) {
    //System.out.println("SetShrubHue: "+shrubId+" hue "+hue);
    if (shrubId >= nShrubs) {
      System.out.println(" can't set shrub: too large shrubId "+shrubId);
      return;
    }
    saturation[shrubId].setValue(value);
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
  final public InteractiveFire shrubFires[];

  // how many shrubs do we have? Hardcode and figure out later
  final int nShrubs = 20;
  
  InteractiveFireEffect(LX lx) {

    System.out.println("InteractiveFireEffect constructor");
    shrubFires = new InteractiveFire[nShrubs];
    for (int i=0;i<nShrubs;i++) {
      shrubFires[i] = new InteractiveFire(lx, TreeOrShrub.SHRUB, i); 
    }
  }

  Effect[] getEffects() {
    return ( shrubFires );
  }

  void onTriggeredShrub(int shrubId) {
    shrubFires[shrubId].onTriggered();
  }


  class InteractiveFire extends Effect {
    final BasicParameter maxHeight = new BasicParameter("HEIGHT", 0.8, 0.3, 1);
    final BasicParameter flameSize = new BasicParameter("SIZE", 30, 10, 75);  
    final BasicParameter flameCount = new BasicParameter ("FLAMES", 75, 0, 75);
    private LinearEnvelope fireHeight = new LinearEnvelope(0,0,500);
    final float hue = 0.0f; // red, no need to change it

    private float height = 0;
    private int numFlames = 12;
    private List<Flame> flames;

    // todo: change to tree or shrub
    private final TreeOrShrub objectType;
    private final int objectId;
    private boolean triggerableModeEnabled;
    private long triggerEndMillis; // when to un-enable if enabled
    
    private class Flame {
      public float flameHeight = 0;
      public float theta = Utils.random(0, 360);
      public LinearEnvelope decay = new LinearEnvelope(0,0,0);
    
      public Flame(float maxHeight, boolean groundStart){
        float flameHeight;
        if (Utils.random(1) > .2f) {
          flameHeight = Utils.pow(Utils.random(0, 1), 3) * maxHeight * 0.3f;
        } else {
          flameHeight = Utils.pow(Utils.random(0, 1), 3) * maxHeight;
        }
        decay.setRange(model.yMin, (model.yMax * 0.9f) * flameHeight, Utils.min(Utils.max(200, 900 * flameHeight), 800));
        if (!groundStart) {
          decay.setBasis(Utils.random(0,.8f));
        }
        addModulator(decay).start();
      }
    }

    InteractiveFire(LX lx, TreeOrShrub objectType, int objectId) {
      super(lx);

      this.objectType = objectType;
      this.objectId = objectId;
      this.triggerableModeEnabled = false;

      addParameter(maxHeight);
      addParameter(flameSize);
      addParameter(flameCount);
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

    public void run(double deltaMs) {
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

      for (BaseCube cube : model.baseCubes) {
        if (cube.treeOrShrub == TreeOrShrub.SHRUB && cube.sculptureIndex == objectId) {
          float yn = (cube.transformedY - model.yMin) / model.yMax;
          float cBrt = 0;
          float cHue = 0;
          float flameWidth = flameSize.getValuef() / 2;
          for (int i = 0; i < flames.size(); ++i) {
            if (Utils.abs(flames.get(i).theta - cube.transformedTheta) < (flameWidth * (1- yn))) {
              cBrt = Utils.min(100, Utils.max(0, Utils.max(cBrt, (100 - 2 * Utils.abs(cube.transformedY - flames.get(i).decay.getValuef()) - flames.get(i).decay.getBasisf() * 25) * Utils.min(1, 2 * (1 - flames.get(i).decay.getBasisf())) )));
              cHue = Utils.max(0,  (cHue + cBrt * 0.7f) * 0.5f);
            }
          }
          colors[cube.index] = lx.hsb(
            (cHue + hue) % 360,
            100,
            Utils.min(100, cBrt + Utils.pow(Utils.max(0, (height - 0.3f) / 0.7f), 0.5f) * Utils.pow(Utils.max(0, 0.8f - yn), 2) * 75)
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


