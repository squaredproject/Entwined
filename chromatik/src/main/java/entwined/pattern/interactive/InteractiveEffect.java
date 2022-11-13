package entwined.pattern.interactive;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import entwined.plugin.Config;
import entwined.utils.EntwinedUtils;

import java.util.HashMap;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;

// TODO -
// Make effects inherit from new TSEffect
// Add parameter to specify the pieceId
// Add parameter to turn on effect for currentId
// ?? Would like to be able to expose the parameters of the child class, that is,
// introspect the child class, pull out the parameters, add similar parameters to the parent class
// and then set the parameters in the child class based on the values in the parent class when
// I create the childclass.
// Not sure how this serializes when we load the project....

abstract class TSEffect extends LXEffect
{
  TSEffect(LX lx) {
    super(lx);
  }

  void triggeredRun(double timeMs, double strength) {
    run(timeMs, strength);
  }
}


public abstract class InteractiveEffect extends LXEffect {
  HashMap<Integer, EffectInfo> activeEffects;
  int effectDuration = 6000;
  Class<?> childClass;  // child classes must define this. Should extend TSEffect
  LX lx;

  public InteractiveEffect(LX lx) {
    super(lx);
  }

  LXModel getModelFromPieceId(int pieceId){
    ArrayList<LXModel> pieces = new ArrayList<LXModel>();

    // Standard case - we're just addressing a single top level component of the sculpture
    if (pieceId >= 0 && pieceId < lx.getModel().children.length) {
      pieces.add(lx.getModel().children[pieceId]);
    }

    // Other case - we're calling out a specified set of high level components
    if (Config.groups.containsKey(pieceId) ) {
      String[] components = Config.groups.get(pieceId);
      LXModel model = lx.getModel();
      for (String componentName : components) {
        for (LXModel child : model.children) {
          if (child.metaData.get("name") == componentName) {
            pieces.add(child);
          }
        }
      }
    }
    return new LXModel(pieces.toArray(new LXModel[pieces.size()]));
  }


  public void startInteractiveEffect(int pieceId) {
    if (activeEffects.containsKey(pieceId)) {
      // effect active, reset timeout
      EffectInfo info = activeEffects.get(pieceId);
      info.resetTimeout(effectDuration);
    } else {
      TSEffect newEffect;
      try {
        newEffect = (TSEffect)(childClass.getConstructor(LX.class).newInstance(lx));
      } catch (InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
        System.out.println("Could not instantiate effect class, moving on");
        return;
      }
      LXModel effectModel = getModelFromPieceId(pieceId);
      newEffect.setModel(effectModel);
      EffectInfo newEffectInfo = new EffectInfo(newEffect, effectModel, effectDuration);
      activeEffects.put(pieceId, newEffectInfo);
    }
  }

  @Override
  public void run(double deltaMs, double strength) {  // And here I'm fucked because we're not actually sending in a model
    // iterate through keys in hashmap
    ArrayList<Integer> removeList = new ArrayList<Integer>();
    int currentTime = EntwinedUtils.millis();
    activeEffects.forEach(
        (key, value) -> {
          if (currentTime > value.timeout) {
            removeList.add(key);
          } else {
            value.effect.triggeredRun(strength, deltaMs);
          }
        }
    );

    for (int i=0; i<removeList.size(); i++) {
      // XXX - grab the effect and shut it down!! XXX - this is java. Garbage collection.
      activeEffects.remove(removeList.get(i));
    }
    // XXX may also want to do a fade if we're getting near the timeout. This would involve making a copy of the colors in the model pieces
    // and lerping them.
    // Hmm. Seems like there's a built in damping thing that maybe I could use....
  }


  class EffectInfo {
    TSEffect effect;
    int timeout;
    LXModel effectModel;

    EffectInfo(TSEffect effect, LXModel effectModel, int timeoutFromNow) {
      this.effect = effect;
      this.effectModel = effectModel;
      this.timeout = EntwinedUtils.millis() + timeoutFromNow;
    }
    void resetTimeout(int timeoutFromNow) {
      this.timeout = EntwinedUtils.millis() + timeoutFromNow;
    }
  }
}

