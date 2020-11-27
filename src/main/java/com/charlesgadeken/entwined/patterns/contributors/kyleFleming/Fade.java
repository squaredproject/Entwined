package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

public class Fade extends EntwinedBasePattern {

    final BoundedParameter speed =
            new BoundedParameter(
                    "SPEE", 11000, 100000,
                    1000); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_OUT);
    final BoundedParameter smoothness =
            new BoundedParameter(
                    "SMOO", 100, 1, 100); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_IN);

    final SinLFO colr = new SinLFO(0, 360, speed);

    public Fade(LX lx) {
        super(lx);
        addParameter(speed);
        addParameter(smoothness);
        addModulator(colr).start();
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] =
                    lx.hsb(
                            (int) ((int) colr.getValuef() * smoothness.getValuef() / 100)
                                    * 100
                                    / smoothness.getValuef(),
                            100,
                            100);
        }
    }
}
