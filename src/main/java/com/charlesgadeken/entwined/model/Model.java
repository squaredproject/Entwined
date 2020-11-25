package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.config.*;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model extends LXModel {
    /** Trees in the model */
    public final List<Tree> trees;

    /** Cubes in the model */
    public final List<Cube> cubes;

    public final List<BaseCube> baseCubes;

    public final Map<String, Cube[]> ipMap = new HashMap<>();

    private final List<TreeConfig> treeConfigs;

    /**
     * Build a new Entwined model from the expected configuration files.
     *
     * @param lx The LX instance to use
     * @return An instantiated model
     */
    public static Model fromConfigs(LX lx) {
        List<TreeCubeConfig> cubeConfig = ConfigLoader.loadCubeConfigFile();
        List<TreeConfig> treeConfigs = ConfigLoader.loadTreeConfigFile();
        List<ShrubCubeConfig> shrubCubeConfig = ConfigLoader.loadShrubCubeConfigFile();
        List<ShrubConfig> shrubConfigs = ConfigLoader.loadShrubConfigFile();
        return new Model(lx, treeConfigs, cubeConfig, shrubConfigs, shrubCubeConfig);
    }

    private Model(
            LX lx,
            List<TreeConfig> treeConfigs,
            List<TreeCubeConfig> cubeConfig,
            List<ShrubConfig> shrubConfigs,
            List<ShrubCubeConfig> shrubCubeConfig) {

        super(new Fixture(treeConfigs, cubeConfig, shrubConfigs, shrubCubeConfig));


        Fixture f = (Fixture) this.getFixture();

        this.treeConfigs = treeConfigs;
        List<Cube> _cubes = new ArrayList<>();
        List<TreeCubeConfig> _inactiveCubeConfigs = new ArrayList<>();
        this.trees = Collections.unmodifiableList(f.trees);
        for (Tree tree : this.trees) {
            ipMap.putAll(tree.ipMap);
            _cubes.addAll(tree.cubes);
        }
        this.cubes = Collections.unmodifiableList(_cubes);

        this.shrubConfigs = shrubConfigs;
        List<ShrubCube> _shrubCubes = new ArrayList<ShrubCube>();
        this.shrubs = Collections.unmodifiableList(f.shrubs);
        for (Shrub shrub : this.shrubs) {
            shrubIpMap.putAll(shrub.ipMap);
            _shrubCubes.addAll(shrub.cubes);
        }
        this.shrubCubes = Collections.unmodifiableList(_shrubCubes);

        // Adding all cubes to baseCubes
        List<BaseCube> _baseCubes = new ArrayList<BaseCube>();

        for (Tree tree : this.trees) {
            // ipMap.putAll(tree.ipMap);
            _baseCubes.addAll(tree.cubes);
        }

        for (Shrub shrub : this.shrubs) {
            // shrubIpMap.putAll(shrub.ipMap);
            _baseCubes.addAll(shrub.cubes);
        }
        this.baseCubes = Collections.unmodifiableList(_baseCubes);
    }

    private static class Fixture {
        final List<Tree> trees = new ArrayList<>();
        final List<Shrub> shrubs = new ArrayList<>();
        final List<LXPoint> points = new ArrayList<>();

        private Fixture(
                List<TreeConfig> treeConfigs,
                List<TreeCubeConfig> cubeConfigs,
                List<ShrubConfig> shrubConfigs,
                List<ShrubCubeConfig> shrubCubeConfigs) {
            for (int i = 0; i < treeConfigs.size(); i++) {

                TreeConfig tc = treeConfigs.get(i);
                trees.add(
                        new Tree(
                                lx,
                                cubeConfigs,
                                i,
                                tc.x,
                                tc.z,
                                tc.ry,
                                tc.canopyMajorLengths,
                                tc.layerBaseHeights));
            }



            for (Tree tree : trees) {
                Collections.addAll(points, tree.points);
            }

            for (int i = 0; i < shrubConfigs.size(); i++) {
                ShrubConfig sc = shrubConfigs.get(i);
                shrubs.add(new Shrub(lx, shrubCubeConfigs, i, sc.x, sc.z, sc.ry));
            }
            for (Shrub shrub : shrubs) {
                Collections.addAll(points, shrub.points);
            }
        }
    }

    /** Shrubs in the model */
    public final List<Shrub> shrubs;

    /** ShrubCubes in the model */
    public final List<ShrubCube> shrubCubes;

    public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<>();

    private final List<ShrubConfig> shrubConfigs;
}
