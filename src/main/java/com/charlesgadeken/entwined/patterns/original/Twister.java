package com.charlesgadeken.entwined.patterns.original;

import com.charlesgadeken.entwined.EntwinedCategory;
import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;

@LXCategory(EntwinedCategory.ORIGINAL)
public class Twister extends EntwinedTriggerablePattern {

    final SinLFO spin = new SinLFO(0, 5 * 360, 16000);

    float coil(float basis) {
        return Utilities.sin(basis * Utilities.TWO_PI - Utilities.PI);
    }

    public Twister(LX lx) {
        super(lx);
        addModulator(spin).start();
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        float spinf = spin.getValuef();
        float coilf = 2 * coil(spin.getBasisf());
        for (BaseCube cube : model.baseCubes) {
            float wrapdist =
                    Utilities.degreeDifference(
                            cube.transformedTheta,
                            spinf + (model.yMax - cube.transformedY) * coilf);
            float yn = (cube.transformedY / model.yMax);
            float width = 10 + 30 * yn;
            float df = Utilities.max(0, 100 - (100 / 45) * Utilities.max(0, wrapdist - width));
            colors[cube.index] =
                    LX.hsb(
                            (lx.engine.palette.getHuef() + .2f * cube.transformedY - 360 - wrapdist)
                                    % 360,
                            Utilities.max(0, 100 - 500 * Utilities.max(0, yn - .8f)),
                            df);
        }
    }
}
