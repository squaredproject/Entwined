package com.charlesgadeken.entwined.patterns.general;

import static org.junit.jupiter.api.Assertions.*;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class ExamplePatternTest {

    @Test
    void testBuild() {
        LX lx = new LX();
        Model model = Model.fromConfigs(lx);
        new ExamplePattern(new LX(model));
    }
}
