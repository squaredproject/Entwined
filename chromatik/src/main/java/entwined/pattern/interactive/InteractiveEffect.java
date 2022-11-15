package entwined.pattern.interactive;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import entwined.plugin.Config;
import entwined.utils.EntwinedUtils;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.LXDeviceComponent;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

// TODO -
// Make the debug options something more useful and/or dynamic
// ?? Would like to be able to expose the parameters of the child class, that is,
// introspect the child class, pull out the parameters, add similar parameters to the parent class
// and then set the parameters in the child class based on the values in the parent class when
// I create the childclass.

public abstract class InteractiveEffect extends LXEffect {
  private final Map<String, EffectInfo> activeEffects = new HashMap<String, EffectInfo>();

  protected int effectDuration = 6000;

  private final Class<? extends LXDeviceComponent> childClass;  // child classes must define this. Should extend LXEffect or LXPattern

  private final String[] debugOptionsArray = { "None", "shrub-11", "shrub-12" };

  private final DiscreteParameter debugId = new DiscreteParameter("ID", this.debugOptionsArray);

  public InteractiveEffect(LX lx, Class<? extends LXDeviceComponent> childClass) {
    super(lx);
    this.childClass = childClass;
    addParameter("debugId", this.debugId);
  }

  @Override
  public void onParameterChanged(LXParameter param) {
    if (param == this.debugId) {
      int index = this.debugId.getValuei();
      if (index > 0) {
        onTriggeredPiece(this.debugOptionsArray[index]);
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

    // System.out.println("Found " + pieces.size() + " model pieces for " + pieceId);
    if (pieces.size() == 0) {
      return null;
    }

    LXModel[] modelArray = new LXModel[pieces.size()];
    modelArray = pieces.toArray(modelArray);
    LXModel newModel = new LXModel(modelArray);
    return newModel;
  }

  abstract void onChildEffectCreated(LXDeviceComponent child);
    // Do whatever you need to do to actually turn the child on, if that's what
    // the effect requires.


  public void onTriggeredPiece(String pieceId) {
    if (activeEffects.containsKey(pieceId)) {
      // effect active, reset timeout
      EffectInfo info = activeEffects.get(pieceId);
      info.resetTimeout(effectDuration);
    } else {
      LXDeviceComponent newEffect;
      try {
        Constructor<?> constructor = childClass.getConstructor(LX.class);
        newEffect = (LXDeviceComponent)constructor.newInstance(lx);
      } catch (InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
        System.out.println("Could not instantiate effect class, moving on");
        System.out.println(e);
        return;
      }
      LXModel effectModel = getModelFromPieceId(pieceId);
      if (effectModel != null) {
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
            LXDeviceComponent effect = value.effect;
            effect.setBuffer(getBuffer());
            effect.loop(deltaMs);
          }
        }
    );


    for (int i=0; i<removeList.size(); i++) {
      activeEffects.remove(removeList.get(i));
    }

    if (activeEffects.size() == 0 && oldSize > 0) {
      debugId.setValue(0);
    }
    // XXX may also want to do a fade if we're getting near the timeout. This would involve making a copy of the colors in the model pieces
    // and lerping them.
    // Hmm. Seems like there's a built in damping thing that maybe I could use....
  }


  class EffectInfo {
    LXDeviceComponent effect;  // Really has to be a TSEffect or TSPattern; I'm choosing the closest ancestor
    int timeout;
    LXModel effectModel;

    EffectInfo(LXDeviceComponent effect, LXModel effectModel, int timeoutFromNow) {

      this.effect = effect;
      this.effectModel = effectModel;
      this.timeout = EntwinedUtils.millis() + timeoutFromNow;
    }
    void resetTimeout(int timeoutFromNow) {
      this.timeout = EntwinedUtils.millis() + timeoutFromNow;
    }
  }
}

