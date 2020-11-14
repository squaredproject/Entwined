package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.ConfigLoader;
import com.charlesgadeken.entwined.model.config.CubeConfig;
import com.charlesgadeken.entwined.model.config.ShrubConfig;
import com.charlesgadeken.entwined.model.config.ShrubCubeConfig;
import com.charlesgadeken.entwined.model.config.TreeConfig;
import heronarts.lx.LX;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {
    @Test
    public void testModelFromConfigs() {
        LX lx = new LX();
        Model m = Model.fromConfigs(lx);
        assertNotNull(m);
    }
}
