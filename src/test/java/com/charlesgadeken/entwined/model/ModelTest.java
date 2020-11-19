package com.charlesgadeken.entwined.model;

import static org.junit.jupiter.api.Assertions.*;

import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

public class ModelTest {
    @Test
    public void testModelFromConfigs() {
        LX lx = new LX();
        Model m = Model.fromConfigs(lx);
        assertNotNull(m);
    }
}
