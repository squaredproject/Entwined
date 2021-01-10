import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

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
