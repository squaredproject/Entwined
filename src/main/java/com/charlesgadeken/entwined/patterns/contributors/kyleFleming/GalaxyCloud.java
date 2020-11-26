package com.charlesgadeken.entwined.patterns.contributors.kyleFleming;

import com.charlesgadeken.entwined.Utilities;
import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.LX;
import toxi.math.noise.PerlinNoise;

public class GalaxyCloud extends EntwinedBasePattern {

    double time = 0;

    public GalaxyCloud(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        // Blue to purple
        float hueMin = 240;
        float hueMax = 280;
        float hueMinExtra = 80;
        float hueMaxExtra = 55;

        float hueSpread = hueMax - hueMin;
        float hueMid = hueSpread / 2 + hueMin;
        float initialSpreadSize = hueMinExtra + hueMaxExtra;
        float initialSpreadMin = hueMid - hueMinExtra;

        time += deltaMs;
        for (Cube cube : model.cubes) {
            float adjustedTheta = cube.transformedTheta / 360;
            float adjustedY = (cube.transformedY - model.yMin) / (model.yMax - model.yMin);
            float adjustedTime = (float) time / 5000;

            // Use 2 textures so we don't have a seam. Interpolate between them between -45 & 45 and
            // 135 & 225
            PerlinNoise perlinNoise = new PerlinNoise();
            float hue1 = perlinNoise.noise(4 * adjustedTheta, 4 * adjustedY, adjustedTime);
            float hue2 =
                    perlinNoise.noise(
                            4 * ((adjustedTheta + 0.5f) % 1), 4 * adjustedY + 100, adjustedTime);
            float hue =
                    Utilities.lerp(
                            hue1,
                            hue2,
                            Utilities.min(
                                    Utilities.max(
                                            Utilities.abs(((adjustedTheta * 4 + 1) % 4) - 2) - 0.5f,
                                            0),
                                    1));

            float adjustedHue = hue * initialSpreadSize + initialSpreadMin;
            hue = Utilities.min(Utilities.max(adjustedHue, hueMin), hueMax);

            // make it black if the hue would go below hueMin or above hueMax
            // normalizedFadeOut: 0 = edge of the initial spread, 1 = edge of the hue spread, >1 =
            // in the hue spread
            float normalizedFadeOut =
                    (adjustedHue - hueMid + hueMinExtra) / (hueMinExtra - hueSpread / 2);
            // scaledFadeOut <0 = black sooner, 0-1 = fade out gradient, >1 = regular color
            float scaledFadeOut = normalizedFadeOut * 5 - 4.5f;
            float brightness = Utilities.min(Utilities.max(scaledFadeOut, 0), 1) * 100;

            // float brightness = Utilities.min(Utilities.max((float)SimplexNoise.noise(4 *
            // adjustedX, 4 * adjustedY, 4 * adjustedZ + 10000, adjustedTime) * 8 - 1, 0), 1) * 100;

            colors[cube.index] = lx.hsb(hue, 100, brightness);
        }
    }
}
