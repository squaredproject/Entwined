package com.charlesgadeken.entwined.model;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;

public abstract class Effect extends LXEffect {

    protected final Model model;
    protected final Model shrubModel;

    Effect(LX lx) {
        super(lx);
        model = (Model) lx.getModel();
        shrubModel = (Model) lx.getModel();
    }

    @Override
    public void loop(double deltaMs) {
        if (isEnabled()) {
            super.loop(deltaMs);
        }
    }
}
