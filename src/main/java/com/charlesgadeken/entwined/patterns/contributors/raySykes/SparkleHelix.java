package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Ray Sykes")
public class SparkleHelix extends EntwinedBasePattern {
    final BoundedParameter minCoil = new BoundedParameter("MinCOIL", .02, .005, .05);
    final BoundedParameter maxCoil = new BoundedParameter("MaxCOIL", .03, .005, .05);
    final BoundedParameter sparkle = new BoundedParameter("Spark", 80, 160, 10);
    final BoundedParameter sparkleSaturation = new BoundedParameter("Sat", 50, 0, 100);
    final BoundedParameter counterSpiralStrength = new BoundedParameter("Double", 0, 0, 1);

    final SinLFO coil = new SinLFO(minCoil, maxCoil, 8000);
    final SinLFO rate = new SinLFO(6000, 1000, 19000);
    final SawLFO spin = new SawLFO(0, Utilities.TWO_PI, rate);
    final SinLFO width = new SinLFO(10, 20, 11000);
    int[] sparkleTimeOuts;

    public SparkleHelix(LX lx) {
        super(lx);
        addParameter(minCoil);
        addParameter(maxCoil);
        addParameter(sparkle);
        addParameter(sparkleSaturation);
        addParameter(counterSpiralStrength);
        addModulator(rate).start();
        addModulator(coil).start();
        addModulator(spin).start();
        addModulator(width).start();
        sparkleTimeOuts = new int[model.baseCubes.size()];
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            float compensatedWidth = (0.7f + .02f / coil.getValuef()) * width.getValuef();
            float spiralVal =
                    Utilities.max(
                            0,
                            100
                                    - (100 * Utilities.TWO_PI / (compensatedWidth))
                                            * Utilities.degreeDifference(
                                                    (Utilities.TWO_PI / 360)
                                                            * cube.transformedTheta,
                                                    8 * Utilities.TWO_PI
                                                            + spin.getValuef()
                                                            + coil.getValuef()
                                                                    * (cube.transformedY
                                                                            - model.cy)));
            float counterSpiralVal =
                    counterSpiralStrength.getValuef()
                            * Utilities.max(
                                    0,
                                    100
                                            - (100 * Utilities.TWO_PI / (compensatedWidth))
                                                    * Utilities.degreeDifference(
                                                            (Utilities.TWO_PI / 360)
                                                                    * cube.transformedTheta,
                                                            8 * Utilities.TWO_PI
                                                                    - spin.getValuef()
                                                                    - coil.getValuef()
                                                                            * (cube.transformedY
                                                                                    - model.cy)));
            float hueVal = (lx.engine.palette.getHuef() + .1f * cube.transformedY) % 360;
            if (sparkleTimeOuts[cube.index] > Utilities.millis()) {
                colors[cube.index] = LX.hsb(hueVal, sparkleSaturation.getValuef(), 100);
            } else {
                colors[cube.index] =
                        LX.hsb(hueVal, 100, Utilities.max(spiralVal, counterSpiralVal));
                if (Utilities.random(Utilities.max(spiralVal, counterSpiralVal))
                        > sparkle.getValuef()) {
                    sparkleTimeOuts[cube.index] = Utilities.millis() + 100;
                }
            }
        }
    }
}
