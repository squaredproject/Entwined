package entwined.core;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;

public abstract class TSTriggerablePattern extends LXPattern implements Triggerable {

  public static final int PATTERN_MODE_PATTERN = 0; // not implemented
  public static final int PATTERN_MODE_TRIGGER = 1; // calls the run loop only when triggered
  public static final int PATTERN_MODE_FIRED = 2;   // like triggered, but pattern must disable itself when finished
  public static final int PATTERN_MODE_CUSTOM = 3;  // always calls the run loop

  protected int patternMode = PATTERN_MODE_TRIGGER;
  protected BooleanParameter trigger;

  protected boolean triggerableModeEnabled = false;
  protected boolean triggered = true;
  double firedTimer = 0;

  public TSTriggerablePattern(LX lx) {
    super(lx);
    this.trigger = new BooleanParameter("trigger", false);
    this.addParameter("trigger", this.trigger);
  }


  // Note here that I'm using onParameter change to actually do the processing of a trigger
  // rather than onTrigger/onRelease. This layer of indirection - through an otherwise
  // superfluous BooleanParameter - is necessary because record and playback only captures
  // events that occur on Parameters. So during playback I *wont* get the onTrigger event
  // from the APC40 that in turn, triggers the onParameterChanged callback; on the other hand,
  // I should get the onParameterChanged callback itself.  --csw 11/2022
  @Override
  public void onParameterChanged(LXParameter parameter) {
    if (parameter == trigger) {
      boolean isTriggered = trigger.getValueb();
      if (isTriggered) {

        if (patternMode == PATTERN_MODE_TRIGGER || patternMode == PATTERN_MODE_FIRED) {
          enabled.setValue(true);
        }
        triggered = true;
        firedTimer = 0;
        System.out.println("Set Trigger");
      } else {
        if (patternMode == PATTERN_MODE_TRIGGER) {
          System.out.println(" *** shutting down pattern***");
          enabled.setValue(false);
          // triggered = false;
          // setCallRun(false);
        }
        System.out.println("Release Trigger");
        triggered = false;
      }
    }
  }

  //@Override
  //protected
  public void enableTriggerMode() {
  //void onTriggerableModeEnabled() {
    if (patternMode == PATTERN_MODE_TRIGGER || patternMode == PATTERN_MODE_FIRED) {
      enabled.setValue(false);
    }
    triggerableModeEnabled = true;
    triggered = false;
  }

  @Override
  public void onTriggered() {
    this.trigger.setValue(true);
  }

  @Override
  public void onReleased() {
    this.trigger.setValue(false);
  }

  @Override
  public void onTimeout() {
    this.trigger.setValue(false);
    System.out.println("Timeout!!!");
  }

  public void addOutputTriggeredListener(LXParameterListener listener) {
  }
}
