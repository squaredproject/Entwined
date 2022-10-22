package entwined.pattern.colin_hunt;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

import entwined.core.CubeManager;

/*
need to have subfixtures to do this... or
*/
public class PartyRings extends LXPattern {

  private List<RingStack> ringStacks = new ArrayList<RingStack>();

  public PartyRings(LX lx) {
    super(lx);
    for (int shrubIdx = 0; shrubIdx <  model.sub("SHRUB").size(); shrubIdx++) {
      ringStacks.add(new RingStack());
    }
  }

  @Override
  public void run(double deltaMs) {

    int shrubIdx = 0;
    for (LXModel shrub: model.sub("SHRUB")) {
      int pointIdx = 0;
      for (LXPoint cube : shrub.points) {
        int rodIndex = pointIdx % 5;
        float localTheta = CubeManager.getCube(cube.index).localTheta;
        colors[cube.index] = LX.hsb(ringStacks.get(shrubIdx).getHue(rodIndex), 100, ringStacks.get(shrubIdx).getBright(localTheta, rodIndex));
        pointIdx++;
      }
      shrubIdx++;
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
      return (Math.abs((theta - head[rodPosition - 1]) / 180.0f)) * 100.0f;
    }
  }
}

