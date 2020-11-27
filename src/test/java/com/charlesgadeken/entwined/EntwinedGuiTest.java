package com.charlesgadeken.entwined;

import static org.junit.jupiter.api.Assertions.*;

import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class EntwinedGuiTest {
    @Test
    void setup() {
        LX.Flags flags = EntwinedGui.headlessInit(null);
        flags.initialize.initialize(new LX());
    }
}
