package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.ConfigLoader;
import com.charlesgadeken.entwined.model.config.CubeConfig;
import com.charlesgadeken.entwined.model.config.ShrubConfig;
import com.charlesgadeken.entwined.model.config.ShrubCubeConfig;
import com.charlesgadeken.entwined.model.config.TreeConfig;
import heronarts.lx.LX;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ModelTest {
    @Test
    public void testModelInit() {
        List<CubeConfig> cubeConfig = ConfigLoader.loadCubeConfigFile();
        List<TreeConfig> treeConfigs = ConfigLoader.loadTreeConfigFile();
        List<ShrubCubeConfig> shrubCubeConfig = ConfigLoader.loadShrubCubeConfigFile();
        List<ShrubConfig> shrubConfigs = ConfigLoader.loadShrubConfigFile();
        LX lx = new LX();
        Model model = new Model(lx, treeConfigs, cubeConfig, shrubConfigs, shrubCubeConfig);
    }
}
