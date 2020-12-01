package com.charlesgadeken.entwined.levels;

import heronarts.lx.parameter.BoundedParameter;
import java.util.Arrays;
import java.util.List;

public class ChannelShrubLevels {
    private BoundedParameter[] levels;

    public ChannelShrubLevels(int numShrubs) {
        levels = new BoundedParameter[numShrubs];
        for (int i = 0; i < numShrubs; i++) {
            this.levels[i] = new BoundedParameter("shrub" + i, 1);
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
