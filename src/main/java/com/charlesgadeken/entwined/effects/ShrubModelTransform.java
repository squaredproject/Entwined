package com.charlesgadeken.entwined.effects;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

public abstract class ShrubModelTransform extends EntwinedBaseEffect {
    public ShrubModelTransform(LX lx) {
        super(lx);
        shrubModel.addModelTransform(this);
    }

    @Override
    public void run(double deltaMs, double unused) {}

    public abstract void transform(LXModel lxModel);
}
