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




// A mini cluster is a group of 12 LEDs about a foot tall and about a foot in radius.

// reminder
// class FairyCircleConfig  {
//   float x;
//   float z;
//   float ry;
//   float radius;
//   int cubeSizeIndex;
//   String[] ipAddresses;
//   String pieceId;
// }

// A mini cluster is as follows:
// 12 cubes on stalks. Radius of about 18 inches.
// The 12 cubes are organized with "1" as basically the shortest
// However, the cubes blink in the OPPOSITE direction - they
// are wired so that cube 12 blinks first and 1 last
// They are put into a fairy circle with stem ONE on the inside
// So cluster 1 has a rotation of 180 degrees and onward

class MiniCluster {

    // it seems the clusters all have the same radius - replace with actual
    // if they are notably different, then change to an array like heights
    static final float RADIUS = 18.0f;

    // the heights are different
    static final float HEIGHTS[] = { 
                                     12.0f, 14.0f, 14.0f, 16.0f,
                                     16.0f, 18.0f, 18.0f, 20.0f,
                                     20.0f, 18.0f, 18.0f, 16.0f,
                                     16.0f, 14.0f, 14.0f, 12.0f,
                                    };

    static final int N_CUBES = 12;

    final List<BaseCube> cubes;

    final int index; // for debuggng

    // positions: fcc has the fairy circle's center (fairyCirclePostion)
    //            the miniClusterPosition is relative to the fairyClusterPostion
    //            the cube position is relative to the miniCluster Position

    MiniCluster(Vec3D miniClusterPosition, float miniClusterRotation /*radians*/, FairyCircleConfig fcc, int sculptureIndex, int index) {

        this.index = index;
        List<BaseCube> _cubes = new ArrayList<BaseCube>();

        float rad_step = (float) Math.toRadians( 360.0f / N_CUBES );
        float rads = (float) miniClusterRotation; // start with the global rotation + local rotation, rotate about
        for (int i = 0; i < N_CUBES; i++ ) {
            Vec3D cubePosition = new Vec3D(); // relative to sculpture

            cubePosition.x = RADIUS * (float) Math.cos(rads);
            cubePosition.y = HEIGHTS[i];
            cubePosition.z = RADIUS * (float) Math.sin(rads);
            rads += rad_step;

            // add self reduces one garbage vec - reuses newly construced
            // Y coordinate (up) not specified in installation
            Vec3D globalPosition = (new Vec3D(fcc.x,0,fcc.z)).addSelf(miniClusterPosition).addSelf(cubePosition);
            Vec3D localPosition = miniClusterPosition.add(cubePosition);

            BaseCube cube = new BaseCube( globalPosition, localPosition, sculptureIndex, PieceType.FAIRY_CIRCLE, fcc.pieceId, fcc.cubeSizeIndex);

            _cubes.add(cube);

        }

        // Mini Shrubs are wired REVERSE - where "cube 12" lights up first.
        // here's where you do the reversal (hope this works)
        // Note to others: if you have some wired the other way, you can add a field to the fcc json struct,
        // and do or not do this
        Collections.reverse(_cubes);

        this.cubes = Collections.unmodifiableList(_cubes);
    }
}

class FairyCircleModel extends LXModel {

    /**
     * FairyCircles in the model
     */
    public final List<FairyCircle> fairyCircles;

    /**
     * Cubes in the model (starting with base cubes, not sure if I need to subclass...)
     */
    public final List<BaseCube> fairyCircleCubes;
    public final Map<String, BaseCube[]> fairyCircleIpMap = new HashMap<String, BaseCube[]>();

    private final ArrayList<Effect> fairyCircleModelTransforms = new ArrayList<>();
    private final List<FairyCircleConfig> fairyCircleConfigs;

    FairyCircleModel(List<FairyCircleConfig> fairyCircleConfigs) {
        super(new FairyCircleFixture(fairyCircleConfigs)); // construct all the fairy circles
        this.fairyCircleConfigs = fairyCircleConfigs;
        FairyCircleFixture f = (FairyCircleFixture) this.fixtures.get(0);
        List<BaseCube> _cubes = new ArrayList<BaseCube>();
        this.fairyCircles = Collections.unmodifiableList(f.fairyCircles);
        for (FairyCircle fairyCircle : this.fairyCircles) {
            fairyCircleIpMap.putAll(fairyCircle.ipMap);
            _cubes.addAll(fairyCircle.cubes);
        }
        this.fairyCircleCubes = Collections.unmodifiableList(_cubes);
    }

    private static class FairyCircleFixture extends LXAbstractFixture {

        final List<FairyCircle> fairyCircles = new ArrayList<FairyCircle>();

        private FairyCircleFixture(List<FairyCircleConfig> fairyCircleConfigs) {
            for (int i = 0; i < fairyCircleConfigs.size(); i++) {
                FairyCircleConfig fcc = fairyCircleConfigs.get(i);
                FairyCircle fc = new FairyCircle(fcc, i);
                fairyCircles.add(fc);
                points.addAll(fc.points);
            }
        }
    }

    public void addFairyCircleModelTransform(FairyCircleModelTransform fairyCircleModelTransform) {
        fairyCircleModelTransforms.add(fairyCircleModelTransform);
    }

    public void runFairyCircleTransforms() {
        for (BaseCube cube : fairyCircleCubes) {
            cube.resetTransform();
        }
        for (Effect modelTransform : fairyCircleModelTransforms) {
            FairyCircleModelTransform fairyCircleModelTransform = (FairyCircleModelTransform) modelTransform;
            if (fairyCircleModelTransform.isEnabled()) {
                fairyCircleModelTransform.transform(this);
            }
        }
        for (BaseCube cube : fairyCircleCubes) {
            cube.didTransform();
        }
    }

    public void addFairyCircleModelTransform(ModelTransform modelTransform) {
        addFairyCircleModelTransform(modelTransform);
    }
}

class FairyCircleConfig  {
  float x;
  float z;
  float ry;
  float radius;
  int cubeSizeIndex;
  String[] ipAddresses;
  String pieceId;
}

class FairyCircle extends LXModel {

    /**
     * NDBs in the FC
     */
    public final Map<String, BaseCube[]> ipMap;

    /**
     * Cubes in the FC
     */
    public final List<BaseCube> cubes;

    /**
     * MiniClusters in the FairyCircle
     */
    public final List<MiniCluster> miniClusters;

    /**
     * index of the fairy circle (in the configuration)
     */
    public final int index;

    /**
     * pieceId is used by the QR codes to fire up individual things
     */
    public final String pieceId;

    /**
     * x-position of center of fairy circle in global coords
     */
    public final float x;

    /**
     * z-position of center fairy circle in global coords
     */
    public final float z;

    /**
     * Rotation in degrees of fairy circle to global grid
     */
    public final float ry;


    FairyCircle(FairyCircleConfig fcc, int fairyCircleIndex) {
        super(new Fixture(fcc, fairyCircleIndex));
        Fixture f = (Fixture) this.fixtures.get(0);
        this.index = fairyCircleIndex;
        this.cubes = Collections.unmodifiableList(f.cubes);
        this.miniClusters = f.miniClusters;
        this.ipMap = f.ipMap;
        this.x = fcc.x;
        this.z = fcc.z;
        this.ry = fcc.ry;
        this.pieceId = fcc.pieceId;
        // Very useful print to see if I'm going the right directions
        //if (shrubIndex == 0) {
        //      for (ShrubCube cube : this.cubes) {
        //          System.out.println("si: "+cube.sculptureIndex+" idx: "+cube.index+" sx: "+cube.sx+" sy: "+cube.sy+" sz: "+cube.sz);
        //          System.out.println("    theta: "+cube.theta+" y: "+cube.y);
        //    }
        //}

    }

    public Vec3D transformPoint(Vec3D point) {
        return ((Fixture) this.fixtures.get(0)).transformPoint(point);
    }

    // Private Fairy Circle Fixture
    private static class Fixture extends LXAbstractFixture {

        List<BaseCube> cubes = new ArrayList<BaseCube>();
        List<MiniCluster> miniClusters = new ArrayList<MiniCluster>();
        public final Map<String, BaseCube[]> ipMap = new HashMap<String, BaseCube[]>();
        public final LXTransform fairyCircleTransform;
        int N_MINICLUSTERS;
        final static int MINICLUSTERS_PER_NDB = 5;

        // Fairy Circle Fixture constructor
        Fixture(FairyCircleConfig fcc, int fairyCircleIndex) {

            // honestly I'm a little confused about this - what is the transform going to do
            // and should it apply the same to all in the fairy circle or be compounded to the mini
            fairyCircleTransform = new LXTransform();
            fairyCircleTransform.translate(fcc.x, 0, fcc.z);
            fairyCircleTransform.rotateY(fcc.ry * Utils.PI / 180);

            // always 5 minis per NDB
            int N_MINICLUSTERS = fcc.ipAddresses.length * MINICLUSTERS_PER_NDB;

            float rad_step = (float) Math.toRadians( 360.0f / N_MINICLUSTERS );
            float rads = (float) Math.toRadians(fcc.ry); // start with the global rotation, rotate about
            float miniClusterRotation = (float) Math.PI; // number 1 is inside
            for (int i = 0; i < N_MINICLUSTERS; i++ ) {

                Vec3D miniClusterPosition = new Vec3D(); // relative to sculpture

                // position of the minicluster relative to fairyCircle center
                miniClusterPosition.x = fcc.radius * (float) Math.cos(rads);
                miniClusterPosition.y = 0;
                miniClusterPosition.z = fcc.radius * (float) Math.sin(rads);

                MiniCluster miniCluster = new MiniCluster(miniClusterPosition, miniClusterRotation, fcc, fairyCircleIndex, i);

                rads += rad_step;
                miniClusterRotation += rad_step;

                miniClusters.add(miniCluster);

                cubes.addAll(miniCluster.cubes);

            }

            // Construct the NDB map
            // this becomes the output map for the NDB - so the cubes have to be in the correct order.
            // Order is a little weird. This is to have good wiring.
            // minicluster 1 - 13-24
            // minicluster 2 - 1-12
            // minicluster 3 - 25
            // minicluster 4 - 37
            // minicluster 5 - 49
            // (these are in the 1's indexed form used by the NDB)
            //
            // NDBs have to be listed in the config file in the correct order
            // have to have exactly 5 minis per NDB

            final int[] miniClusterOffset = {12, 0, 24, 36, 48}; // where the miniCluster is in the NDB - zero indexed!
            final int CUBES_PER_NDB = 12 * MINICLUSTERS_PER_NDB;
            int miniClusterIndex = 0;

            int n_ipAddresses = fcc.ipAddresses.length;
            for (int i=0; i < n_ipAddresses; i++) {

                // these go backward I think???
                String ipAddress = fcc.ipAddresses[ n_ipAddresses - i - 1 ];

                BaseCube[] ndbCubes = new BaseCube[CUBES_PER_NDB];

                // walk through the next 5 miniclusters, gather its cubes and put them right
                for (int j=0; j < MINICLUSTERS_PER_NDB; j++) {
                    MiniCluster mc = miniClusters.get(miniClusterIndex);
                    for (int cubeIndex=0; cubeIndex < mc.cubes.size(); cubeIndex++) {
                        ndbCubes[miniClusterOffset[j] + cubeIndex] = mc.cubes.get(cubeIndex); // insert its cubes at the right offset
                    }
                    miniClusterIndex++;
                }
                ipMap.put(ipAddress, ndbCubes);
            }

            // add all the points to the fixture
            for (BaseCube cube : this.cubes) {
                for (LXPoint p : cube.points) {
                    this.points.add(p);
                }
            }
        }

        public Vec3D transformPoint(Vec3D point) {
            this.fairyCircleTransform.push();
            this.fairyCircleTransform.translate(point.x, point.y, point.z);
            Vec3D result = new Vec3D(this.fairyCircleTransform.x(), this.fairyCircleTransform.y(), this.fairyCircleTransform.z());
            this.fairyCircleTransform.pop();
            return result;
        }
    }
}


abstract class FairyCircleLayer extends LXLayer {

    protected final FairyCircleModel model;

    FairyCircleLayer(LX lx) {
        super(lx);
        model = (FairyCircleModel) lx.model;
    }
}

abstract class FairyCircleModelTransform extends Effect {
    FairyCircleModelTransform(LX lx) {
        super(lx);

        model.addFairyCircleModelTransform(this);
    }

    @Override
    public void run(double deltaMs) {
    }

    abstract void transform(LXModel lxModel);
}

class FairyCircleModelTransformTask implements LXLoopTask {

    protected final FairyCircleModel model;

    FairyCircleModelTransformTask(FairyCircleModel model) {
        this.model = model;
    }

    @Override
    public void loop(double deltaMs) {
        model.runFairyCircleTransforms();
    }
}
