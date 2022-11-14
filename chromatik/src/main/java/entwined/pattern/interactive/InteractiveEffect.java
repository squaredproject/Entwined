package entwined.pattern.interactive;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import entwined.plugin.Config;
import entwined.utils.EntwinedUtils;

import java.util.HashMap;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

// TODO -
// Make effects inherit from new TSEffect
// Add parameter to specify the pieceId
// Add parameter to turn on effect for currentId
// ?? Would like to be able to expose the parameters of the child class, that is,
// introspect the child class, pull out the parameters, add similar parameters to the parent class
// and then set the parameters in the child class based on the values in the parent class when
// I create the childclass.
// Not sure how this serializes when we load the project....

public abstract class InteractiveEffect extends LXEffect {
  HashMap<String, EffectInfo> activeEffects = new HashMap<String, EffectInfo>();
  int effectDuration = 6000;
  Class<?> childClass;  // child classes must define this. Should extend TSEffect
  DiscreteParameter debugId;
  String[] debugOptionsArray = new String[]{"None", "shrub-11", "shrub-12"};

  public InteractiveEffect(LX lx) {
    super(lx);
    debugId = new DiscreteParameter("ID", debugOptionsArray);
    addParameter("debugId", debugId);
  }

  @Override
  public void onParameterChanged(LXParameter param) {
    if (param == debugId) {
      int index = debugId.getValuei();
      if (index > 0) {
        onTriggeredPiece(debugOptionsArray[index]);
      }
    }
  }

  LXModel getModelFromPieceId(String pieceId){
    // exit if this is the null piece
    if (pieceId == "None" ) {
      return null;
    }

    ArrayList<LXModel> pieces = new ArrayList<LXModel>();

    // Look through children to find the one with the correct pieceId
    System.out.println("Model has " + lx.getModel().points.length + " points");
    for (LXModel child : lx.getModel().children) {
      if (child.metaData.get("name").equalsIgnoreCase(pieceId)) {
        pieces.add(child);
        break;
      }
    }

    // If we didn't find anything, look for compound models in the config file
    if (pieces.size() == 0) {
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
    }

    System.out.println("Found " + pieces.size() + " model pieces for " + pieceId);
    if (pieces.size() == 0) {
      return null;
    }

    LXModel[] modelArray = new LXModel[pieces.size()];
    modelArray = pieces.toArray(modelArray);
    LXModel newModel = new LXModel(modelArray);
    System.out.println("New model has " + newModel.points.length + " points");
    System.out.println("new model has " + newModel.children.length + " children");
    return newModel;
  }

  abstract void onChildEffectCreated(TSEffect child);
    // Do whatever you need to do to actually turn the child on, if that's what
    // the effect requires.


  public void onTriggeredPiece(String pieceId) {
    if (activeEffects.containsKey(pieceId)) {
      // effect active, reset timeout
      EffectInfo info = activeEffects.get(pieceId);
      info.resetTimeout(effectDuration);
    } else {
      TSEffect newEffect;
      try {
        Constructor constructor = childClass.getConstructor(LX.class);
        newEffect = (TSEffect)constructor.newInstance(lx);
        System.out.println("Have new instance of class");
      } catch (InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
        System.out.println("Could not instantiate effect class, moving on");
        System.out.println(e);
        return;
      }
      LXModel effectModel = getModelFromPieceId(pieceId);
      if (effectModel != null) {
        System.out.println("Setting model on effect, model has " + effectModel.points.length + " points");
        newEffect.setModel(effectModel);
        EffectInfo newEffectInfo = new EffectInfo(newEffect, effectModel, effectDuration);
        activeEffects.put(pieceId, newEffectInfo);
        onChildEffectCreated(newEffect);
      }
    }
  }


  @Override
  public void run(double deltaMs, double strength) {

    // iterate through keys in hashmap
    int oldSize = activeEffects.size();
    ArrayList<String> removeList = new ArrayList<String>();
    int currentTime = EntwinedUtils.millis();
    activeEffects.forEach(
        (key, value) -> {
          if (currentTime > value.timeout) {
            removeList.add(key);
          } else {
            value.effect.setColors(colors);
            value.effect.triggeredRun(deltaMs, strength);
          }
        }
    );


    for (int i=0; i<removeList.size(); i++) {
      // XXX - grab the effect and shut it down!! XXX - this is java. Garbage collection.
      activeEffects.remove(removeList.get(i));
      System.out.println("Removing effect");
    }

    if (activeEffects.size() == 0 && oldSize > 0) {
      debugId.setValue(0);
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

