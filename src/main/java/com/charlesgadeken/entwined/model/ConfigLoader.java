package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.CubeConfig;
import com.charlesgadeken.entwined.model.config.ShrubConfig;
import com.charlesgadeken.entwined.model.config.ShrubCubeConfig;
import com.charlesgadeken.entwined.model.config.TreeConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
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
                ConfigLoader.SHRUB_CUBE_CONFIG_FILE,
                new TypeToken<List<ShrubCubeConfig>>() {}.getType());
    }

    public static List<ShrubConfig> loadShrubConfigFile() {
        return loadJSONFile(
                ConfigLoader.SHRUB_CONFIG_FILE, new TypeToken<List<ShrubConfig>>() {}.getType());
    }

    private static <T> T loadJSONFile(String filename, Type typeToken) {
        Reader reader = null;
        try {
            reader = new BufferedReader(new FileReader(sketchPath(filename)));
            return new Gson().fromJson(reader, typeToken);
        } catch (IOException ioe) {
            System.out.println("Error reading json file: ");
            System.out.println(ioe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                }
            }
        }
        return null;
    }
}
