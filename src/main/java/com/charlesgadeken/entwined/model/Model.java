package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.config.*;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import com.charlesgadeken.entwined.effects.ModelTransform;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toxi.geom.Vec3D;

public class Model extends LXModelInterceptor {
    /** Trees in the model */
    public final List<Tree> trees;

    /** Cubes in the model */
    public final List<Cube> cubes;

    public final List<BaseCube> baseCubes;

    public final Map<String, Cube[]> ipMap = new HashMap<>();
    private final ArrayList<ModelTransform> modelTransforms = new ArrayList<>();
    private final List<TreeConfig> treeConfigs;
    private final ArrayList<ModelTransform> shrubModelTransforms = new ArrayList<>();

    /**
     * Build a new Entwined model from the expected configuration files.
     *
     * @return An instantiated model
     */
    public static Model fromConfigs() {
        List<TreeCubeConfig> cubeConfig = ConfigLoader.loadCubeConfigFile();
        List<TreeConfig> treeConfigs = ConfigLoader.loadTreeConfigFile();
        List<ShrubCubeConfig> shrubCubeConfig = ConfigLoader.loadShrubCubeConfigFile();
        List<ShrubConfig> shrubConfigs = ConfigLoader.loadShrubConfigFile();
        return new Model(treeConfigs, cubeConfig, shrubConfigs, shrubCubeConfig);
    }

    private Model(
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
        List<ShrubCube> _shrubCubes = new ArrayList<>();
        this.shrubs = Collections.unmodifiableList(f.shrubs);
        for (Shrub shrub : this.shrubs) {
            shrubIpMap.putAll(shrub.ipMap);
            _shrubCubes.addAll(shrub.cubes);
        }
        this.shrubCubes = Collections.unmodifiableList(_shrubCubes);

        // Adding all cubes to baseCubes
        List<BaseCube> _baseCubes = new ArrayList<>();

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

    private static class Fixture extends PseudoAbstractFixture {
        final List<Tree> trees = new ArrayList<>();
        final List<Shrub> shrubs = new ArrayList<>();

        private Fixture(
                List<TreeConfig> treeConfigs,
                List<TreeCubeConfig> cubeConfigs,
                List<ShrubConfig> shrubConfigs,
                List<ShrubCubeConfig> shrubCubeConfigs) {
            super("Entwined");
            for (int i = 0; i < treeConfigs.size(); i++) {

                TreeConfig tc = treeConfigs.get(i);
                trees.add(
                        new Tree(
                                cubeConfigs,
                                i,
                                tc.x,
                                tc.z,
                                tc.ry,
                                tc.canopyMajorLengths,
                                tc.layerBaseHeights));
            }

            List<LXPoint> pts = new ArrayList<>();
            for (Tree tree : trees) {
                Collections.addAll(pts, tree.points);
            }

            for (int i = 0; i < shrubConfigs.size(); i++) {
                ShrubConfig sc = shrubConfigs.get(i);
                shrubs.add(new Shrub(shrubCubeConfigs, i, sc.x, sc.z, sc.ry));
            }
            for (Shrub shrub : shrubs) {
                Collections.addAll(pts, shrub.points);
            }
            setPoints(pts);
        }
    }

    /** Shrubs in the model */
    public final List<Shrub> shrubs;

    /** ShrubCubes in the model */
    public final List<ShrubCube> shrubCubes;

    public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<>();

    private final List<ShrubConfig> shrubConfigs;

    public void runTransforms() {
        for (Cube cube : cubes) {
            cube.resetTransform();
        }
        for (ModelTransform modelTransform : modelTransforms) {
            if (modelTransform.isEnabled()) {
                modelTransform.transform(this);
            }
        }
        for (Cube cube : cubes) {
            cube.didTransform();
        }

        for (ShrubCube cube : shrubCubes) {
            cube.resetTransform();
        }
        for (ModelTransform modelTransform : shrubModelTransforms) {
            if (modelTransform.isEnabled()) {
                modelTransform.transform(this);
            }
        }
        for (ShrubCube cube : shrubCubes) {
            cube.didTransform();
        }
    }

    public Vec3D getShrubMountPoint(ShrubCubeConfig c) {
        Vec3D p = null;
        Shrub shrub;
        try {
            shrub = this.shrubs.get(c.shrubIndex);
            p = shrub.shrubClusters.get(c.clusterIndex).rods.get(c.rodIndex).mountingPoint;
            return shrub.transformPoint(p);
        } catch (Exception e) {
            System.out.println("Error resolving mount point");
            System.out.println(e);
            return null;
        }
    }

    //    public void addShrubModelTransform(ShrubModelTransform modelTransform) {
    //        shrubModelTransforms.add(modelTransform);
    //    }
    public void runShrubTransforms() {
        for (ShrubCube cube : shrubCubes) {
            cube.resetTransform();
        }
        for (ModelTransform modelTransform : shrubModelTransforms) {
            if (modelTransform.isEnabled()) {
                modelTransform.transform(this);
            }
        }
        for (ShrubCube cube : shrubCubes) {
            cube.didTransform();
        }
    }

    public void addModelTransform(EntwinedBaseEffect shrubModelTransform) {
        shrubModelTransforms.add((ModelTransform) shrubModelTransform);
    }

    public void addModelTransform(ModelTransform modelTransform) {
        modelTransforms.add(modelTransform);
        shrubModelTransforms.add(modelTransform);
    }
}
