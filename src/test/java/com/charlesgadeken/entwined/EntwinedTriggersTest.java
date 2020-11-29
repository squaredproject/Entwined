package com.charlesgadeken.entwined;

import static org.junit.jupiter.api.Assertions.*;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class EntwinedTriggersTest {
    @Test
    public void testEntwinedTriggers() {
        LX lx = new LX();
        Model model = Model.fromConfigs();
        EntwinedParameters parameters = new EntwinedParameters(lx, model);
        EngineController engineController = new EngineController(lx);
        EntwinedTriggers et = new EntwinedTriggers(lx, model, engineController, parameters);
        et.configureTriggerables();
        et.configureMIDI();
    }
}
