import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

abstract class TSPattern extends LXPattern {
  ParameterTriggerableAdapter parameterTriggerableAdapter;
  String readableName;

  protected final Model model;

  TSPattern(LX lx) {
    super(lx);
    model = (Model)lx.model;
  }

  void onTriggerableModeEnabled() {
    getChannel().getFader().setValue(0);
    getChannelFade().setValue(0);
    parameterTriggerableAdapter = getParameterTriggerableAdapter();
    parameterTriggerableAdapter.addOutputTriggeredListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        setCallRun(parameter.getValue() != 0);
      }
    });
    setCallRun(false);
  }

  Triggerable getTriggerable() {
    return parameterTriggerableAdapter;
  }

  BasicParameter getChannelFade() {
    return getFaderTransition(getChannel()).fade;
  }

  ParameterTriggerableAdapter getParameterTriggerableAdapter() {
    return new ParameterTriggerableAdapter(lx, getChannelFade());
  }

  void setCallRun(boolean callRun) {
    getChannel().enabled.setValue(callRun);
  }

  boolean getEnabled() {
    return getTriggerable().isTriggered();
  }

  double getVisibility() {
    return getChannel().getFader().getValue();
  }

  TreesTransition getFaderTransition(LXChannel channel) {
    return (TreesTransition) channel.getFaderTransition();
  }
}

abstract class TSTriggerablePattern extends TSPattern implements Triggerable {

  static final int PATTERN_MODE_PATTERN = 0; // not implemented
  static final int PATTERN_MODE_TRIGGER = 1; // calls the run loop only when triggered
  static final int PATTERN_MODE_FIRED = 2; // like triggered, but pattern must disable itself when finished
  static final int PATTERN_MODE_CUSTOM = 3; // always calls the run loop

  int patternMode = PATTERN_MODE_TRIGGER;

  boolean triggerableModeEnabled;
  boolean triggered = true;
  double firedTimer = 0;

  TSTriggerablePattern(LX lx) {
    super(lx);
  }

  void onTriggerableModeEnabled() {
    getChannel().getFader().setValue(1);
    if (patternMode == PATTERN_MODE_TRIGGER || patternMode == PATTERN_MODE_FIRED) {
      setCallRun(false);
    }
    triggerableModeEnabled = true;
    triggered = false;
  }

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

