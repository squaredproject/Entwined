package com.charlesgadeken.entwined.effects;

import heronarts.lx.LX;
import heronarts.lx.effect.BlurEffect;

public class TSBlurEffect extends BlurEffect {
    public TSBlurEffect(LX lx) {
        super(lx);
    }

    @Override
    public void loop(double deltaMs) {
        if (isEnabled()) {
            super.loop(deltaMs);
        }
    }
}
