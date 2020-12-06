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
        new EntwinedOutput(lx, model);
    }

    @Test
    public void testOutputsBuildA() {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);
        EntwinedOutput out = new EntwinedOutput(lx, model);
        out.configureExternalOutput();
    }

    @Test
    public void testOutputsBuildB() {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);
        EntwinedOutput out = new EntwinedOutput(lx, model);
        out.configureFadeCandyOutput();
    }
}
