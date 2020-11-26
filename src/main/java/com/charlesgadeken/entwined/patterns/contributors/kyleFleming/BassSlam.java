package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import com.charlesgadeken.entwined.model.BaseCube;
import heronarts.lx.LX;
import heronarts.lx.utils.LXUtils;

public class BassSlam extends EntwinedTriggerablePattern {

    private final double flashTimePercent = 0.1f;
    private final int patternHue = 200;

    public BassSlam(LX lx) {
        super(lx);

        patternMode = PATTERN_MODE_FIRED;
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        if (triggerableModeEnabled) {
            firedTimer += deltaMs / 800;
            if (firedTimer > 1) {
                setCallRun(false);
                return;
            }
        }

        if (progress() < flashTimePercent) {
            setColors(LX.hsb(patternHue, 100, 100));
        } else {
            float time =
                    (float) ((progress() - flashTimePercent) / (1 - flashTimePercent) * 1.3755f);
            float y;
            // y = 0 when time = 1.3755f
            if (time < 1) {
                y = 1 + Utilities.pow(time + 0.16f, 2) * Utilities.sin(18 * (time + 0.16f)) / 4;
            } else {
                y = 1.32f - 20 * Utilities.pow(time - 1, 2);
            }
            y = Utilities.max(0, 100 * (y - 1) + 250);

            for (BaseCube cube : model.baseCubes) {
                setColor(
                        cube.index,
                        LX.hsb(
                                patternHue,
                                100,
                                LXUtils.constrainf(
                                        100 - 2 * Utilities.abs(y - cube.transformedY), 0, 100)));
            }
        }
    }

    double progress() {
        return triggerableModeEnabled
                ? ((firedTimer + flashTimePercent) % 1)
                : lx.engine.tempo.ramp();
    }
}
