package com.charlesgadeken.entwined.effects;

import heronarts.lx.LX;
import heronarts.lx.effect.BlurEffect;

public class EntwinedBlurEffect extends BlurEffect {
    public EntwinedBlurEffect(LX lx) {
        super(lx);
        level.setValue(0);
    }

    @Override
    public void loop(double deltaMs) {
        if (isEnabled()) {
            super.loop(deltaMs);
        }
    }
}
