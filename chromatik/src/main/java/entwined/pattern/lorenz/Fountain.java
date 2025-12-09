package entwined.pattern.lorenz;

import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;

// All shrubs together look like a water surface with waves changing the illuminated height
// of each shrub. Sometimes a random tree starts cycling through all colors to pick a random
// new color and then pulses with the new color to sprinkle the new color on all shrubs.

public class Fountain extends LXPattern {
  enum State {
    WAIT,                 // Everything has the same color and waives run through all shrubs.
    PICK_NEW_TREE_COLOR,  // One tree cycles through all colors and stops on a new color.
    UPDATE_SHRUBS_HUE     // The tree with the new color pulses and sprinkles the new color everywhere.
  }

  // Variable Declarations go here
  private State state = State.WAIT;
  private double waitDurationMs = 3000;
  private double pickNewTreeColoroDurationMs = 3000;

  // Randomly picked index of the next tree used to update the color.
  private int treeIndexNewColor = 0;

  // Radial distance from tree with new color used to update colors of shrubs. This
  // distance increases linearly during the UPDATE_SHRUBS_HUE state.
  private float rUpdateShrubColor = 0;
  private float rPulsingRing = 0;  // Radial position of pulsing ring in UPDATE_SHRUBS_HUE state
  private float rPulsingRingDelta = 0;  // How fast the ring position changes used to increase the pulsing speed.

  // x/z global coordinates of the tree that is changing colors. This is the position
  // from which to spread the new color to all shrubs.
  private float newColorTreeX = 0;
  private float newColorTreeZ = 0;

  // x/z global coordinates of the three trees.
  private float[] treesX = {0.0f, 0.0f, 0.0f};
  private float[] treesZ = {0.0f, 0.0f, 0.0f};

  private float shrubXmin = 100000;
  private float shrubXmax = -100000;
  private float shrubYmin = 100000;
  private float shrubYmax = -100000;
  private float shrubZmin = 100000;
  private float shrubZmax = -100000;

  private float treeYmin = 100000;
  private float treeYmax = -100000;

  private float midWaterLevel = 0;
  private float waveHeight = 0;

  private float treeHue = 0;
  private float shrubHue = 180;

  private double nextStateTimerMs = 0;

  // Speed of the main wave running over the shrubs in x-direction.
  final CompoundParameter waveParam = new CompoundParameter("Wave", 4, 1, 10);

  // Height of the smaller ripple wave running over the shrubs in z-direction.
  final CompoundParameter rippleParam = new CompoundParameter("Ripple", 0.8, 0, 5);

  // A slow large wave to run across the shrubs in global x-axis.
  final SawLFO wave = new SawLFO(0, 2 * Math.PI, 3000);

  // A faster small ripple on top of the main wave in global z-axis.
  final SawLFO ripple = new SawLFO(0, 2 * Math.PI, 1000);

  // A small offset to the hue so that even in WAIT state the colors chnage slightly.
  final SinLFO hueOffset = new SinLFO(0, 20, 10000);

  // A small offset to the ring position so trees are not static.
  final SinLFO ringOffset = new SinLFO(0, 40, 3000);

  // Constructor and initial setup
  // Remember to use addParameter and addModulator if you're using Parameters or sin waves
  public Fountain(LX lx) {
    super(lx);

    addModulator(wave).start();
    addModulator(ripple).start();
    addModulator(hueOffset).start();
    addModulator(ringOffset).start();

    addParameter("wave", waveParam);
    addParameter("ripple", rippleParam);

    for (LXModel shrub : model.sub("SHRUB")) {
      for (LXPoint cube : shrub.points) {
        shrubXmin = Math.min(shrubXmin, cube.x);
        shrubXmax = Math.max(shrubXmax, cube.x);
        shrubYmin = Math.min(shrubYmin, cube.y);
        shrubYmax = Math.max(shrubYmax, cube.y);
        shrubZmin = Math.min(shrubZmin, cube.z);
        shrubZmax = Math.max(shrubZmax, cube.z);
      }
    }
    int treeIdx = 0;
    for (LXModel tree : model.sub("BIG_TREE")) {
      // Find the world position of each tree.
      treesX[treeIdx] = tree.cx;
      treesZ[treeIdx] = tree.cy;
      for (LXPoint cube : tree.points) {
        treeYmin = Math.min(treeYmin, cube.y);
        treeYmax = Math.max(treeYmax, cube.y);
      }
      treeIdx++;
    }
    // System.out.printf("shrubX min: %.2f  max: %.2f %n", shrubXmin, shrubXmax);
    // System.out.printf("shrubY min: %.2f  max: %.2f %n", shrubYmin, shrubYmax);
    // System.out.printf("shrubZ min: %.2f  max: %.2f %n", shrubZmin, shrubZmax);
    // System.out.printf("treeYmin: %.2f  treeYmax: %.2f %n", treeYmin, treeYmax);
    // System.out.printf("tree0X: %.2f  Z: %.2f %n", treesX[0], treesZ[0]);
    // System.out.printf("tree1X: %.2f  Z: %.2f %n", treesX[1], treesZ[1]);
    // System.out.printf("tree2X: %.2f  Z: %.2f %n", treesX[2], treesZ[2]);

    // Set the water level and wave height for the shrubs so that the level fluctuates
    // roughly between half full and full.
    float shrubYrange = shrubYmax - shrubYmin;
    midWaterLevel = shrubYmin + 0.8f * shrubYrange;
    waveHeight = 0.33f * shrubYrange;
  }


  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    nextStateTimerMs += deltaMs;

    if (state == State.WAIT) {
      if (nextStateTimerMs > waitDurationMs) {
        state = State.PICK_NEW_TREE_COLOR;
        nextStateTimerMs = 0;
        // Set the duration of cycling through colors to 4-7 seconds.
        pickNewTreeColoroDurationMs = 4000 + 3000 * Math.random();
        treeIndexNewColor = (int)(Math.random() * 3);
        newColorTreeX = treesX[treeIndexNewColor];
        newColorTreeZ = treesZ[treeIndexNewColor];
      }
    }
    if (state == State.PICK_NEW_TREE_COLOR) {
      rUpdateShrubColor = 0;

      float colorDifference = Math.abs(treeHue - shrubHue);
      if (colorDifference > 180) {
        colorDifference = 360 - colorDifference;
      }
      if (colorDifference > 70 && nextStateTimerMs > pickNewTreeColoroDurationMs) {
        state = State.UPDATE_SHRUBS_HUE;
        nextStateTimerMs = 0;
      }
      treeHue += deltaMs * 0.1;
      treeHue = treeHue % 360;
    }
    if (state == State.UPDATE_SHRUBS_HUE) {
      rUpdateShrubColor += deltaMs / 3;

      // Sprinkle teh new color everywhere for 14 seconds
      if (nextStateTimerMs < 14000) {
        // Increase the pulsing speed during the first 2 seconds.
        if (nextStateTimerMs < 2000) {
          rPulsingRingDelta += 0.006 * deltaMs;
        }
        // Decrease the pulsing speed during the last 2 seconds.
        else if (nextStateTimerMs > 12000) {
          rPulsingRingDelta -= 0.006 * deltaMs;
        }
        rPulsingRing += rPulsingRingDelta;
        // Pulsing ring moves from center of tree to 200 inches away.
        rPulsingRing = rPulsingRing % 200;
      } else {
        shrubHue = treeHue;
        state = State.WAIT;
        nextStateTimerMs = 0;
        rPulsingRing = 0;
        waitDurationMs = 4000 + 8000 * Math.random();
      }
    }

    wave.setPeriod(waveParam.getValuef() * 1000.0);

    // Use a for loop here to set the cube colors
    int treeIdx = 0;
    for (LXModel component : model.children) {
      for (LXPoint cube : component.points) {
        if (component.tags.contains("BIG_TREE")) {
          if (treeIdx == treeIndexNewColor) {
            // This is the tree that changes to the new color.
            float s = Math.max(0, 100 - Math.abs(cube.r - rPulsingRing - ringOffset.getValuef()));
            colors[cube.index] = LX.hsb(treeHue, s, 100);
          } else {
            // The other two trees.
            float s = Math.max(0, 100 - Math.abs(cube.r - ringOffset.getValuef()));
            float h = shrubHue;
            if (rUpdateShrubColor > cube.r) {
              float fade = Math.min(2000, rUpdateShrubColor - cube.r) / 2000;
              h = fade * treeHue + (1 - fade) * shrubHue;
            }
            colors[cube.index] = LX.hsb(h, s, 100);
          }
        } else {
          float waterLevelY = (float) (midWaterLevel
            + waveHeight * Math.sin(cube.x / 200.0 + wave.getValuef())
            + rippleParam.getValuef() * Math.sin(cube.z / 200.0 + ripple.getValuef()));

          // Fade out brightness for cubes at water level.
          float b = Math.min(100, 20 * (waterLevelY - cube.y));
          b = Math.max(0, b);

          // Make color more white (i.e. less saturated) at top as small sparkle effect.
          float s = Math.max(50, b*b*b/10000);
          float h = shrubHue;

          float distance = Math.abs(cube.x - newColorTreeX) + Math.abs(cube.z - newColorTreeZ);

          if (rUpdateShrubColor > distance) {
            // Let the new color sink in slowly from the top of each shrub.
            float sinkInDepth = 0.01f * (rUpdateShrubColor - distance);
            if (cube.y > waterLevelY - sinkInDepth)
            h = treeHue;
          }
          colors[cube.index] = LX.hsb(h + hueOffset.getValuef(), s, b);
        }
      }
      treeIdx++;
    }
  }
}
