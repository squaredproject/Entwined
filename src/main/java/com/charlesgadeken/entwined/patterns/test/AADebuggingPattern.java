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

import java.util.DoubleSummaryStatistics;

@LXCategory("AAA")
public class AADebuggingPattern extends EntwinedTriggerablePattern {
    public AADebuggingPattern(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        DoubleSummaryStatistics stats = model.baseCubes.stream().mapToDouble(BaseCube::getTransformedTheta).summaryStatistics();
        System.out.println(stats.getMin());
        System.out.println(stats.getMax());

        for (BaseCube cube : model.baseCubes) {
            // float ndTT = (float) ((cube.getTransformedTheta() - stats.getMin()) / (stats.getMax() - stats.getMin()));
            colors[cube.index] = LX.hsa((float) cube.getTransformedTheta(), 100, 100);
        }
    }
}
