package com.charlesgadeken.entwined.model;

import com.charlesgadeken.entwined.config.ShrubConfig;
import com.charlesgadeken.entwined.config.ShrubCubeConfig;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShrubModel extends LXModelInterceptor {

    /** Shrubs in the model */
    public final List<Shrub> shrubs;

    /** ShrubCubes in the model */
    public final List<ShrubCube> shrubCubes;

    public final Map<String, ShrubCube[]> shrubIpMap = new HashMap<String, ShrubCube[]>();

    private final List<ShrubConfig> shrubConfigs;

    ShrubModel(LX lx, List<ShrubConfig> shrubConfigs, List<ShrubCubeConfig> shrubCubeConfig) {
        super(new ShrubFixture(shrubConfigs, shrubCubeConfig));
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

        private final List<ShrubConfig> shrubConfigs;
        private final List<ShrubCubeConfig> shrubCubeConfigs;

        @Override
        List<LXPoint> computePoints() {
            for (int i = 0; i < shrubConfigs.size(); i++) {
                ShrubConfig sc = shrubConfigs.get(i);
                shrubs.add(new Shrub(shrubCubeConfigs, i, sc.x, sc.z, sc.ry));
            }
            List<LXPoint> pts = new ArrayList<>();
            for (Shrub shrub : shrubs) {
                Collections.addAll(pts, shrub.points);
            }
            return pts;
        }

        private ShrubFixture(
                List<ShrubConfig> shrubConfigs, List<ShrubCubeConfig> shrubCubeConfigs) {
            super("Shrub");
            this.shrubConfigs = shrubConfigs;
            this.shrubCubeConfigs = shrubCubeConfigs;
        }
    }



}
