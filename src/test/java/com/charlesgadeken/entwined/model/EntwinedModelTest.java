package com.charlesgadeken.entwined.model;

import static org.junit.jupiter.api.Assertions.*;

import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

public class EntwinedModelTest {
    @Test
    public void testModelFromConfigs() {
        LX lx = new LX();
        Entwined m = Entwined.fromConfigs(lx);
        assertNotNull(m);
    }
}
