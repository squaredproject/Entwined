package com.charlesgadeken.entwined.model;

import heronarts.lx.model.LXPoint;
import java.util.List;

public abstract class PseudoAbstractFixture {
    private List<LXPoint> points;
    private final String name;

    public PseudoAbstractFixture(String name) {
        this.name = name;
    }

    public void setPoints(List<LXPoint> points) {
        this.points = points;
    }

    public List<LXPoint> getPoints() {
        return points;
    }
}
