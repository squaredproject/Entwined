package com.charlesgadeken.entwined.patterns.test;

import com.charlesgadeken.entwined.EntwinedCategory;
import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.config.TreeOrShrub;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.model.ShrubCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;

@LXCategory(EntwinedCategory.TESTPATTERN)
public class DebuggingPattern(LX lx) extends EntwinedTriggerablePattern {
    public DebuggingPattern(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;


        for (BaseCube cube : model.baseCubes) {
            if (cube.treeOrShrub == TreeOrShrub.SHRUB) {
                ShrubCube shrubCube = (ShrubCube) cube;

                if (shrubCube.config.rodIndex == (int) rodLayer.getValue()
                    || shrubCube.config.clusterIndex == (int) clusterIndex.getValue()
                    || shrubCube.config.shrubIndex == (int) shrubIndex.getValue()) {
                    colors[cube.index] = LX.hsb(135, 100, 100);
                } else {
                    colors[cube.index] = LX.hsb(135, 100, 0);
                }
            }
        }
        setColors(LXColor.BLACK);
        for (int i = 0; i < 12; ++i) {
            colors[i] =
                LX.hsb(
                    (hue.getValuef() + (i / 4) * spread.getValuef()) % 360,
                    sat.getValuef() * 100,
                    Utilities.min(100, brt.getValuef() * (i + 1) / 12.f * 200));
        }
    }
}
