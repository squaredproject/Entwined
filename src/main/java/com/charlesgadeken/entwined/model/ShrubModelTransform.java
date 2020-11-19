package com.charlesgadeken.entwined.model;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

public abstract class ShrubModelTransform extends Effect {
    ShrubModelTransform(LX lx) {
        super(lx);
        shrubModel.addModelTransform(this);
    }

    abstract void transform(LXModel lxModel);
}
