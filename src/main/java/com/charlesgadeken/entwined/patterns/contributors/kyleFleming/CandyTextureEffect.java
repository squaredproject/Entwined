package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

public class CandyTextureEffect extends EntwinedBaseEffect {
    final BoundedParameter amount = new BoundedParameter("CAND");

    double time = 0;

    public CandyTextureEffect(LX lx) {
        super(lx);
    }

    public void run(double deltaMs, double unused) {
        if (amount.getValue() > 0) {
            time += deltaMs;
            for (int i = 0; i < colors.length; i++) {
                int oldColor = colors[i];
                float newHue = i * 127 + 9342 + (float) time % 360;
                int newColor = LX.hsb(newHue, 100, 100);
                int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
                colors[i] =
                        LX.hsb(
                                LXColor.h(blendedColor),
                                LXColor.s(blendedColor),
                                LXColor.b(oldColor));
            }
        }
    }
}
