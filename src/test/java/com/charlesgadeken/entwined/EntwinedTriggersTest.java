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
        EntwinedTriggers et = new EntwinedTriggers(lx, model, parameters);
        et.configureMIDI();
    }
}
