package com.charlesgadeken.entwined.triggers.drumpad;

import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class MidiEngineTest {
    @Test
    public void testInit() {
        LX lx = new LX();
            new MidiEngine(lx, null, null, null, null, null, null, null, null, null, null);
    }
}
