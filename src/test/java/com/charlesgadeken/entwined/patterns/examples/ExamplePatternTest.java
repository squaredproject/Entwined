package com.charlesgadeken.entwined.patterns.examples;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class ExamplePatternTest {

    @Test
    void testBuild() {
        LX lx = new LX();
        Model model = Model.fromConfigs();
        new ExamplePattern(new LX(model));
    }
}
