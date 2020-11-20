package com.charlesgadeken.entwined.effects;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;

public abstract class EntwinedBaseEffect extends LXEffect {

    protected final Model model;
    protected final Model shrubModel;

    public EntwinedBaseEffect(LX lx) {
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
