package com.charlesgadeken.entwined.effects;

import com.charlesgadeken.entwined.model.Entwined;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;

public abstract class EntwinedBaseEffect extends LXEffect {

    protected final Entwined model;
    protected final Entwined shrubModel;

    public EntwinedBaseEffect(LX lx) {
        super(lx);
        model = (Entwined) lx.getModel();
        shrubModel = (Entwined) lx.getModel();
    }

    @Override
    public void loop(double deltaMs) {
        if (isEnabled()) {
            super.loop(deltaMs);
        }
    }
}
