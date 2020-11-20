package com.charlesgadeken.entwined.patterns.examples;

import com.charlesgadeken.entwined.model.Entwined;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class ExamplePatternTest {

    @Test
    void testBuild() {
        LX lx = new LX();
        Entwined model = Entwined.fromConfigs(lx);
        new ExamplePattern(new LX(model));
    }
}
