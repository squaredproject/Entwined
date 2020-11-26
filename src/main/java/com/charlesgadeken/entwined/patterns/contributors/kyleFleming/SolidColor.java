package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class SolidColor extends EntwinedBasePattern {
    // 235 = blue, 135 = green, 0 = red
    final BoundedParameter hue = new BoundedParameter("HUE", 135, 0, 360);
    final BoundedParameter brightness = new BoundedParameter("BRT", 100, 0, 100);

    public SolidColor(LX lx) {
        super(lx);
        addParameter(hue);
        addParameter(brightness);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        setColors(lx.hsb(hue.getValuef(), 100, (float) brightness.getValue()));
    }
}
