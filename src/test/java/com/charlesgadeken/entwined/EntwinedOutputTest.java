package com.charlesgadeken.entwined;

import static org.junit.jupiter.api.Assertions.*;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class EntwinedOutputTest {
    @Test
    public void testOutputsBuild() {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);
        EntwinedParameters parameters = new EntwinedParameters(lx, model);
        new EntwinedOutput(lx, model, parameters.outputBrightness);
    }

    @Test
    public void testOutputsBuildDDPDatagrams() {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);
        EntwinedParameters parameters = new EntwinedParameters(lx, model);
        EntwinedOutput out = new EntwinedOutput(lx, model, parameters.outputBrightness);
        out.configureExternalOutput();
    }

    @Test
    public void testOutputsBuildFadecandyDatagrams() {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);
        EntwinedParameters parameters = new EntwinedParameters(lx, model);
        EntwinedOutput out = new EntwinedOutput(lx, model, parameters.outputBrightness);
        out.configureFadeCandyOutput();
    }
}
