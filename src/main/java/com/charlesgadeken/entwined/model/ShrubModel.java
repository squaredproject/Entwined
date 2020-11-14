package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.model.config.ShrubConfig;
import com.charlesgadeken.entwined.model.config.ShrubCubeConfig;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import toxi.geom.Vec3D;

public class ShrubModel extends LXModelInterceptor {

    /** Shrubs in the model */
    public final List<Shrub> shrubs;

    /** ShrubCubes in the model */
    public final List<ShrubCube> shrubCubes;

    public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<String, ShrubCube[]>();

    private final ArrayList<Effect> shrubModelTransforms = new ArrayList<>();
    private final List<ShrubConfig> shrubConfigs;

    ShrubModel(LX lx, List<ShrubConfig> shrubConfigs, List<ShrubCubeConfig> shrubCubeConfig) {
        super(new ShrubFixture(lx, shrubConfigs, shrubCubeConfig));
        this.shrubConfigs = shrubConfigs;
        ShrubFixture f = (ShrubFixture) this.getFixture();
        List<ShrubCube> _cubes = new ArrayList<ShrubCube>();
        this.shrubs = Collections.unmodifiableList(f.shrubs);
        for (Shrub shrub : this.shrubs) {
            shrubIpMap.putAll(shrub.ipMap);
            _cubes.addAll(shrub.cubes);
        }
        this.shrubCubes = Collections.unmodifiableList(_cubes);
    }

    private static class ShrubFixture extends PseudoAbstractFixture {

        final List<Shrub> shrubs = new ArrayList<>();

        private ShrubFixture(
                LX lx, List<ShrubConfig> shrubConfigs, List<ShrubCubeConfig> shrubCubeConfigs) {
            super(lx, "Shrub");
            for (int i = 0; i < shrubConfigs.size(); i++) {
                ShrubConfig sc = shrubConfigs.get(i);
                shrubs.add(new Shrub(lx, shrubCubeConfigs, i, sc.x, sc.z, sc.ry));
            }
            List<LXPoint> pts = new ArrayList<>();
            for (Shrub shrub : shrubs) {
                Collections.addAll(pts, shrub.points);
            }
            this.setPoints(pts);
        }
    }

    public Vec3D getShrubMountPoint(ShrubCubeConfig c) {
        try {
            Shrub shrub = this.shrubs.get(c.shrubIndex);
            Vec3D p = shrub.shrubClusters.get(c.clusterIndex).rods.get(c.rodIndex).mountingPoint;
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

    public void addShrubModelTransform(ModelTransform modelTransform) {
        addShrubModelTransform(modelTransform);
    }
}
