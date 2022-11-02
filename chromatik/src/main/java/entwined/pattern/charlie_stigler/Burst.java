package entwined.pattern.charlie_stigler;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.QuadraticEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class Burst extends LXPattern {
  static final float MAX_R = 1480;

  static final float BURST_PERIOD_MS = 3000;
  static final float PAUSE_LENGTH_INCHES = 1000;
  static final float DEFAULT_FADE_LENGTH_INCHES = 400;
  static float[] HUES = {131, 200, 277, 100, 360, 157, 304, 232, 325, 65, 185, 23};

  final QuadraticEnvelope radiusModulator = new QuadraticEnvelope(0, MAX_R + PAUSE_LENGTH_INCHES, BURST_PERIOD_MS);
  final BoundedParameter satParam = new BoundedParameter("Saturation", 100, 0, 100);
  final BoundedParameter fadeParam = new BoundedParameter("Fade", DEFAULT_FADE_LENGTH_INCHES, 0, MAX_R);
  final SawLFO colorSelectionSaw = new SawLFO(0, HUES.length, BURST_PERIOD_MS * HUES.length);

  // Constructor
  public Burst(LX lx) {
    super(lx);

    addModulator(colorSelectionSaw).start();

    radiusModulator.setEase(QuadraticEnvelope.Ease.IN);
    radiusModulator.setLooping(true);
    addModulator(radiusModulator).start();

    addParameter("saturation", satParam);
    addParameter("fade", fadeParam);
  }

  @Override
  public void run(double deltaMs) {
    float burstRadius = radiusModulator.getValuef();
    float fadeParamVal = fadeParam.getValuef();
    for (LXPoint cube : model.points) {
        int currentIndex = (int)Math.min(Math.floor(colorSelectionSaw.getValuef()), HUES.length - 1);
        int prevIndex = (currentIndex > 0) ? (currentIndex - 1) : (HUES.length - 1);

        float sat = satParam.getValuef();
        float brightness = 100;

        int cubeColor;

        // XXX - okay, what is cube.gr?
        // Appears to be the radius from the global center - ie,
        // cube.x * cube.x + cube.y * cube.y + cube.z * cube.z
        float cubeDistanceSquared = cube.x*cube.x + cube.y*cube.y + cube.z*cube.z;
        // Boolean inRadius = cube.gr < radiusModulator.getValuef();
        Boolean inRadius = cubeDistanceSquared < burstRadius * burstRadius;
        // if we're inside the "spread radius" of the burst, we get the new color
        // BUT we're gonna fade it a bit on the edges so it looks purty
        if (inRadius) {
          float cubeDistance = (float)Math.sqrt(cubeDistanceSquared);
          float fadeRatio = Math.min((burstRadius - cubeDistance) / fadeParamVal, 1);
          // float invFadeRatio = 1 - fadeRatio;

          int curColor = LX.hsb(HUES[currentIndex], sat, brightness);
          int prevColor = LX.hsb(HUES[prevIndex], sat, brightness);
          int blendedColor = LXColor.lerp(prevColor, curColor, fadeRatio);

          cubeColor = blendedColor;
        } else {
          cubeColor = LX.hsb(HUES[prevIndex], sat, brightness);
        }
        colors[cube.index] = cubeColor;
      }
  }
}
