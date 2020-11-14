package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.CubeConfig;
import com.charlesgadeken.entwined.model.config.ShrubConfig;
import com.charlesgadeken.entwined.model.config.ShrubCubeConfig;
import heronarts.lx.LX;
import heronarts.lx.LXLayer;
import heronarts.lx.LXLoopTask;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toxi.geom.Vec3D;

class Model extends LXModel {

    /** Trees in the model */
    public final List<Tree> trees;

    /** Cubes in the model */
    public final List<Cube> cubes;

    public final List<BaseCube> baseCubes;

    public final Map<String, Cube[]> ipMap = new HashMap();

    private final ArrayList<ModelTransform> modelTransforms = new ArrayList<ModelTransform>();
    private final List<TreeConfig> treeConfigs;

    Model(
            List<TreeConfig> treeConfigs,
            List<CubeConfig> cubeConfig,
            List<ShrubConfig> shrubConfigs,
            List<ShrubCubeConfig> shrubCubeConfig) {

        super(new Fixture(treeConfigs, cubeConfig, shrubConfigs, shrubCubeConfig));

        Fixture f = (Fixture) this.fixtures.get(0);

        this.treeConfigs = treeConfigs;
        List<Cube> _cubes = new ArrayList<Cube>();
        List<CubeConfig> _inactiveCubeConfigs = new ArrayList();
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

    private static class Fixture extends LXAbstractFixture {

        final List<Tree> trees = new ArrayList<Tree>();

        final List<Shrub> shrubs = new ArrayList<Shrub>();

        private Fixture(
                List<TreeConfig> treeConfigs,
                List<CubeConfig> cubeConfigs,
                List<ShrubConfig> shrubConfigs,
                List<ShrubCubeConfig> shrubCubeConfigs) {
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
            for (Tree tree : trees) {
                for (LXPoint p : tree.points) {
                    points.add(p);
                }
            }

            for (int i = 0; i < shrubConfigs.size(); i++) {
                ShrubConfig sc = shrubConfigs.get(i);
                shrubs.add(new Shrub(shrubCubeConfigs, i, sc.x, sc.z, sc.ry));
            }
            for (Shrub shrub : shrubs) {
                for (LXPoint p : shrub.points) {
                    points.add(p);
                }
            }
        }
    }

    public Vec3D getMountPoint(CubeConfig c) {
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
        for (Effect modelTransform : shrubModelTransforms) {
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

class Tree extends LXModel {

    /** NDBs in the tree */
    public final Map<String, Cube[]> ipMap;

    /** Cubes in the tree */
    public final List<Cube> cubes;

    /** Layers in the tree */
    public final List<EntwinedLayer> treeLayers;

    /** index of the tree */
    public final int index;

    /** x-position of center of base of tree */
    public final float x;

    /** z-position of center of base of tree */
    public final float z;

    /** Rotation in degrees of tree about vertical y-axis */
    public final float ry;

    Tree(
            List<CubeConfig> cubeConfig,
            int treeIndex,
            float x,
            float z,
            float ry,
            int[] canopyMajorLengths,
            int[] layerBaseHeights) {
        super(new Fixture(cubeConfig, treeIndex, x, z, ry, canopyMajorLengths, layerBaseHeights));
        Fixture f = (Fixture) this.fixtures.get(0);
        this.index = treeIndex;
        this.cubes = Collections.unmodifiableList(f.cubes);
        this.treeLayers = f.treeLayers;
        this.ipMap = f.ipMap;
        this.x = x;
        this.z = z;
        this.ry = ry;
    }

    public Vec3D transformPoint(Vec3D point) {
        return ((Fixture) this.fixtures.get(0)).transformPoint(point);
    }

    private static class Fixture extends LXAbstractFixture {

        final List<Cube> cubes = new ArrayList<Cube>();
        final List<EntwinedLayer> treeLayers = new ArrayList<EntwinedLayer>();
        public final Map<String, Cube[]> ipMap = new HashMap();
        public final LXTransform transform;
        public final List<CubeConfig> inactiveCubeConfigs = new ArrayList();

        Fixture(
                List<CubeConfig> cubeConfig,
                int treeIndex,
                float x,
                float z,
                float ry,
                int[] canopyMajorLengths,
                int[] layerBaseHeights) {
            transform = new LXTransform();
            transform.translate(x, 0, z);
            transform.rotateY(ry * Utils.PI / 180);
            for (int i = 0; i < canopyMajorLengths.length; i++) {
                treeLayers.add(new EntwinedLayer(canopyMajorLengths[i], i, layerBaseHeights[i]));
            }
            for (CubeConfig cc : cubeConfig) {
                if (cc.treeIndex == treeIndex) {
                    Vec3D p;
                    try {
                        p =
                                treeLayers
                                        .get(cc.layerIndex)
                                        .branches
                                        .get(cc.branchIndex)
                                        .availableMountingPoints
                                        .get(cc.mountPointIndex);
                    } catch (Exception e) {
                        System.out.println("Error loading config point");
                        System.out.println(e);
                        p = null;
                    }
                    if (p != null) {
                        cc.isActive = true;
                        Cube cube = new Cube(this.transformPoint(p), p, cc);
                        cubes.add(cube);
                        if (!ipMap.containsKey(cc.ipAddress)) {
                            ipMap.put(cc.ipAddress, new Cube[16]);
                        }
                        Cube[] ndbCubes = ipMap.get(cc.ipAddress);
                        ndbCubes[cc.outputIndex] = cube;
                    }
                }
            }
            for (Map.Entry<String, Cube[]> entry : ipMap.entrySet()) {
                String ip = entry.getKey();
                Cube[] ndbCubes = entry.getValue();
                for (int i = 0; i < 16; i++) {
                    if (ndbCubes[i]
                            == null) { // fill all empty outputs with an inactive cube. Maybe this
                        // would be nicer to do at
                        // the model level in the future.
                        CubeConfig cc = new CubeConfig();
                        cc.treeIndex = treeIndex;
                        cc.branchIndex = 0;
                        cc.cubeSizeIndex = 0;
                        cc.mountPointIndex = 0;
                        cc.outputIndex = i;
                        cc.layerIndex = 0;
                        cc.ipAddress = ip;
                        cc.isActive = false;
                        Cube cube = new Cube(new Vec3D(0, 0, 0), new Vec3D(0, 0, 0), cc);
                        cubes.add(cube);
                        ndbCubes[i] = cube;
                    }
                }
            }
            for (Cube cube : this.cubes) {
                for (LXPoint p : cube.points) {
                    this.points.add(p);
                }
            }
        }

        public Vec3D transformPoint(Vec3D point) {
            this.transform.push();
            this.transform.translate(point.x, point.y, point.z);
            Vec3D result = new Vec3D(this.transform.x(), this.transform.y(), this.transform.z());
            this.transform.pop();
            return result;
        }
    }
}

class TreeConfig {
    float x;
    float z;
    float ry;
    int[] canopyMajorLengths;
    int[] layerBaseHeights;
}

class EntwinedLayer {
    List<EntwinedBranch> branches;

    EntwinedLayer(int canopyMajorLength, int layerType, int layerBaseHeight) {
        List<EntwinedBranch> _branches = new ArrayList<EntwinedBranch>();
        int rotationalPositions[];
        switch (layerType) {
            case 0:
                rotationalPositions = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
                break;
            case 1:
                rotationalPositions = new int[] {0, 2, 4, 6};
                break;
            case 2:
                rotationalPositions = new int[] {1, 3, 5, 7};
                break;
            default:
                rotationalPositions = new int[] {};
        }
        for (int i = 0; i < rotationalPositions.length; i++) {
            EntwinedBranch b =
                    new EntwinedBranch(canopyMajorLength, rotationalPositions[i], layerBaseHeight);
            _branches.add(b);
        }
        this.branches = Collections.unmodifiableList(_branches);
    }
}

class EntwinedBranch {
    /**
     * This defines the available mounting points on a given branch variation. The variable names
     * and ratios for the keypoints reflect what is in the CAD drawings for the branches
     */
    public List<Vec3D> availableMountingPoints;

    private static final int NUM_KEYPOINTS = 5;
    private double[] xKeyPoints = new double[NUM_KEYPOINTS];
    private double[] yKeyPoints = new double[NUM_KEYPOINTS];
    private double[] zKeyPoints = new double[NUM_KEYPOINTS];
    private static final double holeSpacing = 8;

    EntwinedBranch(int canopyMajorLength, int rotationalPosition, int layerBaseHeight) {
        int rotationIndex =
                rotationalPosition > 4 ? 4 - rotationalPosition % 4 : rotationalPosition;
        float canopyScaling = canopyMajorLength / 180;
        double branchLengthRatios[] = {0.37, 0.41, 0.50, 0.56, 0.63};
        double heightAdjustmentFactors[] = {1.0, 0.96, 0.92, 0.88, 0.85};
        double branchLength = canopyMajorLength * branchLengthRatios[rotationIndex];
        xKeyPoints[4] = branchLength;
        xKeyPoints[3] = branchLength * 0.917;
        xKeyPoints[2] = branchLength * 0.623;
        xKeyPoints[1] = branchLength * 0.315;
        xKeyPoints[0] = canopyScaling * 12;
        yKeyPoints[4] = 72 * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[3] = 72 * 0.914 * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[2] = 72 * 0.793 * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[1] = (72 * 0.671 + 6) * heightAdjustmentFactors[rotationIndex];
        yKeyPoints[0] = (72 * 0.455 + 8) * heightAdjustmentFactors[rotationIndex];
        zKeyPoints[4] = branchLength * 0.199;
        zKeyPoints[3] = branchLength * 0.13;
        zKeyPoints[2] = 0;
        zKeyPoints[1] = branchLength * (-0.08);
        zKeyPoints[0] = branchLength * (-0.05);
        List<Vec3D> _availableMountingPoints = new ArrayList<Vec3D>();
        LXTransform transform = new LXTransform();
        transform.rotateY(rotationalPosition * 45 * (Utils.PI / 180));
        double newX = xKeyPoints[0] + 2;
        while (newX < xKeyPoints[NUM_KEYPOINTS - 1]) {
            int keyPointIndex = 0;
            while (xKeyPoints[keyPointIndex] < newX && keyPointIndex < NUM_KEYPOINTS) {
                keyPointIndex++;
            }
            if (keyPointIndex < NUM_KEYPOINTS) {
                double ratio =
                        (newX - xKeyPoints[keyPointIndex - 1])
                                / (xKeyPoints[keyPointIndex] - xKeyPoints[keyPointIndex - 1]);
                double newY =
                        yKeyPoints[keyPointIndex - 1]
                                + ratio
                                        * (yKeyPoints[keyPointIndex]
                                                - yKeyPoints[keyPointIndex - 1])
                                + layerBaseHeight;
                double newZ =
                        zKeyPoints[keyPointIndex - 1]
                                + ratio
                                        * (zKeyPoints[keyPointIndex]
                                                - zKeyPoints[keyPointIndex - 1]);
                transform.push();
                transform.translate((float) newX, (float) newY, (float) newZ);
                _availableMountingPoints.add(
                        new Vec3D(transform.x(), transform.y(), transform.z()));
                transform.pop();
                transform.push();
                transform.translate((float) newX, (float) newY, (float) (-newZ));
                _availableMountingPoints.add(
                        new Vec3D(transform.x(), transform.y(), transform.z()));
                transform.pop();
            }
            newX += holeSpacing;
        }
        this.availableMountingPoints = Collections.unmodifiableList(_availableMountingPoints);
    }
}

class Cube extends BaseCube {
    public static final int[] PIXELS_PER_CUBE = {
        6, 6, 6, 12, 12
    }; // Tiny cubes actually have less, but for Entwined we want to
    // tell the NDB that everything is 6
    public static final float[] CUBE_SIZES = {4f, 7.5f, 11.25f, 15f, 16.5f};
    /** Size of this cube, one of SMALL/MEDIUM/LARGE/GIANT */
    public final float size;

    public final int pixels;
    public CubeConfig config = null;

    Cube(Vec3D globalPosition, Vec3D treePosition, CubeConfig config) {
        super(globalPosition, treePosition);
        this.size = CUBE_SIZES[config.cubeSizeIndex];
        this.pixels = PIXELS_PER_CUBE[config.cubeSizeIndex];
        this.config = config;
    }
}

abstract class Layer extends LXLayer {

    protected final Model model;

    Layer(LX lx) {
        super(lx);
        model = (Model) lx.model;
    }
}

abstract class ModelTransform extends Effect {
    ModelTransform(LX lx) {
        super(lx);
        model.addModelTransform(this);
        //           ((ShrubModel) shrubModel).addShrubModelTransform(this);
    }

    @Override
    public void run(double deltaMs) {}

    abstract void transform(Model model);
}

class ModelTransformTask implements LXLoopTask {

    protected final Model model;

    ModelTransformTask(Model model) {
        this.model = model;
    }

    @Override
    public void loop(double deltaMs) {
        model.runTransforms();
        //        model.runShrubTransforms();
    }
}

class Geometry {
    static final int INCHES = 1;
    static final int FEET = 12 * INCHES;
}
