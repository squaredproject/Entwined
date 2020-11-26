package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.utils.LXUtils;
import toxi.geom.Vec2D;

public class Wisps extends MultiObjectPattern<Wisp> {

    final BoundedParameter baseColor = new BoundedParameter("COLR", 210, 360);
    final BoundedParameter colorVariability = new BoundedParameter("CVAR", 10, 180);
    final BoundedParameter direction = new BoundedParameter("DIR", 90, 360);
    final BoundedParameter directionVariability = new BoundedParameter("DVAR", 20, 180);
    final BoundedParameter thickness =
            new BoundedParameter(
                    "WIDT", 3.5f, 1, 20); // TODO(meawoppl) BoundedParameter.Scaling.QUAD_IN);
    final BoundedParameter speed =
            new BoundedParameter(
                    "SPEE", 10, 1, 20); // TODO(meawoppl), BoundedParameter.Scaling.QUAD_IN);

    // Possible other parameters:
    //  Distance
    //  Distance variability
    //  width variability
    //  Speed variability
    //  frequency variability
    //  Fade time

    public Wisps(LX lx) {
        this(lx, .5f, 210, 10, 90, 20, 3.5f, 10);
    }

    Wisps(
            LX lx,
            double initial_frequency,
            double initial_color,
            double initial_colorVariability,
            double initial_direction,
            double initial_directionVariability,
            double initial_thickness,
            double initial_speed) {
        super(lx, initial_frequency);

        addParameter(baseColor);
        addParameter(colorVariability);
        addParameter(direction);
        addParameter(directionVariability);
        addParameter(thickness);
        addParameter(speed);

        baseColor.setValue(initial_color);
        colorVariability.setValue(initial_colorVariability);
        direction.setValue(initial_direction);
        directionVariability.setValue(initial_directionVariability);
        thickness.setValue(initial_thickness);
        speed.setValue(initial_speed);
    };

    Wisp generateObject(float strength) {
        Wisp wisp = new Wisp(lx);
        wisp.runningTimerEnd = 5000 / speed.getValuef();
        float pathDirection =
                (float)
                                (direction.getValuef()
                                        + LXUtils.random(
                                                -directionVariability.getValuef(),
                                                directionVariability.getValuef()))
                        % 360;
        float pathDist = (float) LXUtils.random(200, 400);
        float startTheta = Utilities.random(360);
        float startY =
                (float)
                        LXUtils.random(
                                Utilities.max(
                                        model.yMin,
                                        model.yMin
                                                - pathDist
                                                        * Utilities.sin(
                                                                Utilities.PI
                                                                        * pathDirection
                                                                        / 180)),
                                Utilities.min(
                                        model.yMax,
                                        model.yMax
                                                - pathDist
                                                        * Utilities.sin(
                                                                Utilities.PI
                                                                        * pathDirection
                                                                        / 180)));
        wisp.startPoint = new Vec2D(startTheta, startY);
        wisp.endPoint = Vec2D.fromTheta(pathDirection * Utilities.PI / 180);
        wisp.endPoint.scaleSelf(pathDist);
        wisp.endPoint.addSelf(wisp.startPoint);
        wisp.hue =
                (int)
                                (baseColor.getValuef()
                                        + LXUtils.random(
                                                -colorVariability.getValuef(),
                                                colorVariability.getValuef()))
                        % 360;
        wisp.thickness = 10 * thickness.getValuef() + (float) LXUtils.random(-3, 3);

        return wisp;
    }
}
