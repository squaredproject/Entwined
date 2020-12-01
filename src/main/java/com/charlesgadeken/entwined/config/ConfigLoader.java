package com.charlesgadeken.entwined.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * This is the module which load the JSON configuration of: - The cubes - The trees - The ShrubCubes
 * - The Entwined shrubs That makeup the geometry for the goldengate park exposition
 */
public class ConfigLoader {
    public static final boolean enableIPad = false;
    public static final boolean autoplayBMSet = false;

    public static final boolean enableNFC = false;
    public static final boolean enableAPC40 = false;
    public static final boolean enableSoundSyphon = false;

    public static final boolean enableOutputMinitree = false;
    public static final boolean enableOutputBigtree = true;

    public static final int NUM_CHANNELS = 8;
    public static final int NUM_IPAD_CHANNELS = 3;
    public static final int NUM_KNOBS = 8;
    public static final int NUM_AUTOMATION = 4;

    static final String CUBE_CONFIG_FILE = "entwinedCubes.json";
    static final String TREE_CONFIG_FILE = "entwinedTrees.json";
    static final String SHRUB_CUBE_CONFIG_FILE = "entwinedShrubCubes.json";
    static final String SHRUB_CONFIG_FILE = "entwinedShrubs.json";

    public static List<TreeCubeConfig> loadCubeConfigFile() {
        return loadJSONFile(
                ConfigLoader.CUBE_CONFIG_FILE, new TypeToken<List<TreeCubeConfig>>() {}.getType());
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
            InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(filename);
            reader = new BufferedReader(new InputStreamReader((is)));
            return new Gson().fromJson(reader, typeToken);
        } catch (Exception ioe) {
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
