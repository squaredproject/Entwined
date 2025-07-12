package com.charlesgadeken.entwined.patterns.test;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.config.TreeOrShrub;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;

public class TestShrubSweep extends EntwinedBasePattern {

    final BoundedParameter x;
    final BoundedParameter y;
    final BoundedParameter z;

    public TestShrubSweep(LX lx) {
        super(lx);
        LXModel model = lx.getModel();
        addParameter(x = new BoundedParameter("X", 200, model.xMin, model.xMax));
        addParameter(y = new BoundedParameter("Y", 200, model.yMin, model.yMax));
        addParameter(z = new BoundedParameter("Z", 200, model.zMin, model.zMax));
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            if (cube.treeOrShrub == TreeOrShrub.SHRUB) {
                if (Utilities.abs(cube.ax - x.getValuef()) < 1
                        || Utilities.abs(cube.ay - y.getValuef()) < 1
                        || Utilities.abs(cube.az - z.getValuef()) < 1) {
                    colors[cube.index] = LX.hsb(135, 100, 100);
                } else {
                    colors[cube.index] = LX.hsb(135, 100, 0);
                }
            }
        }
    }
}
