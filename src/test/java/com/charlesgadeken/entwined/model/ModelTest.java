package com.charlesgadeken.entwined.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ModelTest {
    @Test
    public void testModelFromConfigs() {
        Model m = Model.fromConfigs();
        assertNotNull(m);
    }
}
