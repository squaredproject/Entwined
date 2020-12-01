package com.charlesgadeken.entwined.triggers.drumpad;

import com.charlesgadeken.entwined.EntwinedParameters;
import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MidiEngineTest {
    @Disabled("DISABLED: Only works if drumpad is attached to USB")
    @Test
    public void testInit() {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);
        EntwinedParameters parameters = new EntwinedParameters(lx, model);
        new MidiEngine(lx, parameters, null, null, null);
    }
}
