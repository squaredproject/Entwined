package com.charlesgadeken.entwined.patterns.test;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

@LXCategory("Testing Patterns")
public class DiffusionTestPattern extends EntwinedBasePattern {

    final BoundedParameter hue = new BoundedParameter("HUE", 0, 360);
    final BoundedParameter sat = new BoundedParameter("SAT", 1);
    final BoundedParameter brt = new BoundedParameter("BRT", 1);
    final BoundedParameter spread = new BoundedParameter("SPREAD", 0, 360);

    public DiffusionTestPattern(LX lx) {
        super(lx);
        addParameter("test/diffusion/hue", hue);
        addParameter("test/diffusion/sat", sat);
        addParameter("test/diffusion/brightness", brt);
        addParameter("test/diffusion/spread", spread);
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        setColors(LXColor.BLACK);
        for (int i = 0; i < 12; ++i) {
            colors[i] =
                    LX.hsb(
                            (hue.getValuef() + (i / 4) * spread.getValuef()) % 360,
                            sat.getValuef() * 100,
                            Utilities.min(100, brt.getValuef() * (i + 1) / 12.f * 200));
        }
    }
}
