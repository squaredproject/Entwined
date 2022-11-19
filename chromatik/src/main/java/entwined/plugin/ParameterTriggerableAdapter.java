package entwined.plugin;


import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import entwined.core.Triggerable;

/*
interface Triggerable {
  public boolean isTriggered();
  public void onTriggered();
  public void onRelease();
  public void addOutputTriggeredListener(LXParameterListener listener);
}
*/

// Okay. So the point of this class is to take a signal - a binary trigger - and
// turn it into a continuous variable - 'amount' that signals how much of an effect to
// show.
// 'triggeredEventParameter' is what we're tracking - the signal
// 'triggeredEventDampedParameter' - is our modulation of that into a continuous value
// 'enabledParameter' is what we're going to fill with the value of the triggeredEventDampedParameter.
// This is *not* just a fade in/fade out. This is how much of the effect to give.
// Notice that there's also a 'strength' in the trigger interface.
//
public class ParameterTriggerableAdapter implements Triggerable, LXLoopTask {

  private final LX lx;
  private final BooleanParameter triggeredEventParameter = new BooleanParameter("ANON");
  private final DampedParameter triggeredEventDampedParameter = new DampedParameter(triggeredEventParameter, 2);
  private final BooleanParameter isDampening = new BooleanParameter("ANON");
  //private double strength;
  private double strength = 1; // used to be from the drumpad; now I'm just setting it to 1 all the time.

  private final LXNormalizedParameter enabledParameter;
  private final double offValue;
  private final double onValue;

  ParameterTriggerableAdapter(LX lx, LXNormalizedParameter enabledParameter) {
    this(lx, enabledParameter, 0, 1);
  }

  ParameterTriggerableAdapter(LX lx, LXNormalizedParameter enabledParameter, double offValue, double onValue) {
    this.lx = lx;
    this.enabledParameter = enabledParameter;
    this.offValue = offValue;
    this.onValue = onValue;

    lx.engine.addLoopTask(this);
    lx.engine.addLoopTask(triggeredEventDampedParameter.start());
  }

  public void loop(double deltaMs) {
    if (isDampening.isOn()) {
      enabledParameter.setValue((onValue - offValue) * strength * triggeredEventDampedParameter.getValue() + offValue);
      if (triggeredEventDampedParameter.getValue() == triggeredEventParameter.getValue()) {
        isDampening.setValue(false);
      }
    } else {
      if (triggeredEventDampedParameter.getValue() != triggeredEventParameter.getValue()) {
        enabledParameter.setValue((onValue - offValue) * strength * triggeredEventDampedParameter.getValue() + offValue);
        isDampening.setValue(true);
      }
    }
  }

  public void onTimeout() {
    // XXX - pull everything to the released state
    triggeredEventParameter.setValue(false);
    triggeredEventDampedParameter.setValue(0);
    enabledParameter.setValue(offValue);
  }

  public boolean isTriggered() {
    return triggeredEventParameter.isOn();
  }

  public void addOutputTriggeredListener(final LXParameterListener listener) {
    isDampening.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        listener.onParameterChanged(triggeredEventDampedParameter);
      }
    });
  }

  public void onTriggered() {
    // this.strength = strength;
    triggeredEventDampedParameter.setValue((enabledParameter.getValue() - offValue) / (onValue - offValue));
    // println((enabledParameter.getValue() - offValue) / (onValue - offValue));
    triggeredEventParameter.setValue(true);
  }

  public void onReleased() {
    triggeredEventDampedParameter.setValue((enabledParameter.getValue() - offValue) / (onValue - offValue));
    triggeredEventParameter.setValue(false);
  }
}
