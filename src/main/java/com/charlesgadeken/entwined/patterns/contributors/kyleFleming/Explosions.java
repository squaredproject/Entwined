package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;
import java.util.ArrayList;
import toxi.geom.Vec2D;

public class Explosions extends MultiObjectPattern<Explosion> {

    ArrayList<Explosion> explosions;

    public Explosions(LX lx) {
        this(lx, 0.5f);
    }

    public Explosions(LX lx, double speed) {
        super(lx, false);

        explosions = new ArrayList<Explosion>();

        frequency.setValue(speed);
    }

    BoundedParameter getFrequencyParameter() {
        return new BoundedParameter(
                "FREQ", .50, .1, 20); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_IN);
    }

    Explosion generateObject(float strength) {
        Explosion explosion = new Explosion(lx);
        explosion.origin =
                new Vec2D(
                        Utilities.random(360),
                        (float) LXUtils.random(model.yMin + 50, model.yMax - 50));
        explosion.hue = (int) Utilities.random(360);
        return explosion;
    }
}
