package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Irene Zhou")
public class Lattice extends EntwinedTriggerablePattern {
    final SawLFO spin = new SawLFO(0, 4320, 24000);
    final SinLFO yClimb = new SinLFO(60, 30, 24000);
    final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
    final BoundedParameter yHeight = new BoundedParameter("HEIGHT", 0, -500, 500);

    float coil(float basis) {
        return Utilities.sin(basis * Utilities.PI);
    }

    public Lattice(LX lx) {
        super(lx);
        addModulator(spin).start();
        addModulator(yClimb).start();
        addParameter(hue);
        addParameter(yHeight);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        float spinf = spin.getValuef();
        float coilf = 2 * coil(spin.getBasisf());
        for (BaseCube cube : model.baseCubes) {
            float wrapdistleft =
                    LXUtils.wrapdistf(
                            cube.getTransformedTheta(),
                            spinf + (model.yMax - cube.transformedY) * coilf,
                            180);
            float wrapdistright =
                    LXUtils.wrapdistf(
                            cube.getTransformedTheta(),
                            -spinf - (model.yMax - cube.transformedY) * coilf,
                            180);
            float width =
                    yClimb.getValuef()
                            + ((cube.transformedY - yHeight.getValuef()) / model.yMax) * 50;
            float df =
                    Utilities.min(
                            100,
                            3 * Utilities.max(0, wrapdistleft - width)
                                    + 3 * Utilities.max(0, wrapdistright - width));

            colors[cube.index] =
                    lx.hsb(
                            (hue.getValuef()
                                            + lx.engine.palette.getHuef()
                                            + .2f * cube.transformedY
                                            - 360)
                                    % 360,
                            100,
                            df);
        }
    }
}
