package com.charlesgadeken.entwined.levels;

import heronarts.lx.parameter.BoundedParameter;
import java.util.Arrays;
import java.util.List;

public class ChannelTreeLevels {
    private BoundedParameter[] levels;

    public ChannelTreeLevels(int numTrees) {
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

    public List<BoundedParameter> getLevels() {
        return Arrays.asList(levels);
    }
}
