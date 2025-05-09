import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

import heronarts.lx.LX;
import heronarts.lx.LXLayer;
import heronarts.lx.LXLoopTask;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;


class Rod {

    public Vec3D mountingPoint;
    private double xKeyPoint;
    private double yKeyPoint;
    private double zKeyPoint;

    Rod(int rodPosition, double rodLength, int clusterIndex, double clusterMinRodLength) {
        yKeyPoint = rodLength;

        switch (rodPosition) {
            // looking at cluster from the center of shrub
            case 0: // longest, right
                xKeyPoint = 2;
                zKeyPoint = clusterMinRodLength * 1.25;
                break;
            case 1: // left
                xKeyPoint = -2;
                zKeyPoint = clusterMinRodLength * 1.2;
                break;
            case 2: //right
                xKeyPoint = 2;
                zKeyPoint = clusterMinRodLength;
                break;
            case 3: // left
                xKeyPoint = -2;
                zKeyPoint = clusterMinRodLength * .9;
                break;
            case 4: // shortest, center
                xKeyPoint = 0;
                zKeyPoint = clusterMinRodLength * .7;
                break;
            default:
                break;
        }

        LXTransform transform = new LXTransform();

        // A -> 0, 1
        // B -> 2, 3, 10, 11
        // C -> 4, 5, 8, 9
        // D -> 6, 7
        
        // 0 -> 2 * -0.5236
        // 1 -> 1 * -0.5236
        // 2 -> 0 * -0.5236
        // 3 -> -1 * -0.5236
        // 4 -> -2 * -0.5236
        transform.rotateY((clusterIndex + 1) * -0.5236);

        transform.push();
        transform.translate((float) xKeyPoint, (float) yKeyPoint, (float) zKeyPoint);
        this.mountingPoint = new Vec3D(transform.x(), transform.y(), transform.z());
        transform.pop();

    }

}

class ShrubCluster {
    List<Rod> rods;

    ShrubCluster(String type /* type of shrub */, int clusterIndex) {
        List<Rod> _rods = new ArrayList<Rod>();
        int rodPositions[] = new int[]{4, 3, 2, 1, 0};

        double clusterRodLengths[] = null;

        switch (clusterIndex) {
            // clockwise, starting at the longest left-most cluster

            // A -> 0, 1
            // B -> 2, 3, 10, 11
            // C -> 4, 5, 8, 9
            // D -> 6, 7
            case 0:
            case 1:
                if (type != null && type.equals("king")) {
                    clusterRodLengths = new double[]{31*2, 36.5*2, 40*2, 46*2, 51*2};
                }
                else {
                    clusterRodLengths = new double[]{31, 36.5, 40, 46, 51};                    
                }
                break;
            case 2:
            case 3:
            case 10:
            case 11:
                if (type != null && type.equals("king")) {
                    clusterRodLengths = new double[]{28*2, 33*2, 36.5*2, 41*2, 46*2};
                }
                else {
                    clusterRodLengths = new double[]{28, 33, 36.5, 41, 46};
                }
                break;
            case 4:
            case 5:
            case 8:
            case 9:
                if (type != null && type.equals("king")) {
                    clusterRodLengths = new double[]{24*2, 29*2, 33*2, 37.5*2, 43*2};
                }
                else {
                    clusterRodLengths = new double[]{24, 29, 33, 37.5, 43};
                }
                break;
            case 6:
            case 7:
                if (type != null && type.equals("king")) {
                    clusterRodLengths = new double[]{21*2, 26*2, 30*2, 36*2, 40.5*2};
                }
                else {
                    clusterRodLengths = new double[]{24, 29, 33, 37.5, 43};
                }
                break;
            default:
        }
        for (int i = 0; i < rodPositions.length; i++) {
            Rod p = new Rod(rodPositions[i], clusterRodLengths[i], clusterIndex, clusterRodLengths[0]);
            _rods.add(p);
        }
        this.rods = Collections.unmodifiableList(_rods);
    }
}

class ShrubModel extends LXModel {

    /**
     * Shrubs in the model
     */
    public final List<Shrub> shrubs;

    /**
     * ShrubCubes in the model
     */
    public final List<ShrubCube> shrubCubes;
    public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<String, ShrubCube[]>();

    private final ArrayList<Effect> shrubModelTransforms = new ArrayList<>();
    private final List<ShrubConfig> shrubConfigs;

    ShrubModel(List<ShrubConfig> shrubConfigs, List<ShrubCubeConfig> shrubCubeConfig) {
        super(new ShrubFixture(shrubConfigs, shrubCubeConfig));
        this.shrubConfigs = shrubConfigs;
        ShrubFixture f = (ShrubFixture) this.fixtures.get(0);
        List<ShrubCube> _cubes = new ArrayList<ShrubCube>();
        this.shrubs = Collections.unmodifiableList(f.shrubs);
        for (Shrub shrub : this.shrubs) {
            shrubIpMap.putAll(shrub.ipMap);
            _cubes.addAll(shrub.cubes);
        }
        this.shrubCubes = Collections.unmodifiableList(_cubes);
    }

    private static class ShrubFixture extends LXAbstractFixture {

        final List<Shrub> shrubs = new ArrayList<Shrub>();

        private ShrubFixture(List<ShrubConfig> shrubConfigs, List<ShrubCubeConfig> shrubCubeConfigs) {
            for (int i = 0; i < shrubConfigs.size(); i++) {
                ShrubConfig sc = shrubConfigs.get(i);
                shrubs.add(new Shrub(sc, shrubCubeConfigs, i));
            }
            for (Shrub shrub : shrubs) {
                for (LXPoint p : shrub.points) {
                    points.add(p);
                }
            }
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

    public void addShrubModelTransform(ShrubModelTransform shrubModelTransform) {
        shrubModelTransforms.add(shrubModelTransform);
    }

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

    public void addShrubModelTransform(ModelTransform modelTransform) {
        addShrubModelTransform(modelTransform);
    }
}

class ShrubConfig  {
  float x;
  float z;
  float ry;
  String type;
  int cubeSizeIndex;
  String pieceId;
  String shrubIpAddress;
}

class Shrub extends LXModel {

    /**
     * NDBs in the shrub
     */
    public final Map<String, ShrubCube[]> ipMap;

    /**
     * Cubes in the shrub
     */
    public final List<ShrubCube> cubes;

    /**
     * Clusters in the shrub
     */
    public final List<ShrubCluster> shrubClusters;

    /**
     * index of the shrub
     */
    public final int index;

    /**
     * pieceId is used by the QR codes to fire up individual things
     */
    public final String pieceId;

    /**
     * x-position of center of base of shrub
     */
    public final float x;

    /**
     * z-position of center of base of shrub
     */
    public final float z;

    /**
     * Rotation in degrees of shrub about vertical y-axis
     */
    public final float ry;


    Shrub(ShrubConfig shrubConfig, List<ShrubCubeConfig> shrubCubeConfig, int shrubIndex) {
        super(new Fixture(shrubConfig, shrubCubeConfig, shrubIndex));
        Fixture f = (Fixture) this.fixtures.get(0);
        this.index = shrubIndex;
        this.cubes = Collections.unmodifiableList(f.shrubCubes);
        this.shrubClusters = f.shrubClusters;
        this.ipMap = f.shrubIpMap;
        this.x = shrubConfig.x;
        this.z = shrubConfig.z;
        this.ry = shrubConfig.ry;
        this.pieceId = shrubConfig.pieceId;
    }

    public Vec3D transformPoint(Vec3D point) {
        return ((Fixture) this.fixtures.get(0)).transformPoint(point);
    }

    private static class Fixture extends LXAbstractFixture {

        final List<ShrubCube> shrubCubes = new ArrayList<ShrubCube>();
        final List<ShrubCluster> shrubClusters = new ArrayList<ShrubCluster>();
        public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<String, ShrubCube[]>();
        public final LXTransform shrubTransform;
        int NUM_CLUSTERS_IN_SHRUB = 12;

        Fixture(ShrubConfig shrubConfig, List<ShrubCubeConfig> shrubCubeConfig, int shrubIndex) {
            shrubTransform = new LXTransform();
            shrubTransform.translate(shrubConfig.x, 0, shrubConfig.z);
            shrubTransform.rotateY(shrubConfig.ry * Utils.PI / 180);

            for (int i = 0; i < NUM_CLUSTERS_IN_SHRUB; i++) {
                shrubClusters.add(new ShrubCluster(shrubConfig.type, i));
            }
            for (ShrubCubeConfig cc : shrubCubeConfig) {
                if (cc.shrubIndex == shrubIndex) {
                    Vec3D p;
                    try {
                        p = shrubClusters.get(cc.clusterIndex).rods.get(cc.rodIndex - 1).mountingPoint;
                        //                        System.out.println(cc.rodIndex);

                    } catch (Exception e) {
                        System.out.println("Error loading config point");
                        System.out.println(e);
                        p = null;
                    }
                    if (p != null) {
                        ShrubCube cube = new ShrubCube(this.transformPoint(p), p, cc, shrubConfig.pieceId);
                        shrubCubes.add(cube);
                        if (!shrubIpMap.containsKey(cc.shrubIpAddress)) {
                            shrubIpMap.put(cc.shrubIpAddress, new ShrubCube[60]);
                        }
                        ShrubCube[] ndbCubes = shrubIpMap.get(cc.shrubIpAddress);
                        //                        System.out.println(cc.shrubIpAddress);
                        ndbCubes[cc.outputIndex] = cube;
                    }
                }
            }
            for (Map.Entry<String, ShrubCube[]> entry : shrubIpMap.entrySet()) {
                String ip = entry.getKey();
                ShrubCube[] ndbCubes = entry.getValue();
                for (int i = 0; i < 16; i++) {
                    if (ndbCubes[i] == null) { // fill all empty outputs with an inactive cube. Maybe this would be nicer to do at
                        // the model level in the future.
                        ShrubCubeConfig cc = new ShrubCubeConfig();
                        cc.shrubIndex = shrubIndex;
                        cc.rodIndex = 0;
                        cc.cubeSizeIndex = 0;
                        cc.outputIndex = i;
                        cc.clusterIndex = 0;
                        cc.shrubIpAddress = ip;
                        ShrubCube cube = new ShrubCube(new Vec3D(0, 0, 0), new Vec3D(0, 0, 0), cc, shrubConfig.pieceId);
                        shrubCubes.add(cube);
                        ndbCubes[i] = cube;
                    }
                }
            }
            for (ShrubCube cube : this.shrubCubes) {
                for (LXPoint p : cube.points) {
                    this.points.add(p);
                }
            }
        }

        public Vec3D transformPoint(Vec3D point) {
            this.shrubTransform.push();
            this.shrubTransform.translate(point.x, point.y, point.z);
            Vec3D result = new Vec3D(this.shrubTransform.x(), this.shrubTransform.y(), this.shrubTransform.z());
            this.shrubTransform.pop();
            return result;
        }
    }
}

class ShrubCube extends BaseCube {

    public ShrubCubeConfig config = null;

    ShrubCube(Vec3D globalPosition, Vec3D sculpturePosition, ShrubCubeConfig config, String pieceId) {
        super( globalPosition,  sculpturePosition, config.shrubIndex, config.pieceType, pieceId, config.cubeSizeIndex);

        this.config = config;
    }
}

// 
class ShrubCubeConfig {
    int shrubIndex; // Shrubindex no longer maps to an IP address. We use python to figure out the IP address and attach to every cube.
    int clusterIndex;
    int rodIndex;
    PieceType pieceType = PieceType.SHRUB;

    int outputIndex;
    int cubeSizeIndex;
    String shrubIpAddress;
}

abstract class ShrubLayer extends LXLayer {

    protected final ShrubModel model;

    ShrubLayer(LX lx) {
        super(lx);
        model = (ShrubModel) lx.model;
    }
}

abstract class ShrubModelTransform extends Effect {
    ShrubModelTransform(LX lx) {
        super(lx);

        model.addShrubModelTransform(this);
    }

    @Override
    public void run(double deltaMs) {
    }

    abstract void transform(LXModel lxModel);
}

class ShrubModelTransformTask implements LXLoopTask {

    protected final ShrubModel model;

    ShrubModelTransformTask(ShrubModel model) {
        this.model = model;
    }

    @Override
    public void loop(double deltaMs) {
        model.runShrubTransforms();
    }
}
