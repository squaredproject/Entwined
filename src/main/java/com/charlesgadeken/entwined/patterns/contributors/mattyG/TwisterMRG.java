package com.charlesgadeken.entwined.patterns.contributors.mattyG;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("AAA")
public class TwisterMRG extends EntwinedTriggerablePattern {

    final SinLFO spin = new SinLFO(0, 5 * 360, 16000);
    final BoundedParameter pitch = new BoundedParameter("ZPitch", 0, 0, 360);

    float coil(float basis) {
        return Utilities.sin(basis * Utilities.TWO_PI - Utilities.PI);
    }

    public TwisterMRG(LX lx) {
        super(lx);
        addModulator(spin).start();
        addParameter(pitch);
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        float angleDeg = spin.getValuef();

        for (BaseCube cube : model.baseCubes) {
            float cubeAng =
                    (float) (cube.getTransformedTheta() + (pitch.getValue() * cube.transformedY));
            if ((Math.abs((angleDeg - cubeAng) % 360)) > 10) {
                colors[cube.index] = LX.hsa(0, 0, 0);
                continue;
            }

            colors[cube.index] = LX.hsb((lx.engine.palette.getHuef() + cubeAng) % 360, 100, 100);
        }
    }
}
