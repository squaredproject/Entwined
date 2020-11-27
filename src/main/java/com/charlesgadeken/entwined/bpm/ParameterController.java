package com.charlesgadeken.entwined.bpm;

import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.utils.LXUtils;

class ParameterController {
    final LXListenableNormalizedParameter parameter;
    private final double minValue;
    private final double maxValue;
    final double scale;
    private double lastValue;

    ParameterController(
            LXListenableNormalizedParameter parameter,
            double minValue,
            double maxValue,
            double scale) {
        this.parameter = parameter;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.scale = scale;
        lastValue = parameter.getNormalized();
    }

    public void setValue(double value) {
        parameter.setNormalized(LXUtils.lerp(minValue, maxValue, value));
        lastValue = parameter.getNormalized();
    }

    public boolean parameterWasChanged() {
        return parameter.getNormalized() != lastValue;
    }
}
