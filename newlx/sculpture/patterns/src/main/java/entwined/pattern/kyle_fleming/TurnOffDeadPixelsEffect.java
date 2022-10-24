package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;

public class TurnOffDeadPixelsEffect extends LXEffect {
  int[] deadPixelIndices = new int[] { };
  int[] deadPixelClusters = new int[] { };

  public TurnOffDeadPixelsEffect(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs, double amount) {
    for (int i = 0; i < deadPixelIndices.length; i++) {
      //Cluster cluster = model.clusters.get(deadPixelClusters[i]);
      //Cube cube = cluster.cubes.get(deadPixelIndices[i]);
      //colors[cube.index] = LXColor.BLACK;
    }
  }
}
