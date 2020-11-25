package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.config.ShrubCubeConfig;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toxi.geom.Vec3D;

public class Shrub extends LXModelInterceptor {
    /** NDBs in the shrub */
    public final Map<String, ShrubCube[]> ipMap;

    /** Cubes in the shrub */
    public final List<ShrubCube> cubes;

    /** Clusters in the shrub */
    public final List<EntwinedCluster> shrubClusters;

    /** index of the shrub */
    public final int index;

    /** x-position of center of base of shrub */
    public final float x;

    /** z-position of center of base of shrub */
    public final float z;

    /** Rotation in degrees of shrub about vertical y-axis */
    public final float ry;

    private final LX lx;

    Shrub(
            LX lx,
            List<ShrubCubeConfig> shrubCubeConfig,
            int shrubIndex,
            float x,
            float z,
            float ry) {
        super(new Fixture(lx, shrubCubeConfig, shrubIndex, x, z, ry));
        this.lx = lx;
        Fixture f = (Fixture) this.getFixture();
        this.index = shrubIndex;
        this.cubes = Collections.unmodifiableList(f.shrubCubes);
        this.shrubClusters = f.shrubClusters;
        this.ipMap = f.shrubIpMap;
        this.x = x;
        this.z = z;
        this.ry = ry;
    }

    public Vec3D transformPoint(Vec3D point) {
        return ((Fixture) this.lx.structure.fixtures.get(0)).transformPoint(point);
    }

    protected static class Fixture extends PseudoAbstractFixture {
        final List<ShrubCube> shrubCubes = new ArrayList<>();
        final List<EntwinedCluster> shrubClusters = new ArrayList<>();
        public final LX lx;
        public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<>();
        public final LXTransform shrubTransform;
        int NUM_CLUSTERS_IN_SHRUB = 12;

        Fixture(
                LX lx,
                List<ShrubCubeConfig> shrubCubeConfig,
                int shrubIndex,
                float x,
                float z,
                float ry) {
            super(lx, "Shrub");
            this.lx = lx;
            shrubTransform = new LXTransform();
            shrubTransform.translate(x, 0, z);
            shrubTransform.rotateY(ry * Utilities.PI / 180);
            for (int i = 0; i < NUM_CLUSTERS_IN_SHRUB; i++) {
                shrubClusters.add(new EntwinedCluster(i));
            }
            for (ShrubCubeConfig cc : shrubCubeConfig) {
                if (cc.shrubIndex == shrubIndex) {
                    Vec3D p;
                    try {
                        p =
                                shrubClusters
                                        .get(cc.clusterIndex)
                                        .rods
                                        .get(cc.rodIndex - 1)
                                        .mountingPoint;
                        //                        System.out.println(cc.rodIndex);

                    } catch (Exception e) {
                        System.out.println("Error loading config point");
                        System.out.println(e);
                        p = null;
                    }
                    if (p != null) {
                        ShrubCube cube = new ShrubCube(this.transformPoint(p), p, cc);
                        shrubCubes.add(cube);
                        if (!shrubIpMap.containsKey(cc.shrubIpAddress)) {
                            shrubIpMap.put(cc.shrubIpAddress, new ShrubCube[60]);
                        }
                        ShrubCube[] ndbCubes = shrubIpMap.get(cc.shrubIpAddress);
                        //                        System.out.println(cc.shrubIpAddress);
                        ndbCubes[cc.shrubOutputIndex] = cube;
                    }
                }
            }
            for (Map.Entry<String, ShrubCube[]> entry : shrubIpMap.entrySet()) {
                String ip = entry.getKey();
                ShrubCube[] ndbCubes = entry.getValue();
                for (int i = 0; i < 16; i++) {
                    if (ndbCubes[i]
                            == null) { // fill all empty outputs with an inactive cube. Maybe this
                        // would be nicer to do at
                        // the model level in the future.
                        ShrubCubeConfig cc = new ShrubCubeConfig();
                        cc.shrubIndex = shrubIndex;
                        cc.rodIndex = 0;
                        cc.cubeSizeIndex = 0;
                        cc.shrubOutputIndex = i;
                        cc.clusterIndex = 0;
                        cc.shrubIpAddress = ip;
                        ShrubCube cube = new ShrubCube(new Vec3D(0, 0, 0), new Vec3D(0, 0, 0), cc);
                        shrubCubes.add(cube);
                        ndbCubes[i] = cube;
                    }
                }
            }

            List<LXPoint> pts = new ArrayList<>();
            for (ShrubCube cube : this.shrubCubes) {
                Collections.addAll(pts, cube.points);
            }
            this.setPoints(pts);
        }

        public Vec3D transformPoint(Vec3D point) {
            this.shrubTransform.push();
            this.shrubTransform.translate(point.x, point.y, point.z);
            Vec3D result =
                    new Vec3D(
                            this.shrubTransform.x(),
                            this.shrubTransform.y(),
                            this.shrubTransform.z());
            this.shrubTransform.pop();
            return result;
        }
    }
}
