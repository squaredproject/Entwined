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
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import toxi.geom.Vec2D;
import org.apache.commons.lang3.ArrayUtils;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;



class OscillatingDarkRing extends TSPattern {
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
  OscillatingDarkRing(LX lx) {
    super(lx);

    // This modulator will start the brightSin sin wave
    addModulator(brightSin).start();

    // This will add the SatParam as a parameter nob
    addModulator(radiusModulator).start();

    // This modulator will start the hueSaw saw wave
    addModulator(hueSin).start();

  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {

      // Step through each cube in the model via a for loop
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
          brightness = 50;
        }
        
        // This line of code sets the color for each cube by passing in floats for the hue, saturation, and brightness
        colors[cube.index] = lx.hsb(hueSin.getValuef(), 100, brightness);
    }
  }
}


class RadialGradiant extends TSPattern {

    // Variable Declarations go here
    private float minz = Float.MAX_VALUE;
    private float maxz = -Float.MAX_VALUE;
    private float waveWidth = 10;
    private float speedMult = 1000;
    

    final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
    // final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
    final SinLFO wave360 = new SinLFO(0, 360, speedParam.getValuef() * speedMult);

    // final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
    final BasicParameter waveSlope = new BasicParameter("waveSlope", 0.04, 0.00001, 0.15);

    // Constructor and inital setup
    // Remember to use addParameter and addModulator if you're using Parameters or sin waves
    RadialGradiant(LX lx) {
        super(lx);
        addModulator(wave360).start();
        
        addParameter(waveSlope);
        addParameter(speedParam);
    }
    // This is the pattern loop, which will run continuously via LX
    public void run(double deltaMs) {
        if (getChannel().getFader().getNormalized() == 0) return;

        // wave360.setPeriod(speedParam.getValuef() * speedMult);
        wave360.setPeriod( speedParam.getValuef() * speedMult);
        
        // Use a for loop here to set the cube colors
        for (BaseCube cube : model.baseCubes) {
            // float v = (float)( (-wave360.getValuef() + waveSlope.getValuef()) + Math.sqrt(Math.pow(cube.sx,2)+Math.pow(cube.sz,2))*5 );
            // float v = (float)( (-wave360.getValuef() + 1 ) + Math.sqrt(Math.pow(cube.x,2)+Math.pow(cube.z,2))*5 );
            float v = (float)( (wave360.getValuef() + waveSlope.getValuef() *  Math.sqrt(Math.pow(cube.x,2)+Math.pow(cube.z,2))*5  ) );


            colors[cube.index] = lx.hsb( v % 360, 100,  100);
        }
    }
}
