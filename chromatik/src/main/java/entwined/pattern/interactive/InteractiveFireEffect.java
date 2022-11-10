package entwined.pattern.interactive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.BoundedParameter;

//Per shrub interactivity - OneShotTriggers - Fire
//
//Goal is to be able to have cool patterns like "fire" available as a one-shot-trigger on a per-shrub basis.
//while re-using the fire code would be better, and extending it to have a "global" and "per object" mode,
//I'm taking a shortcut and making a second copy of the pattern here.
//
//Given that we want to do a handful of patterns, it would be good to have an Interface for triggering a one-shot
//on a shrub. Hopefully will do that cleanup in a bit :-). This comes from Irene Zhou, you can see the starting point
//in the other file.
//
//Note: this really does need to be a filter, because if you just "replace", then too
//many shrubs are effected. If you "add", then the effect doesn't come through. So,
//filter is the only proper thing. You do want a triggerable from the sense of the framework,
//but it's not a triggerable pattern, it's a triggerable filter.
//Other than that, what I've really done is simply changed the class type to triggerable
//and put some trigger logic around it but other than that, it's all the same

public class InteractiveFireEffect {

  // needs to be public because needs to be added by the engine.
  // would be nice to have an interface that allows generic iteration of all patterns
  // by the owner... or something....
  final public InteractiveFire pieceFires[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  // constructor
  public InteractiveFireEffect(LX lx, LXModel model) {

   //System.out.println("InteractiveFireEffect constructor");

   // Need to know the different pieces that exist, and be able to look them up by name
   //
   int nPieces = model.children.length;
   this.nPieces = nPieces;
   int componentIdx = 0;
   this.pieceIdMap = new HashMap<String, Integer>();
   for (LXModel component : model.children) {
     this.pieceIdMap.put(component.metaData.get("name"),componentIdx);
     componentIdx++;
   }

   pieceFires = new InteractiveFire[nPieces];
   for (int pieceIndex=0 ; pieceIndex<nPieces ; pieceIndex++) {
     pieceFires[pieceIndex] = new InteractiveFire(lx, pieceIndex);
   }
  }

  public LXEffect[] getEffects() {
   return ( pieceFires );
  }

  public void onTriggeredPiece(String pieceId) {
   Integer pieceIndex_o = pieceIdMap.get(pieceId);
   if (pieceIndex_o == null) return;
   pieceFires[pieceIndex_o ].onTriggered();
  }

  @LXComponent.Hidden
  public class InteractiveFire extends LXEffect {
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
     // public float flameHeight = 0;
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

     // Only do the component that is specified
     for (LXPoint cube : model.children[pieceIndex].points) {
       CubeData cdata = CubeManager.getCube(cube.index);
       float yn = (cdata.localY - model.yMin) / model.yMax;
       float cBrt = 0;
       float cHue = 0;
       float flameWidth = flameSize.getValuef() / 2;
       for (int i = 0; i < flames.size(); ++i) {
         if (EntwinedUtils.abs(flames.get(i).theta - cdata.localTheta) < (flameWidth * (1- yn))) {
           cBrt = EntwinedUtils.min(100, EntwinedUtils.max(0, EntwinedUtils.max(cBrt, (100 - 2 * EntwinedUtils.abs(cdata.localY - flames.get(i).decay.getValuef()) - flames.get(i).decay.getBasisf() * 25) * EntwinedUtils.min(1, 2 * (1 - flames.get(i).decay.getBasisf())) )));
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

