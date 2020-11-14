package com.charlesgadeken.entwined.model;

import static org.junit.jupiter.api.Assertions.*;

import com.charlesgadeken.entwined.model.config.ConfigLoader;
import com.charlesgadeken.entwined.model.config.CubeConfig;
import com.charlesgadeken.entwined.model.config.ShrubConfig;
import com.charlesgadeken.entwined.model.config.ShrubCubeConfig;
import com.charlesgadeken.entwined.model.config.TreeConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigLoaderTest {
    @Test
    void loadCubeConfigFile() {

        List<CubeConfig> cfg = ConfigLoader.loadCubeConfigFile();
        assertNotEquals(cfg.size(), 0);
    }

    @Test
    void loadTreeConfigFile() {
        List<TreeConfig> cfg = ConfigLoader.loadTreeConfigFile();
        assertNotEquals(cfg.size(), 0);
    }

    @Test
    void loadShrubCubeConfigFile() {
        List<ShrubCubeConfig> cfg = ConfigLoader.loadShrubCubeConfigFile();
        assertNotEquals(cfg.size(), 0);
    }

    @Test
    void loadShrubConfigFile() {
        List<ShrubConfig> cfg = ConfigLoader.loadShrubConfigFile();
        assertNotEquals(cfg.size(), 0);
    }
}
