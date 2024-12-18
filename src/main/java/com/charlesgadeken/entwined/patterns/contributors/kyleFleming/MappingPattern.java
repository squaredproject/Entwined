package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class MappingPattern extends EntwinedBasePattern {

    int numBits;
    int count;
    int numCompleteResetCycles = 10;
    int numCyclesToShowFrame = 2;
    int numResetCycles = 3;
    int numCyclesBlack = 4;
    int cycleCount = 0;

    public MappingPattern(LX lx) {
        super(lx);

        numBits = model.baseCubes.size();
    }

    public void run(double deltaMs) {
        if (count >= numBits) {
            if (numBits + numCyclesBlack <= count
                    && count < numBits + numCyclesBlack + numCompleteResetCycles) {
                setColors(LXColor.WHITE);
            } else {
                setColors(LXColor.BLACK);
            }
        } else if (cycleCount >= numCyclesToShowFrame) {
            if (numCyclesToShowFrame + numCyclesBlack <= cycleCount
                    && cycleCount < numCyclesToShowFrame + numCyclesBlack + numResetCycles) {
                setColors(LXColor.WHITE);
            } else {
                setColors(LXColor.BLACK);
            }
        } else {
            for (BaseCube cube : model.baseCubes) {
                setColor(cube.index, cube.index == count ? LXColor.WHITE : LXColor.BLACK);
            }
        }
        cycleCount =
                (cycleCount + 1) % (numCyclesToShowFrame + numResetCycles + 2 * numCyclesBlack);
        if (cycleCount == 0) {
            count = (count + 1) % (numBits + numCompleteResetCycles + 2 * numCyclesBlack);
        }
    }
}
