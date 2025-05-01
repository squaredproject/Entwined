package com.charlesgadeken.entwined.patterns.contributors.raySykes;

import com.charlesgadeken.entwined.Utilities;

public class IceCrystalSettings {
    protected int totalRecursionDepth;
    protected float baseLineWidth;
    protected float baseLineLength;
    protected float basePropagationSpeed;
    protected float[] lineLengths;
    protected boolean growthFinished = false;
    protected int growthFinishedTime = 0;
    protected final int maxRecursionDepth;

    IceCrystalSettings(int maxRecursionDepth) {
        this.maxRecursionDepth = maxRecursionDepth;
    }

    public void doSettings(
            int totalRecursionDepth,
            float baseLineWidth,
            float baseLineLength,
            float basePropagationSpeed) {
        this.totalRecursionDepth = totalRecursionDepth;
        this.baseLineWidth = baseLineWidth;
        this.baseLineLength = baseLineLength;
        this.basePropagationSpeed = basePropagationSpeed;
        growthFinishedTime = 0;
        growthFinished = false;
        lineLengths = new float[totalRecursionDepth + 1];
        for (int i = 0; i <= totalRecursionDepth; i++) {
            lineLengths[i] = Utilities.pow(0.9f, i) * (0.5f + Utilities.random(1)) * baseLineLength;
        }
    }

    public float getLineWidth(int recursionDepth) {
        return baseLineWidth * Utilities.pow(0.9f, recursionDepth);
    }

    public float getLineLength(int recursionDepth) {
        return lineLengths[recursionDepth];
    }

    public float getPropagationSpeed(int recursionDepth) {
        return basePropagationSpeed * Utilities.pow(0.8f, recursionDepth);
    }

    public void setGrowthFinished() {
        if (!growthFinished) {
            growthFinishedTime = Utilities.millis();
        }
        growthFinished = true;
    }
}
