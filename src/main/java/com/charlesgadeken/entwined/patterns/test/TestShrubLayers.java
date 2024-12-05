package com.charlesgadeken.entwined.patterns.test;

import com.charlesgadeken.entwined.config.TreeOrShrub;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.model.ShrubCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class TestShrubLayers extends EntwinedBasePattern {
    final BoundedParameter rodLayer;
    final BoundedParameter clusterIndex;
    final BoundedParameter shrubIndex;

    public TestShrubLayers(LX lx) {
        super(lx);
        // lowest level means turning that param off
        addParameter(rodLayer = new BoundedParameter("layer", 0, 0, 5));
        addParameter(clusterIndex = new BoundedParameter("clusterIndex", -1, -1, 11));
        addParameter(shrubIndex = new BoundedParameter("shrubIndex", -1, -1, 19));
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            if (cube.treeOrShrub == TreeOrShrub.SHRUB) {
                ShrubCube shrubCube = (ShrubCube) cube;

                if (shrubCube.config.rodIndex == (int) rodLayer.getValue()
                        || shrubCube.config.clusterIndex == (int) clusterIndex.getValue()
                        || shrubCube.config.shrubIndex == (int) shrubIndex.getValue()) {
                    colors[cube.index] = LX.hsb(135, 100, 100);
                } else {
                    colors[cube.index] = LX.hsb(135, 100, 0);
                }
            }
        }
    }
}
