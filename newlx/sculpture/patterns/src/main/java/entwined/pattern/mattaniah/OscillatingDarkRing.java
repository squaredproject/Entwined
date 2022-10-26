package entwined.pattern.mattaniah;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.pattern.LXPattern;

public class OscillatingDarkRing extends LXPattern {
  // Variable declarations, parameters, and modulators go here
    static final float MAX_R = 1480;
  static final float PAUSE_LENGTH_INCHES = 1000;
  static final float FADE_LENGTH = 800; // the width of the part of the circle that fades
  static final float MAX_BRIGHTNESS = 100;

  // This will create a sin wave object that we will use to control brightness
  // Pass in the minimum sin value, maximum sin value, and the duration in milliseconds
  final SinLFO brightSin = new SinLFO(0, 100, 2000);

  // This will create a sin wave object that we will use to control brightness
  // Pass in the minimum sin value, maximum sin value, and the duration in milliseconds
  final SawLFO hueSaw = new SawLFO(0, 100, 5000);
  final SinLFO hueSin = new SinLFO(0, 360, 40000);


   // have the circle go further than MAX_R so it pauses in full brightness
   final TriangleLFO radiusModulator = new TriangleLFO(0, MAX_R + PAUSE_LENGTH_INCHES, 5000);

  // Constructor
  public OscillatingDarkRing(LX lx) {
    super(lx);

    // This modulator will start the brightSin sin wave
    addModulator(brightSin).start();

    // This will add the SatParam as a parameter nob
    addModulator(radiusModulator).start();

    // This modulator will start the hueSaw saw wave
    addModulator(hueSin).start();

  }


  // This is the pattern loop, which will run continuously via LX
  @Override
  public void run(double deltaMs) {

      // Step through each cube in the model via a for loop
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
          brightness = 50;
        }

        // This line of code sets the color for each cube by passing in floats for the hue, saturation, and brightness
        colors[cube.index] = LX.hsb(hueSin.getValuef(), 100, brightness);
    }
  }
}

