package com.charlesgadeken.entwined.levels;

import heronarts.lx.parameter.BoundedParameter;

public class ChannelTreeLevels {
    private BoundedParameter[] levels;

    ChannelTreeLevels(int numTrees) {
        levels = new BoundedParameter[numTrees];
        for (int i = 0; i < numTrees; i++) {
            this.levels[i] = new BoundedParameter("tree" + i, 1);
        }
    }

    public BoundedParameter getParameter(int i) {
        return this.levels[i];
    }

    public double getValue(int i) {
        return this.levels[i].getValue();
    }
}
