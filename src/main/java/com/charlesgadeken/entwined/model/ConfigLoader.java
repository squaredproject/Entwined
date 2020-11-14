package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.CubeConfig;
import heronarts.lx.model.LXModel;
import java.util.List;

public class ConfigLoader {
    static final boolean enableIPad = false;
    static final boolean autoplayBMSet = false;

    static final boolean enableNFC = false;
    static final boolean enableAPC40 = false;
    static final boolean enableSoundSyphon = false;

    static final boolean enableOutputMinitree = false;
    static final boolean enableOutputBigtree = true;

    static final String CUBE_CONFIG_FILE = "data/entwinedCubes.json";
    static final String TREE_CONFIG_FILE = "data/entwinedTrees.json";
    static final String SHRUB_CUBE_CONFIG_FILE = "data/entwinedShrubCubes.json";
    static final String SHRUB_CONFIG_FILE = "data/entwinedShrubs.json";

    public static List<CubeConfig> loadCubeConfigFile() {
        return loadJSONFile(
                ConfigLoader.CUBE_CONFIG_FILE, new TypeToken<List<CubeConfig>>() {}.getType());
    }

    public static List<TreeConfig> loadTreeConfigFile() {
        return loadJSONFile(
                ConfigLoader.TREE_CONFIG_FILE, new TypeToken<List<TreeConfig>>() {}.getType());
    }

    public static List<ShrubCubeConfig> loadShrubCubeConfigFile() {
        return loadJSONFile(
                ConfigLoader.SHRUB_CUBE_CONFIG_FILE, new TypeToken<List<ShrubCubeConfig>>() {}.getType());
    }

    public static List<ShrubConfig> loadShrubConfigFile() {
        return loadJSONFile(
                ConfigLoader.SHRUB_CONFIG_FILE, new TypeToken<List<ShrubConfig>>() {}.getType());
    }
}
