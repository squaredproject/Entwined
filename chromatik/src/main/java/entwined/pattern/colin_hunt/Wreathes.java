package entwined.pattern.colin_hunt;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

import entwined.utils.SimplexNoise;

/**
A simple holiday pattern
*/
public class Wreathes extends LXPattern {

  public Wreathes(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

      // Use a for loop here to set the cube colors
      for (LXPoint cube : model.points) {
        if (SimplexNoise.noise(cube.x, cube.y, cube.z) < .5) {
          colors[cube.index] = LX.hsb(120, 100, 100);
        } else {
          colors[cube.index] = LX.hsb(0, 100, 100);
        }
      }
  }
}
