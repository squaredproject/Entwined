import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.modulator.QuadraticEnvelope;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import toxi.geom.Vec2D;
import org.apache.commons.lang3.ArrayUtils;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;


/*
 * A circle expanding in and out from the origin, inspired the "breath" pattern and misc breathing exercise graphics i've seen
 */
class CircleBreath extends TSPattern {
  static final float MAX_R = 1480;
  static final float PAUSE_LENGTH_INCHES = 1000;
  static final float FADE_LENGTH = 800; // the width of the part of the circle that fades
  static final float MAX_BRIGHTNESS = 80;


  final BasicParameter hue = new BasicParameter("HUE", 200, 0, 360);
  // have the circle go further than MAX_R so it pauses in full brightness
  final TriangleLFO radiusModulator = new TriangleLFO(0, MAX_R + PAUSE_LENGTH_INCHES, 10000);

  // Constructor
  CircleBreath(LX lx) {
    super(lx);
    addModulator(radiusModulator).start();
    addParameter(hue);
  }

  public void run(double deltaMs) {
      for (BaseCube cube : model.baseCubes) {
        float brightness;

        // if we're deep inside the expanding/contracting circle, we do full brightness
        if (cube.gr < radiusModulator.getValuef() - FADE_LENGTH) {
          brightness = MAX_BRIGHTNESS;
        }
        // a ring of fade at the edge of the expanding/contracting circle
        else if (cube.gr < radiusModulator.getValuef()) {
          float fade = (cube.gr - (radiusModulator.getValuef() - FADE_LENGTH)) / FADE_LENGTH;
          brightness = (1 - fade) * MAX_BRIGHTNESS;
        }
        // outside the expanding/contracting circle is dark
        else {
          brightness = 0;
        }

        colors[cube.index] = lx.hsb(hue.getValuef(), 100, brightness);
      }
  }
}


// inspired by https://ncase.me/fireflies/
// TODO in the future if we add interactivity that supports this,
//    maybe someone could click a button on a shrub and that would make
//    that shrub light up (by changing the shrub's lightUpOffset)
class FirefliesNcase extends TSPattern {

  final BasicParameter hue = new BasicParameter("HUE", 52, 0, 360);
  final SawLFO cycle = new SawLFO(0, 100, 3000); // fireflies flash every 3 seconds
  // neighbouring fireflies must be within this distance to have their clock move forward
  final BasicParameter flyRadius = new BasicParameter("FLY_RADIUS", 1000, 0, 2000);

  // maps the shrub id to the offset from `cycle`, that determines when it uniquely lights up
  static final Map<Integer, Float> lightUpOffset = new HashMap();
  // keeps track of if a shrub is lit up, so we can change nearby shrubs' offsets during transitions
  static final Map<Integer, Boolean> isLitUp = new HashMap();


  // To reset approx every `resetInterval` ms, we run a saw wave over that time period.
  // If `reset == false` and `resetTimer < 0` then we switch `reset = true` and reset.
  // Once `resetTimer > 0` we flip `reset` back to false, to be ready for the next cycle.
  // (If anyone has any ideas for cleaner ways to do this I'd love to hear them, since this feels convoluted.)
  final BasicParameter resetInterval = new BasicParameter("RESET_INTERVAL", 10000, 0, 30000);
  final SawLFO resetTimer = new SawLFO(-1, 1, resetInterval);
  static Boolean reset = true; // start with true since we run `reset` in the constructor

  void reset() {
    for (Shrub shrub : model.shrubs) {
      lightUpOffset.put(shrub.index, Utils.random(0, 100));
      isLitUp.put(shrub.index, false);
    }
  }

  FirefliesNcase(LX lx) {
    super(lx);
    addModulator(cycle).start();
    addParameter(hue);
    addParameter(resetInterval);
    addModulator(resetTimer).start();
    reset();
  }

  Boolean closeEnough(float fromX, float toX, float fromZ, float toZ){
    float dx = fromX - toX;
    float dz = fromZ - toZ;
    float dist = dx*dx + dz*dz;
    return (dist < flyRadius.getValuef()*flyRadius.getValuef());
  }

  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
      // see above comment for how reset works
      if (reset == false && resetTimer.getValuef() < 0) {
        reset();
        reset = true;
      }
      else if (reset == true && resetTimer.getValuef() > 0) {
        reset = false;
      }

      for (BaseCube cube : model.baseCubes) {
        float sculptureCycleValue = (cycle.getValuef() + lightUpOffset.get(cube.sculptureIndex)) % 100;

        // shrubs are lit when the cycle value is between 0 and 40
        // (influenced by both their own random number and the cycling modulator)
        if (0 < sculptureCycleValue && sculptureCycleValue < 40) {
          colors[cube.index] = lx.hsb(hue.getValuef(), 100, (40 - sculptureCycleValue)/40 * 100);

          // When a shrub transitions to being lit up, loop through all shrubs and move the
          // clocks of close ones forward.
          // I copied this from https://github.com/ncase/fireflies/blob/gh-pages/js/index.js#L234
          // and though its logic seems sort of sketchy, it does look cool, so... seems fine.
          if(!isLitUp.get(cube.sculptureIndex)) {
            isLitUp.put(cube.sculptureIndex, true);
            for (Shrub shrub : model.shrubs) {
              if (shrub.index == cube.sculptureIndex) continue;
              if (closeEnough(shrub.x, cube.x, shrub.z, cube.z)) {
                float newOffset = (lightUpOffset.get(shrub.index) + 5  * lightUpOffset.get(shrub.index)/100);
                if (newOffset > 100) {
                  newOffset = 100; // this feels like cheating, but it also doesn't work without it :shrug:
                }
                lightUpOffset.put(shrub.index, newOffset);
              }
            }
          }
        }
        // this one's simpler - if their shrub isn't lit up, then it's dark
        else {
          colors[cube.index] = lx.hsb(0, 0, 0);
          if (isLitUp.get(cube.sculptureIndex)) {
            isLitUp.put(cube.sculptureIndex, false);
          }
        }
      }
  }
}

