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
  private float minz = Float.MAX_VALUE;
  private float maxz = -Float.MAX_VALUE;
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

    for (BaseCube cube : model.baseCubes) {
      if (cube.z < minz) {minz = cube.z;}
      if (cube.z > maxz) {maxz = cube.z;}
    }
  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    if (getChannel().getFader().getNormalized() == 0) return;

    wave.setPeriod(speedParam.getValuef() * speedMult);

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( (float)( (wave.getValuef() + waveSlope.getValuef() * Utils.map(cube.z, minz, maxz) ) % 360), 100, 100);
      }
  }
}

/**
RGB sprial from center tree
*/
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
      if (getChannel().getFader().getNormalized() == 0) return;

      for (BaseCube baseCube : model.baseCubes) {
        colors[baseCube.index] = lx.hsb(
        // Color is based on degrees from the center point, plus the spinner saw wave to rotate
        baseCube.globalTheta + spinner.getValuef()
        // plus the further from the center, the more hue is added, giving a swirl effect
        - baseCube.r * swirlMult.getValuef(),
        100.0f,
        100.0f);
      }
  }
}

/**
Breath in, breath out
*/
class Breath extends TSPattern {

  // Variable declarations, parameters, and modulators go here
  //final BasicParameter parameterName = new BasicParameter("parameterName", startValue, minValue, maxValue);
  float minValue = 0.f;
  float maxValue = 100.f;
  float period = 8000;
  final SinLFO breath = new SinLFO(minValue, maxValue, period);

  float hue = 180;
  boolean changeHue = false;

    int highestSoFar = -1;
    int treeyes = 0;
    int shrubtes = 0;

  // Constructor
  Breath(LX lx) {
    super(lx);
    // Add any needed modulators or parameters here
    addModulator(breath).start();
    //addParameter(parameterName);
  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {

    if (getChannel().getFader().getNormalized() == 0) return;

    if (changeHue == true && breath.getValuef() < 3) {
      hue = (float)Math.random() * 360;
      changeHue = false;
    }

      //breath.setPeriod(period - (Math.abs(breath.getValuef() - 50.0f) * 50));
      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( hue, 55, breath.getValuef());
      }

    if (breath.getValuef() > 90) {
      changeHue = true;
    }
  }
}

/**
Watch the change of seasons
*/
class Seasons extends TSPattern {
    boolean isSpring = true;
    boolean isFall = false;
    float changeSpeed = 100;
    float brightness = 0;
    float springHue = 120;
    double seasonLength = 0;

    // float[] leafColors = new float[model.baseCubes.size()];
    //
    // for (int i = 0; i > leafColors.size(); i++) {
    //   leafColors[i] = (float)Math.random(90, 150);
    // }

    Seasons(LX lx) {
      super(lx);
    }

    public void run(double deltaMs) {

      if (getChannel().getFader().getNormalized() == 0) return;

      seasonLength += deltaMs;

      if (isSpring) {
          brightness = Math.min( (brightness + (float)deltaMs / changeSpeed), 100);

          for (BaseCube cube : model.baseCubes) {
            colors[cube.index] = lx.hsb(120, 100, brightness);
          }
        } else if (isFall) {

        }
      }
}

/**
Star twinkle using Simplex Noise
*/
class SimplexSparkle extends TSPattern {
    SimplexNoise noize = new SimplexNoise();
    float xOff = 0;
    float zOff = 0;

    SimplexSparkle(LX lx) {
      super(lx);
    }


    public void run(double deltaMs) {
        if (getChannel().getFader().getNormalized() == 0) return;

        xOff += (deltaMs / 5000);
        zOff +=(deltaMs / 5000);
        // Use a for loop here to set the cube colors
        for (BaseCube cube : model.baseCubes) {
          colors[cube.index] = lx.hsb(getHue(cube.x, cube.z, xOff, zOff), 100, getBrightness(cube.x, cube.z, xOff, zOff));
        }
    }

    private float getHue(float _x, float _z, float _xOff, float _zOff) {
        int toReturnHue;

        toReturnHue = (int)Math.abs(noize.noise(_x + _xOff, _z + _zOff) * 360);

        return toReturnHue;
    }

    private float getBrightness(float _x, float _z, float _xOff, float _zOff) {
        int toReturn;

        toReturn = (int)Math.abs(noize.noise(_x + _xOff, _z + _zOff) * 50) + 75;

        return toReturn;
    }

}




// Holiday Patterns

/**
A simple holiday pattern
*/
class Wreathes extends TSPattern {
  SimplexNoise noize = new SimplexNoise();

  Wreathes(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
        if (getChannel().getFader().getNormalized() == 0) return;

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        if (noize.noise(cube.x, cube.y, cube.z) < .5) {
          colors[cube.index] = lx.hsb(120, 100, 100);
        } else {
          colors[cube.index] = lx.hsb(0, 100, 100);
        }
      }
  }
}


class ChristmasTree extends TSPattern {
  SimplexNoise noize = new SimplexNoise();
  double cubeNoise = 0;

  ChristmasTree(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        cubeNoise = noize.noise(cube.x, cube.y, cube.z);

        if (cubeNoise > .80) {
          colors[cube.index] = lx.hsb(240, 100, 100);
        } else if (cubeNoise > .65) {
          colors[cube.index] = lx.hsb(30, 100, 100);
        } else if (cubeNoise > .45) {
          colors[cube.index] = lx.hsb(0, 100, 100);
        } else {
          colors[cube.index] = lx.hsb((float) (cubeNoise * 20 + 120), 100, 90);
        }
      }

      // It's OK this is only cubes, because it's just the tree cubes
      for (Cube cube : model.cubes) {
        if (cube.config.layerIndex == 2) {
            colors[cube.index] = lx.hsb(60, 100, 100);
        }
      }
  }
}


class Peppermint extends TSPattern {

  // Variable Declarations go here
  private float treex;
  private float treez;

  private Tree theTree;

  final BasicParameter speed = new BasicParameter("Speed", 5000, 20000, 1000);
  final BasicParameter swirlMult = new BasicParameter("Swirl", .5, 2, .1);
  final SawLFO spinner = new SawLFO(0, 360, speed);

  float saturation = 0;

  // Constructor
  Peppermint(LX lx) {
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
    if (getChannel().getFader().getNormalized() == 0) return;
    
      for (BaseCube baseCube : model.baseCubes) {

        if (((baseCube.theta + spinner.getValuef()
        // plus the further from the center, the more hue is added, giving a swirl effect
        - baseCube.r / 2// * swirlMult.getValuef()
        ) % 120) > 60) {
          saturation = 0;
        } else {
          saturation = 100;
        }

        colors[baseCube.index] = lx.hsb(0.0f, saturation, 100.0f);
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
