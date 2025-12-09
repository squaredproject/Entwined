package entwined.pattern.omar;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import entwined.core.TSBufferedPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

/*

There's a couple parameters that can tweak this pattern:

* snakeFadeInSin's period, as well as how long it lingers on the min/max values.
  This controls how long it takes to go from one snake to 6-8 snakes, then back to 1.
* snakeLength is how many cubes each snake takes up as it moves
* speedParam <----- this one actually has a GUI param exposed
  This controls how fast the snakes move
* snakeConfigs/numSnakesPerCircle
  These are defined at the bottom of the constructor. These define the individual snake's params
  such as the direction, color, etc
  And how many snakes each fairy circle has
*/

class SnakeConfig {
  float hue;
  int offset;
  int direction;
  boolean alternate;

  SnakeConfig(float hue_, int offset_, int direction_, boolean alternate_) {
    hue = hue_;
    offset = offset_;
    direction = direction_;
    alternate = alternate_;
  }
}

public class FairySnakes extends TSBufferedPattern {
  /* For every fairy circle, construct two "snake paths".
  A regular one and an alternate one.

  These paths are made by taking half of one small circle,
  followed by the opposite half of the next circle, and so on.
  The alternate path is the same but moves along all the halves
  that were not picked in the regular path.

  These paths are stored in a map indexed by the pieceId of the fairy circle.
 */
  HashMap<String, List<LXPoint>> snakePaths;
  HashMap<String, List<LXPoint>> snakePathsAlternate;
  int snakeLength = 10;
  double counter = 0;
  List<SnakeConfig> snakeConfigs;
  HashMap<String, Integer> numSnakesPerCircle;

  final CompoundParameter speedParam = new CompoundParameter("Speed", 3, 0.1, 20);
  final SinLFO snakeFadeInSin = new SinLFO(0.0, 0.9, 1000 * 30);

  public FairySnakes(LX lx) {
    super(lx);

    addParameter("speed", speedParam);
    addModulator(snakeFadeInSin).start();

    snakePaths = new HashMap<String, List<LXPoint>>();
    snakePathsAlternate = new HashMap<String, List<LXPoint>>();
    snakeConfigs = new ArrayList<SnakeConfig>();
    numSnakesPerCircle = new HashMap<String, Integer>();

    HashMap<String, List<LXPoint>> outerPaths = new HashMap<String, List<LXPoint>>();
    HashMap<String, List<LXPoint>> innerPaths = new HashMap<String, List<LXPoint>>();

    // These offsets are to make sure the halves we pick correctly align to
    // the outer/inner halves of the mini clusters.
    // These were obtained through trial & error.
    HashMap<String, Integer> RotationOffsets = new HashMap<String, Integer>();
    // XXX - this is fairly installation dependent - could we make a config file to generalize it?
    RotationOffsets.put("circle-1", 3);
    RotationOffsets.put("circle-2", 0);
    RotationOffsets.put("circle-3", -3);
    int lengthOfSnakePiece = 5;// How many cubes does a snake span inside a mini cluster
    for (LXModel fc : model.sub("FAIRY_CIRCLE")) {
      List<LXPoint> newInnerPath =  new ArrayList<LXPoint>();
      List<LXPoint> newOuterPath =  new ArrayList<LXPoint>();
      String pieceId = fc.meta("name");
      int rotationOffset = RotationOffsets.getOrDefault(pieceId, 0);
      int miniClusterIdx = 0;
      int currentFCCubeIdx = 0;
      for (LXPoint cube : fc.points) {

//      for (MiniCluster cluster : fc.miniClusters) {
        // In each mini cluster, grab roughly half of it as a snake piece

        // inner path
        for (int i = -lengthOfSnakePiece/2; i <= lengthOfSnakePiece/2; i++) {
          int size = 12; //cluster.cubes.size(); // all miniclusters are the same
          int index = Math.round(i + size) % size;
          int newIndex = wrapNegativeIndex(index - rotationOffset, size) % size;
          //newInnerPath.add(cluster.cubes.get(newIndex));
          newInnerPath.add(fc.points[miniClusterIdx*12 + newIndex]);

        }

        // outer path
        for (int i = lengthOfSnakePiece/2; i >= -lengthOfSnakePiece/2; i--) {
          int size = 12; // cluster.cubes.size(); // all miniclusters are the same
          int index = Math.round(i + size + 6) % size;
          int newIndex = wrapNegativeIndex(index - rotationOffset, size) % size;
          // newOuterPath.add(cluster.cubes.get(newIndex));
          newOuterPath.add(fc.points[miniClusterIdx*12 + newIndex]);
        }
        currentFCCubeIdx++;
        if (currentFCCubeIdx % 12 == 0) {
          miniClusterIdx++;
        }
      }

      innerPaths.put(pieceId, newInnerPath);
      outerPaths.put(pieceId, newOuterPath);
    }


    // Fill the final path with a sequence of alternating inner/outer paths
    for (Map.Entry<String, List<LXPoint>> entry : outerPaths.entrySet()) {
      List<LXPoint> finalPath = new ArrayList<LXPoint>();
      List<LXPoint> finalPathAlt = new ArrayList<LXPoint>();

      String pieceId = entry.getKey();

      List<LXPoint> outerPath = entry.getValue();
      List<LXPoint> innerPath = innerPaths.get(pieceId);

      for (int j = 0; j < outerPath.size(); j++) {
        // We switch from inner to outer when index is greater than lengthOfSnakePiece
        int index = j % (lengthOfSnakePiece * 2);
        if (index < lengthOfSnakePiece) {
          // The alternating path always takes the opposite half
          finalPath.add(outerPath.get(j));
          finalPathAlt.add(innerPath.get(j));
        } else {
          finalPath.add(innerPath.get(j));
          finalPathAlt.add(outerPath.get(j));
        }
      }


      snakePaths.put(pieceId, finalPath);
      snakePathsAlternate.put(pieceId, finalPathAlt);
    }

    // Create a config that tells us for each snake what is hue,
    // offset, direction etc is going to be. We then add these snakes
    // to each circle. We'll add more snakes to bigger circle
    // hue, offset, direction, alternate
    snakeConfigs.add(new SnakeConfig(0, 0, 1, false));
    snakeConfigs.add(new SnakeConfig(50, 20, 1, false));
    snakeConfigs.add(new SnakeConfig(100, 0, -1, true));
    snakeConfigs.add(new SnakeConfig(200, 20, -1, true));
    snakeConfigs.add(new SnakeConfig(300, 40, 1, false));
    snakeConfigs.add(new SnakeConfig(350, 70, 1, true));
    snakeConfigs.add(new SnakeConfig(0, 40, -1, true));
    snakeConfigs.add(new SnakeConfig(150, 60, -1, true));
    snakeConfigs.add(new SnakeConfig(170, 85, 1, true));
    snakeConfigs.add(new SnakeConfig(70, 100, 1, false));

    numSnakesPerCircle.put("circle-1", 6);
    numSnakesPerCircle.put("circle-2", 6);
    numSnakesPerCircle.put("circle-3", 10);//this is the big one
  }

  // `offset` is the index it starts at. Used to have multiple snakes going around
  // at different points.
  // `fadeFactor` is 0-1, used to make the snakes fade in
  // `direction` is either clockwise (1) or anticlockwise (-1)
  // `alternate` uses the other path that uses the opposite halves
  void makeSnake(String pieceId, float hue, int offset, float fadeFactor,
           int direction, boolean alternate) {

    List<LXPoint> path = snakePaths.get(pieceId);
    if (alternate == true) {
      path = snakePathsAlternate.get(pieceId);
    }
    // this is an error case. The function doesn't have any kind of error return though
    if (path == null) {
      System.out.println(" can't create snakes ");
      return;
    }

    for (int i = 0; i < snakeLength; i++) {
      int index = (int)(Math.round(i + counter + offset) % path.size());
      float factorNormalized = ((float)i / snakeLength);// from 1 to 0 along the snake

      if (direction == -1) {
        // Snake moving in reverse direction
        index =
          wrapNegativeIndex(
            (int)(Math.round(i - counter + offset) % path.size()),
            path.size()
          );
        factorNormalized = 1.0f - ((float)i / snakeLength);
      }

      float brightness = factorNormalized * 100 * fadeFactor;
      float saturation = factorNormalized * 100 * fadeFactor;

      LXPoint cube = path.get(index);
      int newColor = LX.hsb(hue, saturation, brightness);
      // Only set new color if it is greater than existing
      // This way snakes crossing keep their colors, but the
      // dim tail doesn't overwrite the bright head
      if (newColor > colors[cube.index]) {
        colors[cube.index] = newColor;
      }
    }
  }

  @Override
  public void bufferedRun(double deltaMs) {
    // This counter drives the snakes moving along their paths
    counter += deltaMs * 0.005 * speedParam.getValuef();

    // Reset all fairy circle cubes to black
    for (LXModel fc : model.sub("FAIRY_CIRCLE")) {
      for (LXPoint cube : fc.points) {
          colors[cube.index] = LX.hsb(0, 0, 0);
      }
    }

    // Example of making just two snakes, in one circle, run alternating paths
    // makeSnake("circle-2", 50, 0, 1, 1, false);
    // makeSnake("circle-2", 100, 60, 1, -1, true);

    // Apply the snakes for each circle. Fade in the number of snakes over time
    // they also fade out and repeat this cycle
    for (Map.Entry<String, Integer> entry : numSnakesPerCircle.entrySet()) {
      int num = entry.getValue();
      String pieceId = entry.getKey();
      float fadeFactor = snakeFadeInSin.getValuef() * num;
      if (fadeFactor <= 0) fadeFactor = 0;

      for (int i = 0; i < num; i++) {
        SnakeConfig config = snakeConfigs.get(i);
        float localFadeFactor = fadeFactor - i + 1;
        if (localFadeFactor <= 0) localFadeFactor = 0;
        if (localFadeFactor >= 1) localFadeFactor = 1;

        makeSnake(pieceId, config.hue, config.offset, localFadeFactor, config.direction, config.alternate);
      }
    }
  }

  int wrapNegativeIndex(int index, int arrayLen) {
    if (index >= 0) return index;

    int numTimes = (int)Math.ceil(Math.abs(index) / (double)arrayLen);
    return index + numTimes * arrayLen;
  }
}
