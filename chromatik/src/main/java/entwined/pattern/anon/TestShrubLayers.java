package entwined.pattern.anon;

import entwined.plugin.Entwined;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.TEST)
public class TestShrubLayers extends LXPattern {

  final CompoundParameter rodLayer;
  final CompoundParameter clusterIndex;
  final CompoundParameter shrubIndex;

  public TestShrubLayers(LX lx) {
    super(lx);
    // lowest level means turning that param off
    addParameter("rodLayer", rodLayer = new CompoundParameter("layer", 0, 0, 5));
    addParameter("clusterIndex", clusterIndex = new CompoundParameter("clusterIndex", -1, -1, 11));
    addParameter("shrubIndex", shrubIndex = new CompoundParameter("shrubIndex", -1, -1, 19));
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    int shrubIdx = 0;
    for (LXModel shrub : model.sub("SHRUB")) {
      int idx = 0;
      for (LXPoint cube : shrub.points) {
        // The wiring is laid out in rod order - so, for each one of 5 rods, go through 12 clusters
        int clusterIdx = Entwined.getCubeCluster(idx);
        int rodIndex = Entwined.getCubeLayer(idx);
        if (clusterIdx == (int)clusterIndex.getValue() || rodIndex == (int)rodLayer.getValue() || shrubIdx == (int)shrubIndex.getValue()) {
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
