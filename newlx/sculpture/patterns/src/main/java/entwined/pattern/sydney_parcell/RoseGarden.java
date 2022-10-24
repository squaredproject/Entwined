package entwined.pattern.sydney_parcell;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;
import entwined.utils.EntwinedUtils;

public class RoseGarden extends LXPattern {
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
  public RoseGarden(LX lx) {
    super(lx);

    // Add any needed modulators or parameters here

    // This modulator will start the brightSin sin wave
     addModulator(brightSin).start();

    // This will add the SatParam as a parameter nob
     //addParameter(satParam);

    // This modulator will start the hueSaw saw wave
     addModulator(hueSaw).start();

     idToHueMapping = new float[model.points.length];

    // create an initial mapping for the trees
    randomizeTreeBaseColor();


  }

  private void randomizeTreeBaseColor() {
    for (int i=0; i<model.points.length; i++ ) {
      idToHueMapping[i] = -1;
    }
    for (LXModel tree : model.sub("TREE")) {
      float randomBaseColor = EntwinedUtils.random(0, 360);
      float randomMinus = randomBaseColor - 20;
      if (randomMinus < 0) {
        randomMinus += 360;
      }
      float randomPlus = randomBaseColor + 20;
      if (randomPlus > 360) {
        randomPlus -= 360;
      }
      for (LXPoint cube : tree.points) {
         idToHueMapping[cube.index] = EntwinedUtils.random(randomMinus, randomPlus);
      }
    }
  }


  // This is the pattern loop, which will run continuously via LX
  @Override
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


      //     if (cube.pieceType == PieceType.TREE) {
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
    for (LXPoint cube : model.points) {
      //if (cube.pieceType == PieceType.SHRUB) {
        colors[cube.index] = LX.hsb(current, 100, 90);

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
    //  if (cube.pieceType == PieceType.TREE) {
    //    float theta = (idToHueMapping[cube.index] + time ) % 360;
    //    colors[cube.index] = lx.hsb(theta, 100, 90);
    //  }
    //}

    // int shrubIndex = -1;
    // cubeMax = 0;
    float startHue = 110;
    float startSat = 40;
    float startBright = 60;
    for (LXModel shrub : model.sub("SHRUB")) {
      boolean firstCube = true;
      for (LXPoint cube : shrub.points) {
        if (firstCube) {
          // cubeMax = cube.index + 30;
          startHue = 120;
          startSat = 40;
          startBright = 60;
          firstCube = false;
        } else {
          // make the index green
          colors[cube.index] = LX.hsb(startHue, 70, 40);
          startHue++;
          startSat += 2;
          startBright++;
        }
      }
    }
  }
}
