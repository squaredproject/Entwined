package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Ray Sykes")
public class Stripes extends EntwinedBasePattern {
    final BoundedParameter minSpacing = new BoundedParameter("MinSpacing", 0.5, .3, 2.5);
    final BoundedParameter maxSpacing = new BoundedParameter("MaxSpacing", 2, .3, 2.5);
    final SinLFO spacing = new SinLFO(minSpacing, maxSpacing, 8000);
    final SinLFO slopeFactor = new SinLFO(0.05, 0.2, 19000);

    public Stripes(LX lx) {
        super(lx);
        addParameter(minSpacing);
        addParameter(maxSpacing);
        addModulator(slopeFactor).start();
        addModulator(spacing).start();
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            float hueVal = (lx.engine.palette.getHuef() + .1f * cube.transformedY) % 360;
            float brightVal =
                    50
                            + 50
                                    * Utilities.sin(
                                            spacing.getValuef()
                                                    * (Utilities.sin(
                                                                    (Utilities.TWO_PI / 360)
                                                                            * 4
                                                                            * cube.transformedTheta)
                                                            + slopeFactor.getValuef()
                                                                    * cube.transformedY));
            colors[cube.index] = LX.hsb(hueVal, 100, brightVal);
        }
    }
}
