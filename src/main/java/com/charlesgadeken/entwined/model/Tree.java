package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.config.TreeCubeConfig;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toxi.geom.Vec3D;

public class Tree extends LXModelInterceptor {

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

    private final LX lx;

    public Tree(
            LX lx,
            List<TreeCubeConfig> cubeConfig,
            int treeIndex,
            float x,
            float z,
            float ry,
            int[] canopyMajorLengths,
            int[] layerBaseHeights) {
        super(
                new Fixture(
                        lx, cubeConfig, treeIndex, x, z, ry, canopyMajorLengths, layerBaseHeights));
        this.lx = lx;

        Fixture f = (Fixture) this.getFixture();
        this.index = treeIndex;
        this.cubes = Collections.unmodifiableList(f.cubes);
        this.treeLayers = f.treeLayers;
        this.ipMap = f.ipMap;
        this.x = x;
        this.z = z;
        this.ry = ry;
    }

    public Vec3D transformPoint(Vec3D point) {
        return ((Fixture) this.lx.structure.fixtures.get(0)).transformPoint(point);
    }

    private static class Fixture extends PseudoAbstractFixture {

        final List<Cube> cubes = new ArrayList<>();
        final List<EntwinedLayer> treeLayers = new ArrayList<>();
        public final Map<String, Cube[]> ipMap = new HashMap<>();
        public final LXTransform transform;
        public final List<TreeCubeConfig> inactiveCubeConfigs = new ArrayList<>();

        Fixture(
                LX lx,
                List<TreeCubeConfig> cubeConfig,
                int treeIndex,
                float x,
                float z,
                float ry,
                int[] canopyMajorLengths,
                int[] layerBaseHeights) {
            super(lx, "Tree");
            transform = new LXTransform();
            transform.translate(x, 0, z);
            transform.rotateY(ry * Utilities.PI / 180);
            for (int i = 0; i < canopyMajorLengths.length; i++) {
                treeLayers.add(new EntwinedLayer(canopyMajorLengths[i], i, layerBaseHeights[i]));
            }
            for (TreeCubeConfig cc : cubeConfig) {
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
                    if (ndbCubes[i] == null) {
                        // fill all empty outputs with an inactive cube. Maybe this
                        // would be nicer to do at
                        // the model level in the future.
                        TreeCubeConfig cc = new TreeCubeConfig();
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
            List<LXPoint> pts = new ArrayList<>();
            for (Cube cube : this.cubes) {
                Collections.addAll(pts, cube.points);
            }
            this.setPoints(pts);
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
