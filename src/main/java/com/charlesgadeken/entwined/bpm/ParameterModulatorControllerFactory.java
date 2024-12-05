package com.charlesgadeken.entwined.bpm;

import heronarts.lx.modulator.*;

class ParameterModulatorControllerFactory {
    public ParameterModulatorController makeQuadraticEnvelopeController() {
        return new ParameterModulatorController(new QuadraticEnvelope(0, 1, 1));
    }

    public ParameterModulatorController makeSawLFOController() {
        return new ParameterModulatorController(new SawLFO(0, 1, 1));
    }

    public ParameterModulatorController makeSinLFOController() {
        return new ParameterModulatorController(new SinLFO(0, 1, 1));
    }

    public ParameterModulatorController makeSquareLFOController() {
        return new ParameterModulatorController(new SquareLFO(0, 1, 1));
    }

    public ParameterModulatorController makeTriangleLFOController() {
        return new ParameterModulatorController(new TriangleLFO(0, 1, 1));
    }
}
