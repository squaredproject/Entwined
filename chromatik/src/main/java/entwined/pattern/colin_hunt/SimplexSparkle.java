package entwined.pattern.colin_hunt;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

import entwined.utils.SimplexNoise;

/**
Star twinkle using Simplex Noise
*/
public class SimplexSparkle extends LXPattern {
  float xHueOff = 0;
  float zHueOff = 0;
  float xSatOff = 0;
  float zSatOff = 0;
  float xBrightOff = 0;
  float zBrightOff = 0;
  float aH = 0;
  float aS = 0;
  float aB = 0;

  float hue = 230;

  public SimplexSparkle(LX lx) {
    super(lx);

  }

  @Override
  public void run(double deltaMs) {
      aH += .01;
      aB += .003;
      aS += .0065;
      hue += .1;
      // xOff = (float)Math.cos(a);
      // zOff = (float)Math.sin(a);
      xHueOff = (float)Math.cos(aH);
      zHueOff = (float)Math.sin(aH);
      xSatOff = (float)Math.cos(aS);
      zSatOff = (float)Math.sin(aS);
      xBrightOff = (float)Math.cos(aB);
      zBrightOff = (float)Math.sin(aB);


      // Use a for loop here to set the cube colors
      for (LXPoint cube : model.points) {
        colors[cube.index] = LX.hsb(getHue(cube.x, cube.z), getSat(cube.x, cube.z), getBright(cube.x, cube.z));
      }

      if (hue >= 360) {
        hue = 0;
      }
      if (aS > LX.TWO_PI) {
        aS = 0;
      }
      if (aB > LX.TWO_PI) {
        aB = 0;
      }
  }

  public float getHue(float cx, float cz) {
    // Calculate
    float toReturn = hue + (float)SimplexNoise.noise(cx + xHueOff, cz + zHueOff) * 45;

    // Constrain
    toReturn = hue % 360;

    // return
    return toReturn;

  }

  public float getSat(float cx, float cz) {

    // Calculate
    float toReturn = 50 +  (float)SimplexNoise.noise(cx + xSatOff, cz + zSatOff) * 100;

    // Constrain
    toReturn = Math.min(Math.max(toReturn, 0), 100);

    // return
    return toReturn;
  }

  public float getBright(float cx, float cz) {

    // Calculate
    float toReturn = 50 +  (float)SimplexNoise.noise(cx + xBrightOff, cz + zBrightOff) * 100;

    // Constrain
    toReturn = Math.min(Math.max(toReturn, 0), 100);

    // return
    return toReturn;
  }

}
