package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;

public class ColorStrobeTextureEffect extends EntwinedBaseEffect {

    final BoundedParameter amount =
            new BoundedParameter(
                    "SEIZ", 0, 0, 1); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_IN);

    public ColorStrobeTextureEffect(LX lx) {
        super(lx);
    }

    public void run(double deltaMs, double unused) {
        if (amount.getValue() > 0) {
            float newHue = Utilities.random(360);
            int newColor = lx.hsb(newHue, 100, 100);
            for (int i = 0; i < colors.length; i++) {
                int oldColor = colors[i];
                int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
                colors[i] =
                        lx.hsb(
                                LXColor.h(blendedColor),
                                LXColor.s(blendedColor),
                                LXColor.b(oldColor));
            }
        }
    }
}
