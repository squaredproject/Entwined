package com.charlesgadeken.entwined.patterns.contributors.jackLampack;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;

@LXCategory("Jack Lampack")
public class AcidTrip extends EntwinedTriggerablePattern {

    final SawLFO trails = new SawLFO(360, 0, 7000);

    public AcidTrip(LX lx) {
        super(lx);

        addModulator(trails).start();
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            colors[cube.index] =
                    lx.hsb(
                            Utilities.abs(model.cy - cube.transformedY)
                                    + Utilities.abs(model.cy - cube.transformedTheta)
                                    + trails.getValuef() % 360,
                            100,
                            100);
        }
    }
}
