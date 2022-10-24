package entwined.core;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameterListener;

public abstract class TSTriggerablePattern extends TSPattern implements Triggerable {

  public static final int PATTERN_MODE_PATTERN = 0; // not implemented
  public static final int PATTERN_MODE_TRIGGER = 1; // calls the run loop only when triggered
  public static final int PATTERN_MODE_FIRED = 2; // like triggered, but pattern must disable itself when finished
  public static final int PATTERN_MODE_CUSTOM = 3; // always calls the run loop

  protected int patternMode = PATTERN_MODE_TRIGGER;

  protected boolean triggerableModeEnabled;
  protected boolean triggered = true;
  double firedTimer = 0;

  public TSTriggerablePattern(LX lx) {
    super(lx);
  }

  @Override
  protected
  void onTriggerableModeEnabled() {
    getChannel().fader.setValue(1);
    if (patternMode == PATTERN_MODE_TRIGGER || patternMode == PATTERN_MODE_FIRED) {
      setCallRun(false);
    }
    triggerableModeEnabled = true;
    triggered = false;
  }

  @Override
  Triggerable getTriggerable() {
    return this;
  }

  public boolean isTriggered() {
    return triggered;
  }

  public void onTriggered(float strength) {
    if (patternMode == PATTERN_MODE_TRIGGER || patternMode == PATTERN_MODE_FIRED) {
      setCallRun(true);
    }
    triggered = true;
    firedTimer = 0;
  }

  public void onRelease() {
    if (patternMode == PATTERN_MODE_TRIGGER) {
      setCallRun(false);
    }
    triggered = false;
  }

  public void addOutputTriggeredListener(LXParameterListener listener) {
  }
}
