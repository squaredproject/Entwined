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

class Circles extends TSPattern {

  // Variable Declarations go here
  private float minz = Float.MAX_VALUE;
  private float maxz = -Float.MAX_VALUE;
  private float waveWidth = 1;
  private float speedMult = 1000;

  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SawLFO wave100 = new SawLFO(0, 100, speedParam.getValuef() * speedMult);

  // add speed, wave width

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or sin waves
  Circles(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);

    for (BaseCube cube : model.baseCubes) {
      if (cube.z < minz) {minz = cube.z;}
      if (cube.z > maxz) {maxz = cube.z;}
    }
  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
	float v = (float)( (-wave360.getValuef() + waveSlope.getValuef()) + Math.sqrt(Math.pow(cube.sx,2)+Math.pow(cube.sz,2))*5 );
        colors[cube.index] = lx.hsb( v % 360, 100,  100);
        //colors[cube.index] = lx.hsb( (float)( Math.sqrt(Math.pow(cube.sx,2)+Math.pow(cube.sz,2)) % 360), 100, 100);
      }
  }
}

class LineScan extends TSPattern {
  // Variable Declarations go here
  private float waveWidth = 1;
  private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms =0.0;
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final BasicParameter theta = new BasicParameter("theta", 45, 0, 360);
  final BasicParameter hue = new BasicParameter("hue", 45, 0, 360);
  final BasicParameter wave_width = new BasicParameter("waveWidth", 500, 10, 1500);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

    LineScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);
    addParameter(theta);
    addParameter(hue);
    addParameter(wave_width);


  }
  private float dist(float  x, float z) {
	return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);

      float field_len=7000;
      total_ms+=deltaMs;
      // Use a for loop here to set the ube colors
      for (BaseCube cube : model.baseCubes) {
	double d = Math.abs(dist(cube.x,cube.z)-speedParam.getValuef()*(total_ms%field_len-field_len/2)/10);
        if (d<wave_width.getValuef()) {
        	//float d = (float)(50.0*(Math.sin(dist(cube.x,cube.z)/(wave_width.getValuef()) + speedParam.getValuef()*total_ms/1000.0)+1.0));
        	colors[cube.index] = lx.hsb( hue.getValuef()  , 100, (float)((1.0-d/wave_width.getValuef())*100.0 ));
	} else {
        	colors[cube.index] = lx.hsb( hue.getValuef()  , 100, 0);
	}
      }
  }
}

class WaveScan extends TSPattern {
  // Variable Declarations go here
  private float waveWidth = 1;
  private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms =0.0;
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final BasicParameter theta = new BasicParameter("theta", 45, 0, 360);
  final BasicParameter hue = new BasicParameter("hue", 45, 0, 360);
  final BasicParameter wave_width = new BasicParameter("waveWidth", 20, 1, 50);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

    WaveScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);
    addParameter(theta);
    addParameter(hue);
    addParameter(wave_width);


  }
  private float dist(float  x, float z) {
	return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);
      total_ms+=deltaMs;
      // Use a for loop here to set the ube colors
      for (BaseCube cube : model.baseCubes) {
        float d = (float)(50.0*(Math.sin(dist(cube.x,cube.z)/(wave_width.getValuef()) + speedParam.getValuef()*total_ms/1000.0)+1.0));
        colors[cube.index] = lx.hsb( hue.getValuef()  , 100, d);
      }
  }
}

class RainbowWaveScan extends TSPattern {
  // Variable Declarations go here
  private float waveWidth = 1;
  private float speedMult = 1000;

  private float nx = 0;
  private float nz = 0;
  private float n = 0;
  private double total_ms =0.0;
  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final BasicParameter theta = new BasicParameter("theta", 45, 0, 360);
  final BasicParameter hue = new BasicParameter("hue", 45, 0, 360);
  final BasicParameter wave_width = new BasicParameter("waveWidth", 20, 1, 50);
  final SawLFO wave360 = new SawLFO(0, 360, speedParam.getValuef() * speedMult);
  final SinLFO wave100 = new SinLFO(0, 100, speedParam.getValuef() * speedMult);

    RainbowWaveScan(LX lx) {
    super(lx);
    addModulator(wave360).start();
    addModulator(wave100).start();
    addParameter(waveSlope);
    addParameter(speedParam);
    addParameter(theta);
    addParameter(hue);
    addParameter(wave_width);


  }
  private float dist(float  x, float z) {
	return (nx*x+nz*z)/n;
  }
  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    float theta_rad = (float)Math.toRadians((int)theta.getValuef());
    nx = (float)Math.sin(theta_rad);
    nz = (float)Math.cos(theta_rad);
    n = (float)Math.sqrt(Math.pow(nx,2)+Math.pow(nz,2));
    wave360.setPeriod(speedParam.getValuef() * speedMult);
    wave100.setPeriod(speedParam.getValuef() * speedMult);
      total_ms+=deltaMs;
      // Use a for loop here to set the ube colors
      for (BaseCube cube : model.baseCubes) {
        float d = (float)(50.0*(Math.sin(dist(cube.x,cube.z)/(wave_width.getValuef()) + speedParam.getValuef()*total_ms/1000.0)+1.0));
        colors[cube.index] = lx.hsb( wave360.getValuef()  , 100, d);
      }
  }
}




/**
* A template pattern to get ya started.
*/
/*
class PatternTemplate extends TSPattern {

  // Variable declarations, parameters, and modulators go here
  float minValue;
  float maxValue;
  float startValue;
  float period;
  final BasicParameter parameterName = new BasicParameter("parameterName", startValue, minValue, maxValue);
  final SawLFO modulatorName = new SawLFO(minValue, maxValue, period);

  // Constructor
  PatternTemplate(LX lx) {
    super(lx);

    // Add any needed modulators or parameters here
    addModulator(modulatorName).start();
    addParameter(parameterName);

  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( , , );
      }
  }
}
*/
