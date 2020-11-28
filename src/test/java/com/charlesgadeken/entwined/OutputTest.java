package com.charlesgadeken.entwined;

import static org.junit.jupiter.api.Assertions.*;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class OutputTest {
    @Test
    public void testOutputsConstruction() {
        LX lx = new LX();
        Model model = Model.fromConfigs();
        BasicParameterProxy proxy = new BasicParameterProxy(0.5);
        Output.configureExternalOutput(lx, model, proxy);
        Output.configureFadeCandyOutput(lx, proxy);
    }
}
