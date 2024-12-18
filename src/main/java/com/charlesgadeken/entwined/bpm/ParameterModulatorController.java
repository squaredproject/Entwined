package com.charlesgadeken.entwined.bpm;

import heronarts.lx.modulator.LXRangeModulator;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import java.util.ArrayList;
import java.util.List;

class ParameterModulatorController implements LXParameterListener {

    private final LXRangeModulator modulator;
    ParameterModulationController modulationController;
    private final List<ParameterController> parameterControllers =
            new ArrayList<ParameterController>();

    ParameterModulatorController(LXRangeModulator modulator) {
        this.modulator = modulator;
    }

    public void startModulatingParameter(
            LXListenableNormalizedParameter parameter,
            double minValue,
            double maxValue,
            double scale) {
        parameterControllers.add(new ParameterController(parameter, minValue, maxValue, scale));
    }

    public void stopModulatingAllParameters() {
        parameterControllers.clear();
    }

    public void onParameterChanged(LXParameter parameter) {
        parameterControllers.forEach(
                (ParameterController parameterController) -> {
                    if (parameterController.parameterWasChanged()) {
                        modulationController.onParameterUnboundItself(
                                parameterController.parameter);
                    } else {
                        double scaledValue = (parameter.getValue() * parameterController.scale) % 1;
                        double transformedValue = modulator.setBasis(scaledValue).getValue();
                        parameterController.setValue(transformedValue);
                    }
                });
    }
}
