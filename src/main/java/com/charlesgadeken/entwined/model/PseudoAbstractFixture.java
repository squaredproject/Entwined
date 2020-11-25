package com.charlesgadeken.entwined.model;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.structure.LXBasicFixture;
import heronarts.lx.transform.LXMatrix;
import java.util.List;

public abstract class PseudoAbstractFixture {
    private final List<LXPoint> points;
    private final String name;

    public PseudoAbstractFixture(String name) {
        this.name = name;
        this.points = computePoints();
    }

    abstract List<LXPoint> computePoints();

    public List<LXPoint> getPoints() {
        return points;
    }
}
