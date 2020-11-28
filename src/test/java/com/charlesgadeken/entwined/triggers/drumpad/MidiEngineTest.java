package com.charlesgadeken.entwined.triggers.drumpad;

import com.charlesgadeken.entwined.EntwinedParameters;
import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class MidiEngineTest {
    @Test
    public void testInit() {
        LX lx = new LX();
        Model model = Model.fromConfigs();
        EntwinedParameters parameters = new EntwinedParameters(lx, model);
        new MidiEngine(lx, parameters, null, null, null);
    }
}
