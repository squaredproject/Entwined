package com.charlesgadeken.entwined.effects.original;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import com.charlesgadeken.entwined.model.Shrub;
import com.charlesgadeken.entwined.model.Tree;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;

public class ScrambleEffect extends EntwinedBaseEffect {
    // NOTE(meawoppl) In two the places where lx.getModel().size occurs, this used to be lx.total
    // Not 100% sure, but this is intended to be the _total_ point count in the model

    public final BoundedParameter amount = new BoundedParameter("SCRA");
    final int offset;

    public ScrambleEffect(LX lx) {
        super(lx);

        offset = lx.getModel().size / 4 + 5;
    }

    int getAmount() {
        return (int)(amount.getValue() * lx.getModel().size / 2);
    }

    protected void run(double deltaMs, double unused) {
        for (Tree tree : model.trees) {
            for (int i = Utilities.min(tree.cubes.size() - 1, getAmount()); i > 0; i--) {
                colors[tree.cubes.get(i).index] = colors[tree.cubes.get((i + offset) % tree.cubes.size()).index];
            }
        }
        for (Shrub shrub : model.shrubs) {
            for (int i = Utilities.min(shrub.cubes.size() - 1, getAmount()); i > 0; i--) {
                colors[shrub.cubes.get(i).index] = colors[shrub.cubes.get((i + offset) % shrub.cubes.size()).index];
            }
        }
    }
}
