package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import heronarts.lx.LX;
import toxi.geom.Vec2D;

class Wisp extends MultiObject {

    Vec2D startPoint;
    Vec2D endPoint;

    Wisp(LX lx) {
        super(lx);
    }

    public void onProgressChanged(float progress) {
        currentPoint = startPoint.interpolateTo(endPoint, progress);
    }
}
