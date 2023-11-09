package com.charlesgadeken.entwined.bpm;

import heronarts.lx.parameter.LXListenableNormalizedParameter;

class BPMParameterListener {

    private final BPMTool bpmTool;
    private final LXListenableNormalizedParameter parameter;
    private final double startValue;

    BPMParameterListener(BPMTool bpmTool, LXListenableNormalizedParameter parameter) {
        this.bpmTool = bpmTool;
        this.parameter = parameter;
        startValue = parameter.getNormalized();
    }

    public void stopListening() {
        double endValue = parameter.getNormalized();
        if (startValue != endValue) {
            bpmTool.bindParameter(parameter, startValue, endValue);
        }
    }
}
