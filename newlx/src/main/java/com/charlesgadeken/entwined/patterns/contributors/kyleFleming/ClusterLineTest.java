package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;
import toxi.geom.Vec2D;

public class ClusterLineTest extends EntwinedBasePattern {

    final BoundedParameter y;
    final BoundedParameter theta;
    final BoundedParameter spin;

    public ClusterLineTest(LX lx) {
        super(lx);

        addParameter(theta = new BoundedParameter("\u0398", 0, -90, 430));
        addParameter(y = new BoundedParameter("Y", 200, lx.getModel().yMin, lx.getModel().yMax));
        addParameter(spin = new BoundedParameter("SPIN", 0, -90, 430));
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        Vec2D origin = new Vec2D(theta.getValuef(), y.getValuef());
        for (BaseCube cube : model.baseCubes) {
            Vec2D cubePointPrime =
                    VecUtils.movePointToSamePlane(origin, cube.transformedCylinderPoint);
            float dist = origin.distanceTo(cubePointPrime);
            float cubeTheta =
                    (spin.getValuef() + 15)
                            + cubePointPrime.sub(origin).heading() * 180 / Utilities.PI
                            + 360;
            colors[cube.index] =
                    lx.hsb(
                            135,
                            100,
                            100
                                    * LXUtils.constrainf(
                                            (1
                                                    - Utilities.abs(cubeTheta % 90 - 15)
                                                            / 100
                                                            / Utilities.asin(
                                                                    20 / Utilities.max(20, dist))),
                                            0,
                                            1));
        }
    }
}
