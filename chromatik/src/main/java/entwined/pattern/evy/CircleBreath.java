package entwined.pattern.evy;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

/*
 * A circle expanding in and out from the origin, inspired the "breath" pattern and misc breathing exercise graphics i've seen
 */
public class CircleBreath extends LXPattern {
  static final float MAX_R = 1480;
  static final float PAUSE_LENGTH_INCHES = 1000;
  static final float FADE_LENGTH = 800; // the width of the part of the circle that fades
  static final float MAX_BRIGHTNESS = 80;


  final CompoundParameter hue = new CompoundParameter("HUE", 200, 0, 360);
  // have the circle go further than MAX_R so it pauses in full brightness
  final TriangleLFO radiusModulator = new TriangleLFO(0, MAX_R + PAUSE_LENGTH_INCHES, 10000);

  // Constructor
  public CircleBreath(LX lx) {
    super(lx);
    addModulator(radiusModulator).start();
    addParameter("hue", hue);
  }

  @Override
  public void run(double deltaMs) {
      for (LXPoint cube : model.points) {
        float brightness;

        // if we're deep inside the expanding/contracting circle, we do full brightness
        if (cube.r < radiusModulator.getValuef() - FADE_LENGTH) {
          brightness = MAX_BRIGHTNESS;
        }
        // a ring of fade at the edge of the expanding/contracting circle
        else if (cube.r < radiusModulator.getValuef()) {
          float fade = (cube.r - (radiusModulator.getValuef() - FADE_LENGTH)) / FADE_LENGTH;
          brightness = (1 - fade) * MAX_BRIGHTNESS;
        }
        // outside the expanding/contracting circle is dark
        else {
          brightness = 0;
        }

        colors[cube.index] = LX.hsb(hue.getValuef(), 100, brightness);
      }
  }
}


