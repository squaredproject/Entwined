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



// A Spot is a Spotlight. It will be controlled by a single LED element
// on an NDB. It has the X,Y,Z position of its center
// in the graphical representation, there will be a H, W, L, but this is only used to look good in the simulator

// reminder
// class SpotConfig  {
//   // center of the lightfield (used to calculate color)
//   float x; // side to side
//   float z; // front to back
//   float y; // height from ground (not rotation like everything else!)
//   // I would prefer to have full H, W, D, but let's just do size in inches for now
//   float size;
//   // do we need to rotate them?
//   String ipAddress;
//   String pieceId;
// }


// SO... it appears that the above level code creates
// spots directly from the config file, instead of creating a SpotModel.
// This is a little unfortunate, because we need to go back
// and patch up the ipMaps, this code would do it
// THis is untested since it hasn't been called anywhere and probably could/should be removed
// execpt there's a bunch of code about transforms.

class SpotModel extends LXModel {

    /**
     * Spots in the model
     */
    public final List<Spot> spots;

    /**
     * Cubes in the model (starting with base cubes, not sure if I need to subclass...)
     */
    public final List<BaseCube> spotCubes;
    public final Map<String, BaseCube[]> spotIpMap = new HashMap<String, BaseCube[]>();

    private final ArrayList<Effect> spotModelTransforms = new ArrayList<>();
    private final List<SpotConfig> spotConfigs;

    SpotModel(List<SpotConfig> spotConfigs) {
        super(new SpotFixture(spotConfigs)); // construct all the fairy circles

        System.out.println("CTOR Spotmodel: nconfigs is "+spotConfigs.size());

        this.spotConfigs = spotConfigs;
        SpotFixture f = (SpotFixture) this.fixtures.get(0);
        List<BaseCube> _cubes = new ArrayList<BaseCube>();
        this.spots = Collections.unmodifiableList(f.spots);
        for (Spot spot : this.spots) {
            spotIpMap.putAll(spot.ipMap);
            _cubes.addAll(spot.cubes);
        }
        this.spotCubes = Collections.unmodifiableList(_cubes);
    }

    private static class SpotFixture extends LXAbstractFixture {

        final List<Spot> spots = new ArrayList<Spot>();

        private SpotFixture(List<SpotConfig> spotConfigs) {
            for (int i = 0; i < spotConfigs.size(); i++) {
                SpotConfig sc = spotConfigs.get(i);
                Spot s = new Spot(sc, i);
                spots.add(s);
                points.addAll(s.points);
            }
        }
    }

    public void addSpotModelTransform(SpotModelTransform spotModelTransform) {
        spotModelTransforms.add(spotModelTransform);
    }

    public void runSpotTransforms() {
        for (BaseCube cube : spotCubes) {
            cube.resetTransform();
        }
        for (Effect modelTransform : spotModelTransforms) {
            SpotModelTransform spotModelTransform = (SpotModelTransform) modelTransform;
            if (spotModelTransform.isEnabled()) {
                spotModelTransform.transform(this);
            }
        }
        for (BaseCube cube : spotCubes) {
            cube.didTransform();
        }
    }

    public void addSpotModelTransform(ModelTransform modelTransform) {
        addSpotModelTransform(modelTransform);
    }
}


class SpotCube extends BaseCube {
    public final String ipAddress;
    public final int ndbOffset;

    SpotCube(Vec3D globalPosition, float size, int spotIndex, String pieceId, int ndbOffset, String ipAddress) {

        super( globalPosition,  new Vec3D(), size, spotIndex, PieceType.SPOT, pieceId, 1 /* pixels: always new cubes */ );

        this.ipAddress = ipAddress;
        this.ndbOffset = ndbOffset;
    }
}


// This represents the JSON in the config file
// it's written to be easiest to configure
class SpotConfig  {
  // center of the place that will be lit up (used to calculate color)
  float     x; // side to side
  float     z; // front to back
  float     y; // height from ground (not rotation like everything else!)
  // I would prefer to have full H, W, D, but let's just do size in inches for now
  float     size;
  // The offset within the NDB array. 0 index I suspect
  String    ipAddress; 
  int       ndbOffset;
  //
  String    pieceId;
  int       spotIndex;
}




// There is one model per spot?

class Spot extends LXModel {

    /**
     * NDBs controlling Spots
     */
    public final Map<String, SpotCube[]> ipMap;

    /**
     * "Cubes" which are actually spots
     */
    public final List<BaseCube> cubes;

    /**
     * pieceId is used by the QR codes to fire up individual things
     */
    public final String pieceId;

    /**
     * index where the spot is in the list spots, some patterns might like it
     */
    public final int spotIndex;


    Spot(SpotConfig sc, int index) {
        super(new Fixture(sc, index));

        System.out.println(" CTOR for a spot ");

        Fixture f = (Fixture) this.fixtures.get(0);
        this.cubes = Collections.unmodifiableList(f.cubes);
        this.pieceId = sc.pieceId;
        this.spotIndex = index;
        this.ipMap = f.spotIpMap;
        // Very useful print to see if I'm going the right directions
        //if (shrubIndex == 0) {
        //      for (ShrubCube cube : this.cubes) {
        //          System.out.println("si: "+cube.sculptureIndex+" idx: "+cube.index+" sx: "+cube.sx+" sy: "+cube.sy+" sz: "+cube.sz);
        //          System.out.println("    theta: "+cube.theta+" y: "+cube.y);
        //    }
        //}

    }

    // since different spots share the same IP address, we have to basically merge
    // the ipmaps of the spots - create a single map, with the spots in the
    // right order. Will use a simple two-pass because we have to figure out the
    // sizes of the cube arrays. Let's try not making it n2 thou

    public static Map<String, BaseCube[]> spotsIpMapGet(List<Spot> spots ) {
        Map<String, BaseCube[]> ipMap = new HashMap<String, BaseCube[]>();
        Map<String, Integer> ipSizeMap = new HashMap<String, Integer>();

        // System.out.printf(" spotsIpMapGet: \n");

        // find all the sizes (max ndbOffset+1)
        for (Spot spot : spots) {
            // this could be simplified because we know there is only one cube
            // and one IP address, but we're keeping with other patterns
            for (Map.Entry<String, SpotCube[]> entry : spot.ipMap.entrySet() ) {
                String ipAddress = entry.getKey();
                // System.out.printf(" first pass: ip %s\n",ipAddress);
                for (SpotCube sc : entry.getValue()) {
                    if (! ipSizeMap.containsKey(ipAddress)) {
                        // System.out.printf(" setting size for %s to %d\n",ipAddress,sc.ndbOffset);
                        ipSizeMap.put(ipAddress, new Integer(sc.ndbOffset));
                    }
                    else {
                        Integer _i = ipSizeMap.get(ipAddress);
                        if (sc.ndbOffset > _i) {
                            // System.out.printf(" resetting size for %s to %d\n",ipAddress,sc.ndbOffset);
                            ipSizeMap.replace(ipAddress, new Integer(sc.ndbOffset));
                        }
                    }
                }
            }
        }
        // construct the output map
        for (Map.Entry<String, Integer> entry : ipSizeMap.entrySet()) {
            ipMap.put(entry.getKey(), new BaseCube[ entry.getValue() + 1 ] );
        }

        // fill the output map
        for (Spot spot : spots) {
            for (Map.Entry<String, SpotCube[]> entry : spot.ipMap.entrySet() ) {
                String ipAddress = entry.getKey();
                BaseCube[] baseCubes = ipMap.get(ipAddress);
                for (SpotCube sc : entry.getValue()) {
                    // System.out.printf(" adding spot %s ip %s ndbOff %d\n",spot.pieceId,ipAddress,sc.ndbOffset);
                    baseCubes[sc.ndbOffset] = (BaseCube) sc;
                }
            }
        }

        return ipMap;
    }

    public Vec3D transformPoint(Vec3D point) {
        return ((Fixture) this.fixtures.get(0)).transformPoint(point);
    }

    // Private Spot Fixture
    private static class Fixture extends LXAbstractFixture {

        List<BaseCube> cubes = new ArrayList<BaseCube>();
        public final Map<String, SpotCube[]> spotIpMap = new HashMap<String, SpotCube[]>();
        public final LXTransform spotTransform;

        // Spot Fixture constructor
        Fixture(SpotConfig sc, int spotIndex) {

            // honestly I'm a little confused about this - what is the transform going to do
            // and should it apply the same to all in the fairy circle or be compounded to the mini
            spotTransform = new LXTransform();
            spotTransform.translate(sc.x, 0, sc.z);
            //spotTransform.rotateY(sc.ry * Utils.PI / 180);

            // Spot is the simplest thing: a cube at this location
            Vec3D globalPosition = new Vec3D();
            globalPosition.x = sc.x;
            globalPosition.y = sc.y;
            globalPosition.z = sc.z;

            SpotCube cube = new SpotCube(globalPosition, sc.size, spotIndex, sc.pieceId, sc.ndbOffset, sc.ipAddress);
            // the cube has the ipAddress and the ndbOffset so we can construct the complete ipMap later

            // set the offset directly here, we will reconstruct the IP larger IP map based on that
            cubes.add( cube );

            // Construct the NDB map for this spot.
            // It will be all merged together to form the global IP map.
            SpotCube[] ndbCubes = new SpotCube[1];
            ndbCubes[0] = cube;
            spotIpMap.put(sc.ipAddress, ndbCubes);


            // final int[] miniClusterOffset = {12, 0, 24, 36, 48}; // where the miniCluster is in the NDB - zero indexed!
            // final int CUBES_PER_NDB = 12 * MINICLUSTERS_PER_NDB;
            // int miniClusterIndex = 0;

            // int n_ipAddresses = sc.ipAddresses.length;
            // for (int i=0; i < n_ipAddresses; i++) {

            //     // these go backward I think???
            //     String ipAddress = sc.ipAddresses[ n_ipAddresses - i - 1 ];

            //     BaseCube[] ndbCubes = new BaseCube[CUBES_PER_NDB];

            //     // walk through the next 5 miniclusters, gather its cubes and put them right
            //     for (int j=0; j < MINICLUSTERS_PER_NDB; j++) {
            //         MiniCluster mc = miniClusters.get(miniClusterIndex);
            //         for (int cubeIndex=0; cubeIndex < mc.cubes.size(); cubeIndex++) {
            //             ndbCubes[miniClusterOffset[j] + cubeIndex] = mc.cubes.get(cubeIndex); // insert its cubes at the right offset
            //         }
            //         miniClusterIndex++;
            //     }
            //     ipMap.put(ipAddress, ndbCubes);
            // }

            // add all the points to the fixture
            for (LXPoint p : cube.points) {
                this.points.add(p);
            }
            // for (BaseCube cube : this.cubes) {
            //     for (LXPoint p : cube.points) {
            //         this.points.add(p);
            //     }
            // }
        }

        public Vec3D transformPoint(Vec3D point) {
            this.spotTransform.push();
            this.spotTransform.translate(point.x, point.y, point.z);
            Vec3D result = new Vec3D(this.spotTransform.x(), this.spotTransform.y(), this.spotTransform.z());
            this.spotTransform.pop();
            return result;
        }
    }
}


abstract class SpotLayer extends LXLayer {

    protected final SpotModel model;

    SpotLayer(LX lx) {
        super(lx);
        model = (SpotModel) lx.model;
    }
}

abstract class SpotModelTransform extends Effect {
    SpotModelTransform(LX lx) {
        super(lx);

        model.addSpotModelTransform(this);
    }

    @Override
    public void run(double deltaMs) {
    }

    abstract void transform(LXModel lxModel);
}

class SpotModelTransformTask implements LXLoopTask {

    protected final SpotModel model;

    SpotModelTransformTask(SpotModel model) {
        this.model = model;
    }

    @Override
    public void loop(double deltaMs) {
        model.runSpotTransforms();
    }
}
