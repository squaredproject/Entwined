import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

class MappingTool extends Effect {

  final List<TreeCubeConfig> cubeConfig;
  final List<ShrubCubeConfig> shrubCubeConfig;

  final SinLFO strobe = new SinLFO(20, 100, 1000);
  
  final DiscreteParameter ipIndex;
  final DiscreteParameter outputIndex;
  final DiscreteParameter stringOffsetIndex;
  final BooleanParameter showBlanks = new BooleanParameter("BLANKS", false);
  final DiscreteParameter shrubIpIndex;
  final DiscreteParameter shrubOutputIndex;
  final Object[] ipList;
  final Object[] shrubIpList;

  MappingTool(LX lx, List<TreeCubeConfig> cubeConfig, List<ShrubCubeConfig> shrubCubeConfig) {
    super(lx);
    this.cubeConfig = cubeConfig;
    this.shrubCubeConfig = shrubCubeConfig;
    this.ipList = model.ipMap.keySet().toArray();
    this.shrubIpList = model.shrubIpMap.keySet().toArray();
    if (ipList.length == 0) { System.out.println(" WARNING: no cubes in valid NDB configuration file, incorrect config, ABORTING"); }
    ipIndex = new DiscreteParameter("IP", ipList.length);
    // it is too hard to calculate the correct maximum for every NDB and change them, so pick large numbers
    outputIndex = new DiscreteParameter("POS", 16);
    stringOffsetIndex = new DiscreteParameter("OFFSET", 50);
    shrubIpIndex = new DiscreteParameter("SHRUB_IP", shrubIpList.length);
    shrubOutputIndex = new DiscreteParameter("SHRUB_POS", 5*12);
    addModulator(strobe).start();
    addLayer(new MappingLayer());
  }

  public void run(double deltaMs) {
  }

  Cube getCube(){
    NDBConfig ndbConfig = model.ndbMap.get(this.ipList[ipIndex.getValuei()]);
    int cubeIndex = ndbConfig.getCubeIndex(outputIndex.getValuei(), stringOffsetIndex.getValuei());
    if (cubeIndex == -1) return(null);
    return model.ipMap.get(this.ipList[ipIndex.getValuei()]) [ cubeIndex ];
  }

  TreeCubeConfig getConfig(){
    Cube c = getCube();
    if (c == null) return(null);
    return c.config;
  }
  
  ShrubCube getShrubCube(){
    return shrubModel.shrubIpMap.get(this.shrubIpList[shrubIpIndex.getValuei()])[shrubOutputIndex.getValuei()];
  }

  ShrubCubeConfig getShrubConfig(){
    return getShrubCube().config;
  }
  
  class MappingLayer extends Layer {
    
    MappingLayer() {
      super(MappingTool.this.lx);
    }
    
    public void run(double deltaMs) {
      if (isEnabled()) {
        Cube cube = getCube();
        if (cube != null) {
          blendColor(cube.index, lx.hsb(0, 0, strobe.getValuef()), LXColor.Blend.ADD);
        }
        blendColor(getShrubCube().index, lx.hsb(0, 0, strobe.getValuef()), LXColor.Blend.ADD);
      }
    }
  }
}
