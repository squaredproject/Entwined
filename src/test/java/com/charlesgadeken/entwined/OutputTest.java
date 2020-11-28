package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputTest {
    @Test
    public void testOutputsConstruction(){
        LX lx = new LX();
        Model model = Model.fromConfigs();
        BasicParameterProxy proxy = new BasicParameterProxy(0.5);
        Output.configureExternalOutput(lx, model, proxy);
        Output.configureFadeCandyOutput(lx, proxy);
    }

}
