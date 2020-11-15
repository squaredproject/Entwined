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

/**
An RGB wave that covers the whole field.
*/
class ColorWave extends TSPattern {

  // Variable Declarations go here
  private float minx = Float.MAX_VALUE;
  private float maxx = -Float.MAX_VALUE;
  private float waveWidth = 1;
  private float speedMult = 1000;

  final BasicParameter speedParam = new BasicParameter("Speed", 5, 20, .01);
  final BasicParameter waveSlope = new BasicParameter("waveSlope", 360, 1, 720);
  final SawLFO wave = new SawLFO(0, 360, speedParam.getValuef() * speedMult);

  // add speed, wave width

  // Constructor and inital setup
  // Remember to use addParameter and addModulator if you're using Parameters or sin waves
  ColorWave(LX lx) {
    super(lx);
    addModulator(wave).start();
    addParameter(waveSlope);
    addParameter(speedParam);

    for (Cube cube : model.cubes) {
      if (cube.x < minx) {minx = cube.x;}
      if (cube.x > maxx) {maxx = cube.x;}
    }
  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    wave.setPeriod(speedParam.getValuef() * speedMult);

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( (float)( (wave.getValuef() + waveSlope.getValuef() * Utils.map(cube.x, minx, maxx) ) % 360), 100, 100);
      }
  }
}

class BeachBall extends TSPattern {

  // Variable Declarations go here
  private float treex;
  private float treez;

  private Tree theTree;

  final BasicParameter speed = new BasicParameter("Speed", 5000, 20000, 1000);
  final BasicParameter swirlMult = new BasicParameter("Swirl", .5, 2, .1);
  final SawLFO spinner = new SawLFO(0, 360, speed);

  // Constructor
  BeachBall(LX lx) {
    super(lx);

    addModulator(spinner).start();
    addParameter(speed);
    addParameter(swirlMult);

    theTree = model.trees.get(0);
    treex = theTree.x;
    treez = theTree.z;

  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {

      // // Use a for loop here to set the cube colors
      // for (BaseCube cube : model.baseCubes) {
      //   colors[cube.index] = lx.hsb(
      //   // Color is based on degrees from the center point, plus the spinner saw wave to rotate
      //   (float) Math.toDegrees(Math.atan2((double)(treez - cube.z), (double)(treex - cube.x))) + spinner.getValuef()
      //   // plus the further from the center, the more hue is added, giving a swirl effect
      //   - (float)(Math.hypot(treez - cube.z, treex - cube.x) * swirlMult.getValuef()),
      //   100.0f,
      //   100.0f);
      // }
      //

      for (BaseCube baseCube : model.baseCubes) {
        colors[baseCube.index] = lx.hsb(
        // Color is based on degrees from the center point, plus the spinner saw wave to rotate
        (float) Math.toDegrees(Math.atan2((double)(treez - baseCube.z), (double)(treex - baseCube.x))) + spinner.getValuef()
        // plus the further from the center, the more hue is added, giving a swirl effect
        - (float)(Math.hypot(treez - baseCube.z, treex - baseCube.x) * swirlMult.getValuef()),
        100.0f,
        100.0f);
      }
  }
}


class Breath extends TSPattern {

  // Variable declarations, parameters, and modulators go here
  //final BasicParameter parameterName = new BasicParameter("parameterName", startValue, minValue, maxValue);
  float minValue = 0.f;
  float maxValue = 100.f;
  float period = 10000;
  final SinLFO breath = new SinLFO(minValue, maxValue, period);

  // Constructor
  Breath(LX lx) {
    super(lx);

    // Add any needed modulators or parameters here
    addModulator(breath).start();
    //addParameter(parameterName);

  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
      breath.setPeriod(period - (Math.abs(breath.getValuef() - 50.0f) * 50));

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( 180, 25, breath.getValuef());
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
