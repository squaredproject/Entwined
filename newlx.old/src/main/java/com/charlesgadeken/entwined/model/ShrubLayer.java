package com.charlesgadeken.entwined.model;

import heronarts.lx.LX;
import heronarts.lx.LXLayer;

abstract class ShrubLayer extends LXLayer {
    protected final ShrubModel model;

    ShrubLayer(LX lx) {
        super(lx);
        model = (ShrubModel) lx.getModel();
    }
}
