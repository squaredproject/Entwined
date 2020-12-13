package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Ray Sykes")
public class SparkleTakeOver extends EntwinedTriggerablePattern {
    int[] sparkleTimeOuts;
    int lastComplimentaryToggle = 0;
    int complimentaryToggle = 0;
    boolean resetDone = false;
    final SinLFO timing = new SinLFO(6000, 10000, 20000);
    final SawLFO coverage = new SawLFO(0, 100, timing);
    final BoundedParameter hueVariation = new BoundedParameter("HueVar", 0.1, 0.1, 0.4);
    float hueSeparation = 180;
    float newHueVal;
    float oldHueVal;
    float newBrightVal = 100;
    float oldBrightVal = 100;

    public SparkleTakeOver(LX lx) {
        super(lx);
        sparkleTimeOuts = new int[model.baseCubes.size()];
        addModulator(timing).start();
        addModulator(coverage).start();
        addParameter(hueVariation);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (coverage.getValuef() < 5) {
            if (!resetDone) {
                lastComplimentaryToggle = complimentaryToggle;
                oldBrightVal = newBrightVal;
                if (Utilities.random(5) < 2) {
                    complimentaryToggle = 1 - complimentaryToggle;
                    newBrightVal = 100;
                } else {
                    newBrightVal = (newBrightVal == 100) ? 70 : 100;
                }
                for (int i = 0; i < sparkleTimeOuts.length; i++) {
                    sparkleTimeOuts[i] = 0;
                }
                resetDone = true;
            }
        } else {
            resetDone = false;
        }
        for (BaseCube cube : model.baseCubes) {
            float newHueVal =
                    (lx.engine.palette.getHuef()
                                    + complimentaryToggle * hueSeparation
                                    + hueVariation.getValuef() * cube.transformedY)
                            % 360;
            float oldHueVal =
                    (lx.engine.palette.getHuef()
                                    + lastComplimentaryToggle * hueSeparation
                                    + hueVariation.getValuef() * cube.transformedY)
                            % 360;
            if (sparkleTimeOuts[cube.index] > Utilities.millis()) {
                colors[cube.index] =
                        LX.hsb(newHueVal, (30 + coverage.getValuef()) / 1.3f, newBrightVal);
            } else {
                colors[cube.index] =
                        LX.hsb(oldHueVal, (140 - coverage.getValuef()) / 1.4f, oldBrightVal);
                float chance =
                        Utilities.random(
                                Utilities.abs(
                                                Utilities.sin(
                                                                (Utilities.TWO_PI / 360)
                                                                        * cube.transformedTheta
                                                                        * 4)
                                                        * 50)
                                        + Utilities.abs(
                                                        Utilities.sin(
                                                                Utilities.TWO_PI
                                                                        * (cube.transformedY
                                                                                / 9000)))
                                                * 50);
                if (chance > (100 - 100 * (Utilities.pow(coverage.getValuef() / 100, 2)))) {
                    sparkleTimeOuts[cube.index] = Utilities.millis() + 50000;
                } else if (chance > 1.1f * (100 - coverage.getValuef())) {
                    sparkleTimeOuts[cube.index] = Utilities.millis() + 100;
                }
            }
        }
    }
}
