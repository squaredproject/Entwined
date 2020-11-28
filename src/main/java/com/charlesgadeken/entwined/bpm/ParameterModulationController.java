package com.charlesgadeken.entwined.bpm;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import java.util.HashMap;
import java.util.Map;

class ParameterModulationController {

    final TempoAdapter tempoAdapter;

    private final ParameterModulatorController[] modulatorControllers;
    private final Map<LXListenableNormalizedParameter, ParameterModulatorController>
            parametersToControllers =
                    new HashMap<LXListenableNormalizedParameter, ParameterModulatorController>();

    ParameterModulationController(LX lx, ParameterModulatorController[] modulatorControllers) {
        this.modulatorControllers = modulatorControllers;

        tempoAdapter = new TempoAdapter(lx.engine.tempo);
        // @Slee not sure what the modern equivalent is here...
        //        lx.addModulator(tempoAdapter).start();

        for (ParameterModulatorController modulatorController : modulatorControllers) {
            modulatorController.modulationController = this;
            tempoAdapter.ramp.addListener(modulatorController);
        }
    }

    public void bindParameter(
            ParameterModulatorController modulatorController,
            LXListenableNormalizedParameter parameter,
            double minValue,
            double maxValue,
            double scale) {
        if (!parametersToControllers.containsKey(parameter)) {
            parametersToControllers.put(parameter, modulatorController);
            modulatorController.startModulatingParameter(parameter, minValue, maxValue, scale);
        }
    }

    public void onParameterUnboundItself(LXListenableNormalizedParameter parameter) {
        parametersToControllers.remove(parameter);
    }

    public void unbindAllParameters() {
        for (ParameterModulatorController modulatorController : modulatorControllers) {
            modulatorController.stopModulatingAllParameters();
        }
        parametersToControllers.clear();
    }
}
