package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;
import toxi.geom.Vec2D;

@LXCategory("Irene Zhou")
public class Cells extends EntwinedBasePattern {
    final BoundedParameter speed = new BoundedParameter("SPEED", 1, 0, 5);
    final BoundedParameter width = new BoundedParameter("WIDTH", 0.75, 0.5, 1.25);
    final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
    final int NUM_SITES = 15;
    private Site[] sites = new Site[NUM_SITES];

    private class Site {
        public float theta = 0;
        public float yPos = 0;
        public Vec2D velocity = new Vec2D(0, 0);

        public Site() {
            theta = Utilities.random(0, 360);
            yPos = Utilities.random(model.yMin, model.yMax);
            velocity = new Vec2D(Utilities.random(-1, 1), Utilities.random(-1, 1));
        }

        public void move(float speed) {
            theta = (theta + speed * velocity.x) % 360;
            yPos += speed * velocity.y;
            if ((yPos < model.yMin - 20) || (yPos > model.yMax + 20)) {
                velocity.y *= -1;
            }
        }
    }

    public Cells(LX lx) {
        super(lx);
        addParameter(speed);
        addParameter(width);
        addParameter(hue);
        for (int i = 0; i < sites.length; ++i) {
            sites[i] = new Site();
        }
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        for (BaseCube cube : model.baseCubes) {
            float minDistSq = 1000000;
            float nextMinDistSq = 1000000;
            for (int i = 0; i < sites.length; ++i) {
                if (Utilities.abs(sites[i].yPos - cube.transformedY)
                        < 150) { // restraint on calculation
                    float distSq =
                            Utilities.pow(
                                            (LXUtils.wrapdistf(
                                                    sites[i].theta, cube.transformedTheta, 360)),
                                            2)
                                    + Utilities.pow(sites[i].yPos - cube.transformedY, 2);
                    if (distSq < nextMinDistSq) {
                        if (distSq < minDistSq) {
                            nextMinDistSq = minDistSq;
                            minDistSq = distSq;
                        } else {
                            nextMinDistSq = distSq;
                        }
                    }
                }
            }
            colors[cube.index] =
                    LX.hsb(
                            (lx.engine.palette.getHuef() + hue.getValuef()) % 360,
                            100,
                            Utilities.max(
                                    0,
                                    Utilities.min(
                                            100,
                                            100 - Utilities.sqrt(nextMinDistSq - 2 * minDistSq))));
        }
        for (Site site : sites) {
            site.move(speed.getValuef() * (float) deltaMs * 60 / 1000);
        }
    }
}
