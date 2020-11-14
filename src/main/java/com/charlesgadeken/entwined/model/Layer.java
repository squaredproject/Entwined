package com.charlesgadeken.entwined.model;

import heronarts.lx.LX;
import heronarts.lx.LXLayer;

public abstract class Layer extends LXLayer {
    protected final Model model;

    Layer(LX lx) {
        super(lx);
        model = (Model) lx.structure.getModel();
    }
}
