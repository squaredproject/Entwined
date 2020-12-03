package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class Brightness extends EntwinedTriggerablePattern {

    public Brightness(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {}

    public void onTriggered(float strength) {
        setColors(lx.hsb(0, 0, 100 * strength));
    }

    public void onRelease() {
        setColors(LXColor.BLACK);
    }
}
