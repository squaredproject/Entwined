package entwined.pattern.colin_hunt;

import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

/*
 * XXX - need subfixtures for tree layers/branches. */

public class ChristmasTree extends LXPattern {
  double cubeNoise = 0;

  public ChristmasTree(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {

    for (LXPoint cube : model.points) {
      cubeNoise = SimplexNoise.noise(cube.x, cube.y, cube.z);

      if (cubeNoise > .80) {
        colors[cube.index] = LX.hsb(240, 100, 100);
      } else if (cubeNoise > .65) {
        colors[cube.index] = LX.hsb(30, 100, 100);
      } else if (cubeNoise > .45) {
        colors[cube.index] = LX.hsb(0, 100, 100);
      } else {
        colors[cube.index] = LX.hsb((float) (cubeNoise * 20 + 120), 100, 90);
      }
    }

    for (LXModel level_2: model.sub("LAYER_1")) {
      for (LXPoint cube : level_2.points) {
        colors[cube.index] = LX.hsb(60, 100, 100);
      }
    }
  }
}
