package com.charlesgadeken.entwined.patterns;

import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.triggers.ParameterTriggerableAdapter;
import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;

public abstract class EntwinedBasePattern extends LXPattern {
    ParameterTriggerableAdapter parameterTriggerableAdapter;
    public String readableName;

    protected final Model model;

    protected EntwinedBasePattern(LX lx) {
        super(lx);
        model = (Model) lx.getModel();
    }

    void onTriggerableModeEnabled() {
        getChannel().fader.setValue(0);
        parameterTriggerableAdapter = getParameterTriggerableAdapter();
        parameterTriggerableAdapter.addOutputTriggeredListener(
                parameter -> setCallRun(parameter.getValue() != 0));
        setCallRun(false);
    }

    void setCallRun(boolean callRun) {
        getChannel().enabled.setValue(callRun);
    }

    ParameterTriggerableAdapter getParameterTriggerableAdapter() {
        return new ParameterTriggerableAdapter(lx, getChannel().fader);
    }
}
