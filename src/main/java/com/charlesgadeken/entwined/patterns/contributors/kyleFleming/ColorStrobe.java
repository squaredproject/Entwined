package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedTriggerablePattern;
import heronarts.lx.LX;

public class ColorStrobe extends EntwinedTriggerablePattern {

    double timer = 0;

    public ColorStrobe(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        timer += deltaMs;
        if (timer > 16) {
            timer = 0;
            setColors(lx.hsb(Utilities.random(360), 100, 100));
        }
    }
}
