package com.charlesgadeken.entwined.triggers.http;

import static org.junit.jupiter.api.Assertions.*;

import com.charlesgadeken.entwined.EngineController;
import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import org.junit.jupiter.api.Test;

class AppServerTest {
    @Test
    public void testServerInit() {
        Model model = Model.fromConfigs();
        LX lx = new LX(model);

        EngineController controller = new EngineController(lx);
        new AppServer(lx, controller);
    }
}
