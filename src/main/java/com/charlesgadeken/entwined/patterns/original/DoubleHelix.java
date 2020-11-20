package com.charlesgadeken.entwined.patterns.original;

import com.charlesgadeken.entwined.EntwinedCategory;
import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.cube.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.utils.LXUtils;

@LXCategory(EntwinedCategory.ORIGINAL)
public class DoubleHelix extends EntwinedBasePattern {

    final SinLFO rate = new SinLFO(400, 3000, 11000);
    final SawLFO theta = new SawLFO(0, 180, rate);
    final SinLFO coil = new SinLFO(0.2, 2, 13000);

    public DoubleHelix(LX lx) {
        super(lx);
        addModulator(rate).start();
        addModulator(theta).start();
        addModulator(coil).start();
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            float coilf = coil.getValuef() * (cube.cy - model.cy);
            colors[cube.index] =
                    lx.hsb(
                            lx.engine.palette.getHuef()
                                    + .4f * Utilities.abs(cube.transformedY - model.cy)
                                    + .2f * Utilities.abs(cube.transformedTheta - 180),
                            100,
                            Utilities.max(
                                    0,
                                    100
                                            - 2
                                                    * LXUtils.wrapdistf(
                                                            cube.transformedTheta,
                                                            theta.getValuef() + coilf,
                                                            180)));
        }
    }
}
