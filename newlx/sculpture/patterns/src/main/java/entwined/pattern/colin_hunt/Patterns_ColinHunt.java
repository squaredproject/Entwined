package entwined.pattern.colin_hunt;

import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

/*
 * XXX - need subfixtures for tree layers/branches. */

class ChristmasTree extends LXPattern {
  double cubeNoise = 0;

  ChristmasTree(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs) {

    // Use a for loop here to set the cube colors
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

    // It's OK this is only cubes, because it's just the tree cubes
    //  XXX - Yes, i also need layers as separate fixture objects.
    for (LXModel tree: model.sub("TREE")) {  // XXX - get sub BRANCH
      for (LXPoint cube : model.points) {  // not points
        // will become if model.meta.get("layer") == "2" ...
        if (cube.config.layerIndex == 2) {
          colors[cube.index] = LX.hsb(60, 100, 100);
        }
      }
    }
  }
}


/* Working example of simplex noise for me because I am dumb
class Simplex extends LXPattern {
  SimplexNoise noize = new SimplexNoise();
  float xOff = 0;
  float zOff = 0;
  float a = 0;

  Simplex(LX lx) {
    super(lx);

  }

  public void run(double deltaMs) {
      a += .001;
      xOff = (float)Math.cos(a);
      zOff = (float)Math.sin(a);

      // Use a for loop here to set the cube colors
      for (LXPoint cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( Utils.map((float)noize.noise(cube.x + xOff, cube.z + zOff), -1, 1, 0, 360), 100, 100);
      }

      if (a > Utils.TWO_PI) {
        a = 0;
      }
  }

}
*/
