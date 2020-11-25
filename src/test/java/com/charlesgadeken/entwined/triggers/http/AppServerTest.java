package com.charlesgadeken.entwined.triggers.http;

import static org.junit.jupiter.api.Assertions.*;

import com.charlesgadeken.entwined.EngineController;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class AppServerTest {
    @Test
    public void testServerInit() {
        LX lx = new LX();
        EngineController controller = new EngineController(lx);
        new AppServer(lx, controller);
    }
}
