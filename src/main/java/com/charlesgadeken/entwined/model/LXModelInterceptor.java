package com.charlesgadeken.entwined.model;

import heronarts.lx.model.LXModel;

public class LXModelInterceptor extends LXModel {
    private final PseudoAbstractFixture fixture;

    public LXModelInterceptor(PseudoAbstractFixture fixture) {
        super(fixture.getPoints());
        this.fixture = fixture;
    }

    public PseudoAbstractFixture getFixture() {
        return fixture;
    }
}
