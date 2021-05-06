package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import heronarts.lx.LX;
import heronarts.lx.utils.LXUtils;
import toxi.geom.Vec2D;

class RainDrop extends MultiObject {

    float theta;
    float startY;
    float endY;

    RainDrop(LX lx) {
        super(lx);
        shouldFade = false;
    }

    public void onProgressChanged(float progress) {
        currentPoint = new Vec2D(theta, (float) LXUtils.lerp(startY, endY, progress));
    }
}
