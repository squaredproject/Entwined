import heronarts.lx.LX;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;

import java.util.Random;

/**
   Sparkle wave

   This pattern creates a wave of brightness from the central tree. The trailing edge of the wave
   has a "sparkle" effect.
  */
class SparkleWave extends TSPattern {
  float time = 0;
  float[] sparkleMap;
  int sparkleDecayRate = 5;
  int sparkleRangeBase = 15;
  int brightnessDecayThreshold = 50;
  int baseSaturation = 50;
  int sparkleBrightness = 90;

  final BasicParameter speedParam = new BasicParameter("Speed", 50, 0, 100);
  final BasicParameter sizeParam = new BasicParameter("Size", 35, 0, 50);
  final BasicParameter sparkleWidth = new BasicParameter("Sparkle width", 5, 1, 50);
  final BasicParameter saturationThreshold = new BasicParameter("Saturation threshold", 0.99, 0.1, 0.99999);
  final BooleanParameter waveDirection = new BooleanParameter("Direction", true);

  SparkleWave(LX lx) {
    super(lx);
      addParameter(speedParam);
      addParameter(sizeParam);
      addParameter(sparkleWidth);
      addParameter(saturationThreshold);
      addParameter(waveDirection);

      sparkleMap = new float[model.baseCubes.size()];
  }

  public void run(double deltaMs) {
    // waveDirection = true -- wave emits out from central tree
    // waveDirection = false -- wave flows in towards central tree
    if (waveDirection.getValueb()) {
      time += deltaMs;
    } else {
      time -= deltaMs;
    }

    // Step through each cube in the model via a for loop
    for (BaseCube cube : model.baseCubes) {
      int index = cube.index;

      float saturation = baseSaturation;
      float brightness = calcBrightness(time, cube);

      if (shouldStartSparkle(brightness)) {
        double random = Math.random();
        if (random > saturationThreshold.getValuef()) {
          brightness = sparkleBrightness; // high brightness = "sparkle"
          // Enter brightness value into the `sparkleMap` array so the brightness decays
          // outside of the loop
          sparkleMap[index] = brightness;
          // Adjust the saturation value slightly to give a little variation
          saturation *= saturationModifier();
        }
      }

      // If we're above the decay threshold, decrease sparkle brightness.
      // Don't let the brightness decay lower than the brightnessDecayThreshold
      // (aka don't let it drop to 0).
      // If we let it drop to 0, the cube turns black, rather than "staying" with the wave.
      if(sparkleMap[index] > brightnessDecayThreshold) {
        // Set the new value of the cube in the sparkleMap array to slightly lower than it was before.
        sparkleMap[index] -= sparkleDecayRate;
        saturation = saturation / 4; // divide so the saturation is closer to 0, aka whiter.
        brightness = sparkleMap[index];
      }

      colors[cube.index] = lx.hsb(cube.theta, saturation, brightness);
    }
  }

  public float saturationModifier() {
    float max = 0.6f;
    float range = 0.4f;
    float modifier = max + (float) Math.random() * range;
    return modifier;
  }

  public float calcBrightness(float time, BaseCube cube) {
    // How fast the wave moves
    float waveSpeed = time/speedParam.getValuef();

    // How wide the wave is
    float waveWidth = cube.gr/sizeParam.getValuef();

    return Math.abs(waveWidth - waveSpeed) % 100;
  }

  public boolean shouldStartSparkle(float brightness) {
    float sparkleRangeMin = sparkleRangeBase - sparkleWidth.getValuef();
    float sparkleRangeMax = sparkleRangeBase + sparkleWidth.getValuef();

    return brightness > sparkleRangeMin && brightness < sparkleRangeMax;
  }
}
