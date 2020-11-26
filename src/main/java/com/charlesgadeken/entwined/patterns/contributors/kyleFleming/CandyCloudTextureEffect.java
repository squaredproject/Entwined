package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import com.charlesgadeken.entwined.model.Cube;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BoundedParameter;
import toxi.math.noise.SimplexNoise;

public class CandyCloudTextureEffect extends EntwinedBaseEffect {
    final BoundedParameter amount = new BoundedParameter("CLOU");

    double time = 0;
    final double scale = 2400;
    final double speed = 1.0f / 5000;

    public CandyCloudTextureEffect(LX lx) {
        super(lx);
    }

    public void run(double deltaMs, double unused) {
        if (amount.getValue() > 0) {
            time += deltaMs;
            for (int i = 0; i < colors.length; i++) {
                int oldColor = colors[i];
                Cube cube = model.cubes.get(i);

                double adjustedX = cube.x / scale;
                double adjustedY = cube.y / scale;
                double adjustedZ = cube.z / scale;
                double adjustedTime = time * speed;

                float newHue =
                        ((float) SimplexNoise.noise(adjustedX, adjustedY, adjustedZ, adjustedTime)
                                        + 1)
                                / 2
                                * 1080
                                % 360;
                int newColor = lx.hsb(newHue, 100, 100);

                int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
                colors[i] =
                        lx.hsb(
                                LXColor.h(blendedColor),
                                LXColor.s(blendedColor),
                                LXColor.b(oldColor));
            }
        }
    }
}
