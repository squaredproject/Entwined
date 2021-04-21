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
Blips go up
*/
class BleepBloop extends TSPattern {
  private List<Blip> blips = new ArrayList<Blip>();

  SimplexNoise noize = new SimplexNoise();
  float xOff = 0;
  float zOff = 0;
  float a = 0;
  float hue1 = (float)Math.random() * 360.0f;
  float hue2 = (hue1 + 100.0f) % 360.0f;

  BleepBloop(LX lx) {
    super(lx);

    // for every shrub, add a Blip
    for (Shrub shrub : model.shrubs) {
      blips.add(new Blip(false, shrub.index));
    }
    //System.out.println("BleepBLoop: hue 1 " +  hue1 + " hue 2 " + hue2);
  }



  public void run(double deltaMs) {

    a += .01;
    hue1 = (hue1 + .05f) % 360.0f;
    hue2 = (hue2 + .05f) % 360.0f;

    xOff = (float)Math.cos(a);
    zOff = (float)Math.sin(a);
      for (ShrubCube cube : model.shrubCubes) {
        colors[cube.index] = lx.hsb(blips.get(cube.sculptureIndex).getHue(), blips.get(cube.sculptureIndex).getSat(cube.x, cube.y, cube.z, a), blips.get(cube.sculptureIndex).getBrightness(cube.x, cube.y, cube.z, a));
      }

      for (Blip blip : blips) {
        blip.update(deltaMs);
      }

      if (a > Utils.TWO_PI) {
        a = 0;
      }
  }


  private class Blip {
    float height;
    float hue;
    //static final float baseHue;
    float speed;
    float tailLen;
    boolean isOn;
    boolean isTree;
    int sculptureIdx;

    Blip(boolean TorS, int _idx) {
      height = 0;
      if (Math.random() < .70) {
        hue = hue1;
      } else {
        hue = hue2;
      }
      speed = 1 + (9 * (float)Math.random());
      tailLen = 100;
      isOn = false;
      isTree = TorS;
      sculptureIdx = _idx;

      if (Math.random() < .01) {
        isOn = true;
      }
    }

    private float getHue() {
      if (!isOn) {return 0;}

      return hue;
    }

    private float getSat(float x, float y, float z, float _a) {
      if (!isOn) {return 0;}

      if (height - y < 0 || height - y > tailLen) {return 100;}

      // set saturation
      float toReturn = (100/tailLen) * (height-y) + 15;

      // add simplex sparkle
      toReturn = toReturn + ((float)noize.noise(x + xOff, z + zOff) * 50.0f);

      // constrin and return
      toReturn = Math.max(Math.min(toReturn, 100), 0);
      return toReturn;
    }

    private float getBrightness(float x, float y, float z, float _a) {
      if (!isOn) {return 0;}

      // get rid of edge cases first, Dist > tail or < 0, THEN do the math
      if (height - y < 0 || height - y > tailLen) {return 0;}

      // get brightness
      float toReturn = -(100/tailLen) * (height-y) + 100;

      // add simplex sparkle
      toReturn = toReturn + Math.abs(((float)noize.noise(x + xOff, z + zOff) * 30.0f));

      // constrin and return
      toReturn = Math.max(Math.min(toReturn, 100), 0);
      return toReturn;


    }

    private void update(double _deltaMs) {
      if (isOn) { // on
        height += (((float)_deltaMs)/50) * speed;

        if (height >= tailLen * 2) {
          isOn = false;
          height = 0;
          speed = 1 + (9 * (float)Math.random());
          if (Math.random() < .70) {
            hue = hue1;
          } else {
            hue = hue2;
          }
        }
      } else { // off
        // stagger the turn Ons here
        if (Math.random() < .01) {
          isOn = true;
        }
      }
    }
  }
}

/**
Blips go up
*/
class Bloop extends TSPattern {
  private List<Blip> blips = new ArrayList<Blip>();

  SimplexNoise noize = new SimplexNoise();
  float xOff = 0;
  float zOff = 0;
  float a = 0;
  float hue1 = (float)Math.random() * 360.0f;
  float hue2 = (hue1 + 100.0f) % 360.0f;

  Bloop(LX lx) {
    super(lx);

    // for every shrub, add a Blip
    for (Shrub shrub : model.shrubs) {
      blips.add(new Blip(false, shrub.index));
    }
    // System.out.println("Bloop: hue 1 " +  hue1 + " hue 2 " + hue2);
  }



  public void run(double deltaMs) {

    a += .01;
    hue1 = (hue1 + .05f) % 360.0f;
    hue2 = (hue2 + .05f) % 360.0f;

    xOff = (float)Math.cos(a);
    zOff = (float)Math.sin(a);
      for (ShrubCube cube : model.shrubCubes) {
        colors[cube.index] = lx.hsb(blips.get(cube.sculptureIndex).getHue(), blips.get(cube.sculptureIndex).getSat(cube.x, cube.y, cube.z, a), blips.get(cube.sculptureIndex).getBrightness(cube.x, cube.y, cube.z, a));
      }

      for (Blip blip : blips) {
        blip.update(deltaMs);
      }

      if (a > Utils.TWO_PI) {
        a = 0;
      }
  }


  private class Blip {
    float height;
    float hue;
    //static final float baseHue;
    float speed;
    float tailLen;
    boolean isOn;
    boolean isTree;
    int sculptureIdx;

    Blip(boolean TorS, int _idx) {
      height = 0;
      if (Math.random() < .70) {
        hue = hue1;
      } else {
        hue = hue2;
      }
      speed = 1 + (9 * (float)Math.random());
      tailLen = 100;
      isOn = false;
      isTree = TorS;
      sculptureIdx = _idx;

      if (Math.random() < .01) {
        isOn = true;
      }
    }

    private float getHue() {
      if (!isOn) {return 0;}

      return hue;
    }

    private float getSat(float x, float y, float z, float _a) {
      if (!isOn) {return 0;}

      if (height - y < 0 || height - y > tailLen) {return 100;}

      // set saturation
      float toReturn = (100/tailLen) * (height-y) + 15;

      // add simplex sparkle
      toReturn = toReturn + ((float)noize.noise(x + xOff, z + zOff) * 50.0f);

      // constrin and return
      toReturn = Math.max(Math.min(toReturn, 100), 0);
      return toReturn;
    }

    private float getBrightness(float x, float y, float z, float _a) {
      if (!isOn) {return 0;}

      // get rid of edge cases first, Dist > tail or < 0, THEN do the math
      if (height - y < 0 || height - y > tailLen) {return 0;}

      // get brightness
      float toReturn = -(100/tailLen) * (height-y) + 100;

      // add simplex sparkle
      toReturn = toReturn + Math.abs(((float)noize.noise(x + xOff, z + zOff) * 30.0f));

      // constrin and return
      toReturn = Math.max(Math.min(toReturn, 100), 0);
      return toReturn;


    }

    private void update(double _deltaMs) {
      if (isOn) { // on
        height += (((float)_deltaMs)/50) * speed;

        if (height >= tailLen * 2) {
          isOn = false;
          height = 0;
          speed = 1 + (9 * (float)Math.random());
          if (Math.random() < .850) {
            hue = hue1;
          } else {
            hue = hue2;
          }
        }
      } else {
          isOn = true;
      }
    }
  }
}

/**
Blips go up
*/
class UpNDown extends TSPattern {
  private List<Blip> blips = new ArrayList<Blip>();

  SimplexNoise noize = new SimplexNoise();
  float xOff = 0;
  float zOff = 0;
  float a = 0;
  float hue1 = (float)Math.random() * 360.0f;
  float hue2 = (hue1 + 100.0f) % 360.0f;

  UpNDown(LX lx) {
    super(lx);

    // for every shrub, add a Blip
    for (Shrub shrub : model.shrubs) {
      blips.add(new Blip(false, shrub.index));
    }
    // System.out.println("UpNDown: hue 1 " +  hue1 + " hue 2 " + hue2);
  }



  public void run(double deltaMs) {

    a += .01;
    hue1 = (hue1 + .05f) % 360.0f;
    hue2 = (hue2 + .05f) % 360.0f;

    xOff = (float)Math.cos(a);
    zOff = (float)Math.sin(a);
      for (ShrubCube cube : model.shrubCubes) {
        colors[cube.index] = lx.hsb(blips.get(cube.sculptureIndex).getHue(), blips.get(cube.sculptureIndex).getSat(cube.x, cube.y, cube.z, a), blips.get(cube.sculptureIndex).getBrightness(cube.x, cube.y, cube.z, a));
      }

      for (Blip blip : blips) {
        blip.update(deltaMs);
      }

      if (a > Utils.TWO_PI) {
        a = 0;
      }
  }


  private class Blip {
    float height;
    float hue;
    //static final float baseHue;
    float speed;
    float tailLen;
    boolean isOn;
    boolean isTree;
    int sculptureIdx;

    Blip(boolean TorS, int _idx) {
      height = 0;
      if (Math.random() < .70) {
        hue = hue1;
      } else {
        hue = hue2;
      }
      speed = 1 + (9 * (float)Math.random());
      tailLen = 100;
      isOn = false;
      isTree = TorS;
      sculptureIdx = _idx;

      if (Math.random() < .01) {
        isOn = true;
      }
    }

    private float getHue() {
      if (!isOn) {return 0;}

      return hue;
    }

    private float getSat(float x, float y, float z, float _a) {
      if (!isOn) {return 0;}

      if (height - y < 0 || height - y > tailLen) {return 100;}

      // set saturation
      float toReturn = (100/tailLen) * (height-y) + 15;

      // add simplex sparkle
      toReturn = toReturn + ((float)noize.noise(x + xOff, z + zOff) * 50.0f);

      // constrin and return
      toReturn = Math.max(Math.min(toReturn, 100), 0);
      return toReturn;
    }

    private float getBrightness(float x, float y, float z, float _a) {
      if (!isOn) {return 0;}

      // get rid of edge cases first, Dist > tail or < 0, THEN do the math
      if (height - y < 0 || height - y > tailLen) {return 0;}

      // get brightness
      float toReturn = -(100/tailLen) * (height-y) + 100;

      // add simplex sparkle
      toReturn = toReturn + Math.abs(((float)noize.noise(x + xOff, z + zOff) * 30.0f));

      // constrin and return
      toReturn = Math.max(Math.min(toReturn, 100), 0);
      return toReturn;


    }

    private void update(double _deltaMs) {
      if (isOn) { // on
        height += (((float)_deltaMs)/50) * speed;

        if (height >= tailLen * 2) {
          isOn = false;
          height = 0;
          speed = 1 + (9 * (float)Math.random());
          if (Math.random() < .850) {
            hue = hue1;
          } else {
            hue = hue2;
          }
        }
      } else {
          isOn = true;
      }
    }
  }
}



/**
Star twinkle using Simplex Noise
*/
class SimplexSparkle extends TSPattern {
  SimplexNoise noize = new SimplexNoise();
  float xHueOff = 0;
  float zHueOff = 0;
  float xSatOff = 0;
  float zSatOff = 0;
  float xBrightOff = 0;
  float zBrightOff = 0;
  float aH = 0;
  float aS = 0;
  float aB = 0;

  float hue = 230;

  SimplexSparkle(LX lx) {
    super(lx);

  }

  public void run(double deltaMs) {
      aH += .01;
      aB += .003;
      aS += .0065;
      hue += .1;
      // xOff = (float)Math.cos(a);
      // zOff = (float)Math.sin(a);
      xHueOff = (float)Math.cos(aH);
      zHueOff = (float)Math.sin(aH);
      xSatOff = (float)Math.cos(aS);
      zSatOff = (float)Math.sin(aS);
      xBrightOff = (float)Math.cos(aB);
      zBrightOff = (float)Math.sin(aB);


      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb(getHue(cube.x, cube.z), getSat(cube.x, cube.z), getBright(cube.x, cube.z));
      }

      if (hue >= 360) {
        hue = 0;
      }
      if (aS > Utils.TWO_PI) {
        aS = 0;
      }
      if (aB > Utils.TWO_PI) {
        aB = 0;
      }
  }

  public float getHue(float cx, float cz) {
    // Calculate
    float toReturn = hue + (float)noize.noise(cx + xHueOff, cz + zHueOff) * 45;

    // Constrain
    toReturn = hue % 360;

    // return
    return toReturn;

  }

  public float getSat(float cx, float cz) {

    // Calculate
    float toReturn = 50 +  (float)noize.noise(cx + xSatOff, cz + zSatOff) * 100;

    // Constrain
    toReturn = Math.min(Math.max(toReturn, 0), 100);

    // return
    return toReturn;
  }

  public float getBright(float cx, float cz) {

    // Calculate
    float toReturn = 50 +  (float)noize.noise(cx + xBrightOff, cz + zBrightOff) * 100;

    // Constrain
    toReturn = Math.min(Math.max(toReturn, 0), 100);

    // return
    return toReturn;
  }

}



class PartyRings extends TSPattern {

  private List<RingStack> ringStacks = new ArrayList<RingStack>();

  PartyRings(LX lx) {
    super(lx);
    for (Shrub shrub : model.shrubs) {
      ringStacks.add(new RingStack());
    }

  }


  public void run(double deltaMs) {

    for (ShrubCube cube : model.shrubCubes) {
      colors[cube.index] = lx.hsb(ringStacks.get(cube.sculptureIndex).getHue(cube.config.rodIndex), 100, ringStacks.get(cube.sculptureIndex).getBright(cube.theta, cube.config.rodIndex));
    }


  }

  private class RingStack {

    float origin[] = new float[5];
    float head[] = new float[5];
    float hues[] = new float[5];

    RingStack () {
      for (int i = 0; i < 5; i++) {
        origin[i] = (float)Math.random() * 360;
        head[i] = origin[i];
        hues[i] = (float)Math.random() * 30 + 120;
      }
    }

    private float getHue(int rodPosition) {
      return hues[rodPosition - 1];
    }

    private float getSat(int rodPosition) {
      return 100;
    }

    private float getBright(float theta, int rodPosition) {
      return ((float)Math.abs((theta - head[rodPosition - 1]) / 180.0f)) * 100.0f;
    }
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


class CottonCandy extends TSPattern {

  // Variable Declarations go here
  private float treex;
  private float treez;

  private Tree theTree;

  final BasicParameter speed = new BasicParameter("Speed", 2500, 20000, 1000);
  final BasicParameter swirlMult = new BasicParameter("Swirl", .5, 2, .1);
  final SawLFO spinner = new SawLFO(0, 360, speed);

  float hue = 0;

  // Constructor
  CottonCandy(LX lx) {
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
          hue = 330;
        } else {
          hue = 180;
        }

        colors[baseCube.index] = lx.hsb(hue, 100.0f, 100.0f);
      }
  }
}



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
          colors[cube.index] = lx.hsb(cube.theta, 100, 100);
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


/* Working example of simplex noise for me because I am dumb
class Simplex extends TSPattern {
  SimplexNoise noize = new SimplexNoise();
  float xOff = 0;
  float zOff = 0;
  float a = 0;

  Simplex(LX lx) {
    super(lx);

  }

  public void run(double deltaMs) {
      a += .001;
      xOff = (float)Math.cos(a);
      zOff = (float)Math.sin(a);

      // Use a for loop here to set the cube colors
      for (BaseCube cube : model.baseCubes) {
        colors[cube.index] = lx.hsb( Utils.map((float)noize.noise(cube.x + xOff, cube.z + zOff), -1, 1, 0, 360), 100, 100);
      }

      if (a > Utils.TWO_PI) {
        a = 0;
      }
  }

}
*/
