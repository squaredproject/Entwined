import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

class MappingTool extends Effect {

  final List<CubeConfig> cubeConfig;

  final SinLFO strobe = new SinLFO(20, 100, 1000);
  
  final DiscreteParameter ipIndex;
  final DiscreteParameter outputIndex;
  final BooleanParameter showBlanks = new BooleanParameter("BLANKS", false);
  final Object[] ipList;

  MappingTool(LX lx, List<CubeConfig> cubeConfig) {
    super(lx);
    this.cubeConfig = cubeConfig;
    this.ipList = model.ipMap.keySet().toArray();
    ipIndex = new DiscreteParameter("IP", ipList.length);
    outputIndex = new DiscreteParameter("POS", 16);
    addModulator(strobe).start();
    addLayer(new MappingLayer());
  }

  public void run(double deltaMs) {
  }

  Cube getCube(){
    return model.ipMap.get(this.ipList[ipIndex.getValuei()])[outputIndex.getValuei()];
  }

  CubeConfig getConfig(){
    return getCube().config;
  }
  
  class MappingLayer extends Layer {
    
    MappingLayer() {
      super(MappingTool.this.lx);
    }
    
    public void run(double deltaMs) {
      if (isEnabled()) {
        blendColor(getCube().index, lx.hsb(0, 0, strobe.getValuef()), LXColor.Blend.ADD);
      }
    }
  }
}
