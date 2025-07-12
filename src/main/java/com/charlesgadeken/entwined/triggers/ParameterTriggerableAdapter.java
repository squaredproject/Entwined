package com.charlesgadeken.entwined.triggers;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.*;

public class ParameterTriggerableAdapter implements Triggerable, LXLoopTask {

    private final BooleanParameter triggeredEventParameter = new BooleanParameter("ANON");
    private final DampedParameter triggeredEventDampedParameter =
            new DampedParameter(triggeredEventParameter, 2);
    private final BooleanParameter isDampening = new BooleanParameter("ANON");
    private double strength;

    private final LXNormalizedParameter enabledParameter;
    private final double offValue;
    private final double onValue;

    public ParameterTriggerableAdapter(LX lx, LXListenableNormalizedParameter enabledParameter) {
        this(lx, enabledParameter, 0, 1);
    }

    public ParameterTriggerableAdapter(
            LX lx, LXNormalizedParameter enabledParameter, double offValue, double onValue) {
        this.enabledParameter = enabledParameter;
        this.offValue = offValue;
        this.onValue = onValue;

        lx.engine.addLoopTask(this);
        lx.engine.addLoopTask(triggeredEventDampedParameter.start());
    }

    public void loop(double deltaMs) {
        if (isDampening.isOn()) {
            enabledParameter.setValue(
                    (onValue - offValue) * strength * triggeredEventDampedParameter.getValue()
                            + offValue);
            if (triggeredEventDampedParameter.getValue() == triggeredEventParameter.getValue()) {
                isDampening.setValue(false);
            }
        } else {
            if (triggeredEventDampedParameter.getValue() != triggeredEventParameter.getValue()) {
                enabledParameter.setValue(
                        (onValue - offValue) * strength * triggeredEventDampedParameter.getValue()
                                + offValue);
                isDampening.setValue(true);
            }
        }
    }

    public boolean isTriggered() {
        return triggeredEventParameter.isOn();
    }

    public void addOutputTriggeredListener(final LXParameterListener listener) {
        isDampening.addListener(
                new LXParameterListener() {
                    public void onParameterChanged(LXParameter parameter) {
                        listener.onParameterChanged(triggeredEventDampedParameter);
                    }
                });
    }

    public void onTriggered(float strength) {
        this.strength = strength;
        triggeredEventDampedParameter.setValue(
                (enabledParameter.getValue() - offValue) / (onValue - offValue));
        // println((enabledParameter.getValue() - offValue) / (onValue - offValue));
        triggeredEventParameter.setValue(true);
    }

    public void onRelease() {
        triggeredEventDampedParameter.setValue(
                (enabledParameter.getValue() - offValue) / (onValue - offValue));
        triggeredEventParameter.setValue(false);
    }
}
