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

    protected void setCallRun(boolean callRun) {
        getChannel().enabled.setValue(callRun);
    }

    public String getReadableName() {
        return readableName == null ? getClass().getCanonicalName() : readableName;
    }

    @Override
    public abstract void run(double v);
}
