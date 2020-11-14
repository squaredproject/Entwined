package com.charlesgadeken.entwined.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigLoaderTest {

    ConfigLoader configLoader;

    @BeforeAll
    void setup() {
        configLoader = new ConfigLoader();
    }

    @Test
    void loadCubeConfigFile() {
        configLoader.loadCubeConfigFile();
    }

    @Test
    void loadTreeConfigFile() {
        configLoader.loadTreeConfigFile();
    }

    @Test
    void loadShrubCubeConfigFile() {
        configLoader.loadShrubCubeConfigFile();
    }

    @Test
    void loadShrubConfigFile() {
        configLoader.loadShrubConfigFile();
    }
}
