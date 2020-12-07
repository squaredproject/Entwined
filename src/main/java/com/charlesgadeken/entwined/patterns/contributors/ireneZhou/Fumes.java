package com.charlesgadeken.entwined.patterns.contributors.ireneZhou;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.BaseCube;
import com.charlesgadeken.entwined.patterns.EntwinedTriggerablePattern;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;
import toxi.geom.Vec2D;

@LXCategory("Irene Zhou")
public class Fumes extends EntwinedTriggerablePattern {
    final BoundedParameter speed = new BoundedParameter("SPEED", 2, 0, 20);
    final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
    final BoundedParameter sat = new BoundedParameter("SAT", 25, 0, 100);
    final int NUM_SITES = 15;
    private Site[] sites = new Site[NUM_SITES];

    private class Site {
        public float theta = 0;
        public float yPos = 0;
        public Vec2D velocity = new Vec2D(0, 0);

        public Site() {
            theta = Utilities.random(0, 360);
            yPos = Utilities.random(model.yMin, model.yMax);
            velocity = new Vec2D(Utilities.random(0, 1), Utilities.random(0, 0.75f));
        }

        public void move(float speed) {
            theta = (theta + speed * velocity.x) % 360;
            yPos += speed * velocity.y;
            if (yPos < model.yMin - 50) {
                velocity.y *= -1;
            }
            if (yPos > model.yMax + 50) {
                yPos = model.yMin - 50;
            }
        }
    }

    public Fumes(LX lx) {
        super(lx);
        addParameter(hue);
        addParameter(speed);
        addParameter(sat);
        for (int i = 0; i < sites.length; ++i) {
            sites[i] = new Site();
        }
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        float minSat = sat.getValuef();
        for (BaseCube cube : model.baseCubes) {
            float minDistSq = 1000000;
            float nextMinDistSq = 1000000;
            for (int i = 0; i < sites.length; ++i) {
                if (Utilities.abs(sites[i].yPos - cube.transformedY)
                        < 150) { // restraint on calculation
                    float distSq =
                            Utilities.pow(
                                            (LXUtils.wrapdistf(
                                                    sites[i].theta, cube.getTransformedTheta(), 360)),
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
            float brt = Utilities.max(0, 100 - Utilities.sqrt(nextMinDistSq));
            colors[cube.index] =
                    lx.hsb(
                            (lx.engine.palette.getHuef() + hue.getValuef()) % 360,
                            100 - Utilities.min(minSat, brt),
                            brt);
        }
        for (Site site : sites) {
            site.move(speed.getValuef() * (float) deltaMs * 60 / 1000);
        }
    }
}
