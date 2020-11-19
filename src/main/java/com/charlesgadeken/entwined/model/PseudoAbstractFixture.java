package com.charlesgadeken.entwined.model;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.structure.LXBasicFixture;
import heronarts.lx.transform.LXMatrix;
import java.util.List;

public abstract class PseudoAbstractFixture extends LXBasicFixture {
    private List<LXPoint> points;

    PseudoAbstractFixture(LX lx, String name) {
        super(lx, name);
    }

    public List<LXPoint> getPoints() {
        return points;
    }

    public void setPoints(List<LXPoint> points) {
        this.points = points;
    }

    @Override
    protected int size() {
        return this.points.size();
    }

    @Override
    protected void computePointGeometry(LXMatrix lxMatrix, List<LXPoint> list) {
        for (int i = 0; i < this.points.size(); i++) {
            list.get(i).set(this.points.get(i));
        }
    }

    @Override
    protected void buildOutputs() {
        super.buildOutputs();
    }
}
