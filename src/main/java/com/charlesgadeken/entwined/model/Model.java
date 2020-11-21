package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.ConfigLoader;
import com.charlesgadeken.entwined.model.config.TreeCubeConfig;
import com.charlesgadeken.entwined.model.config.ShrubConfig;
import com.charlesgadeken.entwined.model.config.ShrubCubeConfig;
import com.charlesgadeken.entwined.model.config.TreeConfig;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
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

    private final ArrayList<ModelTransform> modelTransforms = new ArrayList<ModelTransform>();
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

        super(new Fixture(lx, treeConfigs, cubeConfig, shrubConfigs, shrubCubeConfig));

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

    private static class Fixture extends PseudoAbstractFixture {
        final List<Tree> trees = new ArrayList<>();
        final List<Shrub> shrubs = new ArrayList<>();

        private Fixture(
                LX lx,
                List<TreeConfig> treeConfigs,
                List<TreeCubeConfig> cubeConfigs,
                List<ShrubConfig> shrubConfigs,
                List<ShrubCubeConfig> shrubCubeConfigs) {
            super(lx, "TheInstallation");
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

            List<LXPoint> pts = new ArrayList<>();

            for (Tree tree : trees) {
                Collections.addAll(pts, tree.points);
            }

            for (int i = 0; i < shrubConfigs.size(); i++) {
                ShrubConfig sc = shrubConfigs.get(i);
                shrubs.add(new Shrub(lx, shrubCubeConfigs, i, sc.x, sc.z, sc.ry));
            }
            for (Shrub shrub : shrubs) {
                Collections.addAll(pts, shrub.points);
            }
            this.setPoints(pts);
        }
    }

    public Vec3D getMountPoint(TreeCubeConfig c) {
        Vec3D p = null;
        Tree tree;
        Shrub shrub;
        try {
            tree = this.trees.get(c.treeIndex);
            p =
                    tree.treeLayers
                            .get(c.layerIndex)
                            .branches
                            .get(c.branchIndex)
                            .availableMountingPoints
                            .get(c.mountPointIndex);
            return tree.transformPoint(p);
        } catch (Exception e) {
            System.out.println("Error resolving mount point");
            System.out.println(e);
            return null;
        }
    }

    public void addModelTransform(ModelTransform modelTransform) {
        modelTransforms.add(modelTransform);
        shrubModelTransforms.add(modelTransform);
    }

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
        for (Effect modelTransform : shrubModelTransforms) {
            ModelTransform shrubModelTransform = (ModelTransform) modelTransform;
            if (shrubModelTransform.isEnabled()) {
                shrubModelTransform.transform(this);
            }
        }
        for (ShrubCube cube : shrubCubes) {
            cube.didTransform();
        }
    }

    /** Shrubs in the model */
    public final List<Shrub> shrubs;

    /** ShrubCubes in the model */
    public final List<ShrubCube> shrubCubes;

    public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<String, ShrubCube[]>();

    private final ArrayList<ModelTransform> shrubModelTransforms = new ArrayList<>();
    private final List<ShrubConfig> shrubConfigs;

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
        for (LXEffect modelTransform : shrubModelTransforms) {
            ShrubModelTransform shrubModelTransform = (ShrubModelTransform) modelTransform;
            if (shrubModelTransform.isEnabled()) {
                shrubModelTransform.transform(this);
            }
        }
        for (ShrubCube cube : shrubCubes) {
            cube.didTransform();
        }
    }

    public void addModelTransform(Effect shrubModelTransform) {
        shrubModelTransforms.add((ModelTransform) shrubModelTransform);
    }
}
