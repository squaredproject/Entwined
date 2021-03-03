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
import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import toxi.math.noise.PerlinNoise;
import toxi.math.noise.SimplexNoise;

import java.util.Map;

class RoseGarden extends TSPattern {
  // Variable declarations, parameters, and modulators go here
  float h = 256;
  boolean iterateForward = true;
  float time = 0;
  float[] idToHueMapping;
  
  // This will create a sin wave object that we will use to control brightness
  // Pass in the minimum sin value, maximum sin value, and the duration in milliseconds
   final SinLFO brightSin = new SinLFO(20, 60, 5000);

  // This will create a basic parameter object that we will use to control saturation
  // Pass in a label for the parameter, an initial value, a minimum value, and a maximum value
   //final BasicParameter satParam = new BasicParameter("Saturation", 60, 50, 100);

  // This will create a sin wave object that we will use to control brightness
  // Pass in the minimum sin value, maximum sin value, and the duration in milliseconds
   final SawLFO hueSaw = new SawLFO(0, 180, 10000);

  // Constructor
  RoseGarden(LX lx) {
    super(lx);

    // Add any needed modulators or parameters here

    // This modulator will start the brightSin sin wave
     addModulator(brightSin).start();

    // This will add the SatParam as a parameter nob
     //addParameter(satParam);

    // This modulator will start the hueSaw saw wave
     addModulator(hueSaw).start();
     
     idToHueMapping = new float[model.baseCubes.size()];

    // create an initial mapping for the trees
    randomizeTreeBaseColor();
    
    
  }
  
  private void randomizeTreeBaseColor() {
    for (int treeId = 0; treeId < 22; treeId++) {
      float randomBaseColor = Utils.random(0, 360);
      float randomMinus = randomBaseColor - 20;
      if (randomMinus < 0) {
        randomMinus += 360;
      }
      float randomPlus = randomBaseColor + 20;
      if (randomPlus > 360) {
        randomPlus -= 360;
      }
      
      for (BaseCube cube : model.baseCubes) {
        idToHueMapping[cube.index] = -1;
        if (cube.treeOrShrub == TreeOrShrub.TREE && cube.sculptureIndex == treeId) {
          // save the id to the map 
          idToHueMapping[cube.index] = Utils.random(randomMinus, randomPlus);
        }
      }
    }
  }


  // This is the pattern loop, which will run continuously via LX
  public void run(double deltaMs) {
    time += deltaMs; 
    
    // [0-2] = tree index
    // 0 - 19 shrub index
    // TREE or SHRUB
        
       //Step through each cube in the model via a for loop
      //for (BaseCube cube : model.baseCubes) {
      //  if (Utils.random(0, 1) > 0.5) {
      //    float c = idToHueMapping[cube.index];
      //    float flip = Utils.random(0, 1);
      //    if (flip < 0.5) {
      //      c--;
      //    }
      //    else {
      //      c++;
      //    }
          
          
      //     if (cube.treeOrShrub == TreeOrShrub.TREE) {
      //       colors[cube.index] = lx.hsb(c, 100, 80);
      //     }
           
      //     idToHueMapping[cube.index] = c;
      //  }
        
      //}
      
    if (time % 500 == 0) {
       // reset the tree leaves to a random next color 
       randomizeTreeBaseColor();
    }
    
    
    if (iterateForward) {
      h += 1;
      if (h > 360) {
        h = 0;
      } 
      else if (h == 46) {
        iterateForward = !iterateForward;
      }
    }
    else {
      h -= 1;
      if (h < 0) {
        h = 360;
      }
      else if (h == 255) {
        iterateForward = !iterateForward;
      }
    }
    
    float current = h;
    boolean currentIterator = iterateForward;
    for (BaseCube cube : model.baseCubes) {
      //if (cube.treeOrShrub == TreeOrShrub.SHRUB) {
        colors[cube.index] = lx.hsb(current, 100, 90);
        
        if (currentIterator) {
          current += 1;
          if (current > 360) {
            current = 0;
          } 
          else if (current == 46) {
            currentIterator = !currentIterator;
          }
        }
        else {
          current -= 1;
          if (current < 0) {
            current = 360;
          }
          else if (current == 255) {
            currentIterator = !currentIterator;
          }
        }
      //}
    }
    
    //for (BaseCube cube : model.baseCubes) {
    //  if (cube.treeOrShrub == TreeOrShrub.TREE) {
    //    float theta = (idToHueMapping[cube.index] + time ) % 360;    
    //    colors[cube.index] = lx.hsb(theta, 100, 90);
    //  }
    //}
    
    int shrubIndex = -1;
    int cubeMax = 0;
    float startHue = 110;
    float startSat = 40;
    float startBright = 60;
    for (BaseCube cube : model.baseCubes) {
      int newIndex = cube.sculptureIndex;
      if (cube.treeOrShrub == TreeOrShrub.SHRUB) {
        if (shrubIndex != newIndex) {
          // new shrub
          shrubIndex = newIndex;
          cubeMax = cube.index + 30;
          startHue = 120;
          startSat = 40;
          startBright = 60;
        }
        
        if (cube.index <= cubeMax) {
          // make the index green 
          colors[cube.index] = lx.hsb(startHue, 70, 40);
          startHue++;
          startSat += 2;
          startBright++;
        }
      }
    }
  }
}
