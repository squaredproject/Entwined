package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import heronarts.lx.LXLayer;

public abstract class Layer extends LXLayer {

    protected final Model model;

    Layer(LX lx) {
        super(lx);
        model = (Model) lx.getModel();
    }
}
