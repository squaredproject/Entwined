package com.charlesgadeken.entwined.triggers.http;

import com.charlesgadeken.entwined.EngineController;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppServerTest {
    @Test
    public void testServerInit() {
        LX lx = new LX();
        EngineController controller = new EngineController(lx);
        new AppServer(lx, controller);
    }

}