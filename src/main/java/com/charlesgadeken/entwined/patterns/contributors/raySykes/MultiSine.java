package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Ray Sykes")
public class MultiSine extends EntwinedBasePattern {
    final int numLayers = 3;
    int[][] distLayerDivisors = {{50, 140, 200}, {360, 60, 45}};
    final BoundedParameter brightEffect = new BoundedParameter("Bright", 100, 0, 100);

    final BoundedParameter[] timingSettings = {
        new BoundedParameter("T1", 6300, 5000, 30000),
        new BoundedParameter("T2", 4300, 2000, 10000),
        new BoundedParameter("T3", 11000, 10000, 20000)
    };
    SinLFO[] frequencies = {
        new SinLFO(0, 1, timingSettings[0]),
        new SinLFO(0, 1, timingSettings[1]),
        new SinLFO(0, 1, timingSettings[2])
    };

    public MultiSine(LX lx) {
        super(lx);
        for (int i = 0; i < numLayers; i++) {
            addParameter(timingSettings[i]);
            addModulator(frequencies[i]).start();
        }
        addParameter(brightEffect);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            float[] combinedDistanceSines = {0, 0};
            for (int i = 0; i < numLayers; i++) {
                combinedDistanceSines[0] +=
                        Utilities.sin(
                                        Utilities.TWO_PI * frequencies[i].getValuef()
                                                + cube.transformedY / distLayerDivisors[0][i])
                                / numLayers;
                combinedDistanceSines[1] +=
                        Utilities.sin(
                                        Utilities.TWO_PI * frequencies[i].getValuef()
                                                + Utilities.TWO_PI
                                                        * (cube.transformedTheta
                                                                / distLayerDivisors[1][i]))
                                / numLayers;
            }
            float hueVal =
                    (lx.engine.palette.getHuef()
                                    + 20
                                            * Utilities.sin(
                                                    Utilities.TWO_PI
                                                            * (combinedDistanceSines[0]
                                                                    + combinedDistanceSines[1])))
                            % 360;
            float brightVal =
                    (100 - brightEffect.getValuef())
                            + brightEffect.getValuef()
                                    * (2 + combinedDistanceSines[0] + combinedDistanceSines[1])
                                    / 4;
            float satVal =
                    90
                            + 10
                                    * Utilities.sin(
                                            Utilities.TWO_PI
                                                    * (combinedDistanceSines[0]
                                                            + combinedDistanceSines[1]));
            colors[cube.index] = lx.hsb(hueVal, satVal, brightVal);
        }
    }
}
