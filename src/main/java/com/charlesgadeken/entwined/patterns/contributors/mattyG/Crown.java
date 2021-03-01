package com.charlesgadeken.entwined.patterns.contributors.mattyG;

import com.charlesgadeken.entwined.config.TreeOrShrub;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import java.util.PrimitiveIterator;
import java.util.Random;

@LXCategory("MattyG")
public class Crown extends EntwinedTriggerablePattern {
    private final Random myRandom;
    private final BoundedParameter weighting = new BoundedParameter("Weighting", 0, 0.5, 1);
    private final BoundedParameter height = new BoundedParameter("Height", 0, 0.0, 50);

    public Crown(LX lx) {
        super(lx);
        myRandom = new Random(3);
        addParameter(weighting);
        addParameter(height);
    }

    public void run(double deltaMs) {
        PrimitiveIterator.OfDouble ds = myRandom.doubles(0.0, 100.0).iterator();
        for (BaseCube cube : model.baseCubes) {
            double inRad = cube.transformedTheta / (180.0 * Math.PI);

            boolean inside =
                    (cube.treeOrShrub == TreeOrShrub.TREE)
                            || (Math.pow(1 - Math.sin(inRad / 8), 3) * height.getValue()
                                    > cube.transformedY);

            int currentColor = colors[cube.index];
            double w = weighting.getValue();
            double prev = LXColor.b(currentColor) * w;
            double next = ds.next() * (1.0 - w);

            colors[cube.index] = inside ? LXColor.gray(next + prev) : LXColor.BLACK;
        }
    }
}
