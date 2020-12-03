package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;

@LXCategory("Ray Sykes")
public class Ripple extends EntwinedTriggerablePattern {
    final BoundedParameter speed = new BoundedParameter("Speed", 15000, 25000, 8000);
    final BoundedParameter baseBrightness = new BoundedParameter("Bright", 0, 0, 100);
    final SawLFO rippleAge = new SawLFO(0, 100, speed);
    float hueVal;
    float brightVal;
    boolean resetDone = false;
    float yCenter;
    float thetaCenter;

    public Ripple(LX lx) {
        super(lx);
        addParameter(speed);
        addParameter(baseBrightness);
        addModulator(rippleAge).start();
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (rippleAge.getValuef() < 5) {
            if (!resetDone) {
                yCenter = 150 + Utilities.random(300);
                thetaCenter = Utilities.random(360);
                resetDone = true;
            }
        } else {
            resetDone = false;
        }
        float radius = Utilities.pow(rippleAge.getValuef(), 2) / 3;
        for (BaseCube cube : model.baseCubes) {
            float distVal =
                    Utilities.sqrt(
                            Utilities.pow(
                                            (LXUtils.wrapdistf(
                                                            thetaCenter,
                                                            cube.transformedTheta,
                                                            360))
                                                    * 0.8f,
                                            2)
                                    + Utilities.pow(yCenter - cube.transformedY, 2));
            float heightHueVariance = 0.1f * cube.transformedY;
            if (distVal < radius) {
                float rippleDecayFactor = (100 - rippleAge.getValuef()) / 100;
                float timeDistanceCombination = distVal / 20 - rippleAge.getValuef();
                hueVal =
                        (lx.engine.palette.getHuef()
                                        + 40
                                                * Utilities.sin(
                                                        Utilities.TWO_PI
                                                                * (12.5f + rippleAge.getValuef())
                                                                / 200)
                                                * rippleDecayFactor
                                                * Utilities.sin(timeDistanceCombination)
                                        + heightHueVariance
                                        + 360)
                                % 360;
                brightVal =
                        Utilities.constrain(
                                (baseBrightness.getValuef()
                                        + rippleDecayFactor * (100 - baseBrightness.getValuef())
                                        + 80
                                                * rippleDecayFactor
                                                * Utilities.sin(
                                                        timeDistanceCombination
                                                                + Utilities.TWO_PI / 8)),
                                0,
                                100);
            } else {
                hueVal = (lx.engine.palette.getHuef() + heightHueVariance) % 360;
                brightVal = baseBrightness.getValuef();
            }
            colors[cube.index] = LX.hsb(hueVal, 100, brightVal);
        }
    }
}
