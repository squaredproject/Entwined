package com.charlesgadeken.entwined.effects;

import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.triggers.Triggerable;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameterListener;

public abstract class EntwinedTriggerablePattern extends EntwinedBasePattern
        implements Triggerable {

    public static final int PATTERN_MODE_PATTERN = 0; // not implemented
    public static final int PATTERN_MODE_TRIGGER = 1; // calls the run loop only when triggered
    public static final int PATTERN_MODE_FIRED =
            2; // like triggered, but pattern must disable itself when finished
    public static final int PATTERN_MODE_CUSTOM = 3; // always calls the run loop

    public int patternMode = PATTERN_MODE_TRIGGER;

    public boolean triggerableModeEnabled;
    public boolean triggered = true;
    public double firedTimer = 0;

    public EntwinedTriggerablePattern(LX lx) {
        super(lx);
    }

    protected void onTriggerableModeEnabled() {
        getChannel().fader.setValue(1);
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

    public void addOutputTriggeredListener(LXParameterListener listener) {}
}
