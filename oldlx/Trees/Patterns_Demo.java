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
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import toxi.geom.Vec2D;
import org.apache.commons.lang3.ArrayUtils;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;



class Red extends TSPattern {
  // Variable declarations, parameters, and modulators go here

  // This will create a sin wave object that we will use to control brightness
  // Pass in the minimum sin value, maximum sin value, and the duration in milliseconds
  // final SinLFO brightSin = new SinLFO(minValue, maxValue, periodMs);

  // This will create a basic parameter object that we will use to control saturation
  // Pass in a label for the parameter, an initial value, a minimum value, and a maximum value
  // final BasicParameter satParam = new BasicParameter("Saturation", startValue, minValue, maxValue);

  // This will create a sin wave object that we will use to control brightness
  // Pass in the minimum sin value, maximum sin value, and the duration in milliseconds
  // final SawLFO hueSaw = new SawLFO(minValue, maxValue, periodMs);

  // Constructor
  Red(LX lx) {
    super(lx);

    // Add any needed modulators or parameters here

    // This modulator will start the brightSin sin wave
    // addModulator(brightSin).start();

    // This will add the SatParam as a parameter nob
    // addParameter(satParam);

    // This modulator will start the hueSaw saw wave
    // addModulator(hueSaw).start();

  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {

      // Step through each cube in the model via a for loop
      for (BaseCube cube : model.baseCubes) {
        // This line of code sets the color for each cube by passing in floats for the hue, saturation, and brightness
        colors[cube.index] = lx.hsb(0, 100, 100);

        // Final
        // colors[cube.index] = lx.hsb(hueSaw.getValuef(), satParam.getValuef(), brightSin.getValuef());
      }
  }
}
