package com.charlesgadeken.entwined.bpm;

import heronarts.lx.Tempo;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

class TempoAdapter extends LXModulator {

    public final Tempo tempo;

    public final BoundedParameter bpm;
    public final BoundedParameter ramp;

    TempoAdapter(final Tempo tempo) {
        super("Tempo Listener");
        this.tempo = tempo;
        ramp = new BoundedParameter("Tempo Ramp", tempo.ramp());
        bpm = new BoundedParameter("Tempo BPM", tempo.bpm(), 30, 300);
        bpm.addListener(
                new LXParameterListener() {
                    public void onParameterChanged(LXParameter parameter) {
                        tempo.setBpm(parameter.getValue());
                    }
                });
    }

    protected double computeValue(double deltaMs) {
        double progress = tempo.ramp();
        ramp.setValue(progress);
        bpm.setValue(tempo.bpm());
        return progress;
    }
}
