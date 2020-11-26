package com.charlesgadeken.entwined.effects;

import heronarts.lx.LX;

public class TurnOffDeadPixelsEffect extends EntwinedBaseEffect {
    int[] deadPixelIndices = new int[] {};
    int[] deadPixelClusters = new int[] {};

    TurnOffDeadPixelsEffect(LX lx) {
        super(lx);
    }

    public void run(double deltaMs, double unused) {
        for (int i = 0; i < deadPixelIndices.length; i++) {
            // Cluster cluster = model.clusters.get(deadPixelClusters[i]);
            // Cube cube = cluster.cubes.get(deadPixelIndices[i]);
            // colors[cube.index] = LXColor.BLACK;
        }
    }
}
