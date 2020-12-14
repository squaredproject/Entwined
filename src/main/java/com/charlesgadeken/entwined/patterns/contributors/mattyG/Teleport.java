package com.charlesgadeken.entwined.patterns.contributors.mattyG;

import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PApplet;

@LXCategory("MattyG")
public class Teleport extends EntwinedTriggerablePattern {
    public final CompoundParameter speed = new CompoundParameter("Speed", 5000, 20000, 250);

    public final CompoundParameter bandWidth =
            new CompoundParameter(
                    "Bandwidth",
                    (model.yMax - model.yMin) / 10,
                    (model.yMax - model.yMin) / 100,
                    (model.yMax - model.yMin) / 2);

    private SawLFO saw = new SawLFO(model.yMin, model.yMax, speed);

    public Teleport(LX lx) {
        super(lx);
        addModulator(saw).trigger();
        addParameter(bandWidth);
        addParameter(speed);
    }

    public void run(double deltaMs) {
        float currentY = saw.getValuef();
        float halfBandwidth = (bandWidth.getValuef() / 2);
        currentY =
                PApplet.map(
                        currentY,
                        model.yMin,
                        model.yMax,
                        model.yMin - halfBandwidth,
                        model.yMax + halfBandwidth);

        for (LXPoint p : model.points) {
            float dist = Math.abs(p.y - currentY);
            float falloff = 100 / halfBandwidth;
            float b = Math.max(0, 100 - falloff * dist);
            colors[p.index] = LXColor.gray(b);
        }
    }
}
