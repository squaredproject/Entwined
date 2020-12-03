package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class StrobeOneshot extends EntwinedTriggerablePattern {
    public StrobeOneshot(LX lx) {
        super(lx);
        patternMode = PATTERN_MODE_FIRED;
        setColors(LXColor.WHITE);
    }

    public void run(double deltaMs) {
        firedTimer += deltaMs;
        if (firedTimer >= 80) {
            setCallRun(false);
        }
    }
}
