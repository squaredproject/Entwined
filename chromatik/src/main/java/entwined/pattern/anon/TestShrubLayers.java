package entwined.pattern.anon;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class TestShrubLayers extends LXPattern {

  final BoundedParameter rodLayer;
  final BoundedParameter clusterIndex;
  final BoundedParameter shrubIndex;

  public TestShrubLayers(LX lx) {
    super(lx);
    // lowest level means turning that param off
    addParameter("rodLayer", rodLayer = new BoundedParameter("layer", 0, 0, 5));
    addParameter("clusterIndex", clusterIndex = new BoundedParameter("clusterIndex", -1, -1, 11));
    addParameter("shrubIndex", shrubIndex = new BoundedParameter("shrubIndex", -1, -1, 19));
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    int shrubIdx = 0;
    for (LXModel shrub : model.sub("SHRUB")) {
      int idx = 0;
      for (LXPoint cube : shrub.points) {
        // The wiring is laid out in rod order - so, for each one of 5 rods, go through 12 clusters
        int rodIndex = idx % 12;
        int clusterIdx = idx/5;
        if (rodIndex == (int)rodLayer.getValue() || clusterIdx == (int)clusterIndex.getValue() || shrubIdx == (int)shrubIndex.getValue()) {
            colors[cube.index] = LX.hsb(135, 100, 100);
        } else {
            colors[cube.index] = LX.hsb(135, 100, 0);
        }
        idx++;
      }
      shrubIdx++;
    }
  }
}
