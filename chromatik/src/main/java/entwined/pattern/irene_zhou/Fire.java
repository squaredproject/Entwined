package entwined.pattern.irene_zhou;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.core.TSTriggerablePattern;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;

public class Fire extends TSTriggerablePattern {
  final CompoundParameter maxHeight = new CompoundParameter("HEIGHT", 5.0, 0.3, 12);
  final CompoundParameter flameSize = new CompoundParameter("SIZE", 30, 10, 75);
  final CompoundParameter flameCount = new CompoundParameter ("FLAMES", 75, 0, 75);
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
  private LinearEnvelope fireHeight = new LinearEnvelope(0,0,500);

  private float height = 0;
  private int numFlames = 12;
  private List<Flame> flames;

  private class Flame {
    public float flameHeight = 0;
    public float theta = EntwinedUtils.random(0, 360);
    public LinearEnvelope decay = new LinearEnvelope(0,0,0);

    public Flame(float maxHeight, boolean groundStart){
      float flameHeight;
      if (EntwinedUtils.random(1) > .2f) {
        flameHeight = EntwinedUtils.pow(EntwinedUtils.random(0, 1), 3) * maxHeight * 0.3f;
      } else {
        flameHeight = EntwinedUtils.pow(EntwinedUtils.random(0, 1), 3) * maxHeight;
      }
      decay.setRange(model.yMin, (model.yMax * 0.9f) * flameHeight, EntwinedUtils.min(EntwinedUtils.max(200, 900 * flameHeight), 800));
      if (!groundStart) {
        decay.setBasis(EntwinedUtils.random(0,.8f));
      }
      addModulator(decay).start();
    }
  }

  public Fire(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter("maxHeight", maxHeight);
    addParameter("flameSize", flameSize);
    addParameter("flameCount", flameCount);
    addParameter("hue", hue);
    addModulator(fireHeight);

    flames = new ArrayList<Flame>(numFlames);
    for (int i = 0; i < numFlames; ++i) {
      flames.add(new Flame(height, false));
    }
  }

  public void updateNumFlames(int numFlames) {
    for (int i = flames.size(); i < numFlames; ++i) {
      flames.add(new Flame(height, false));
    }
  }

  @Override
  public void run(double deltaMs) {
    //if (getChannel().fader.getNormalized() == 0) return;

    if (!triggered && flames.size() == 0) {
      enabled.setValue(false);
      // setCallRun(false);
    }

    if (!triggerableModeEnabled) {
      height = maxHeight.getValuef();
      numFlames = (int) (flameCount.getValue() / 75 * 30); // Convert for backwards compatibility
    } else {
      height = fireHeight.getValuef();
    }

    if (flames.size() != numFlames) {
      updateNumFlames(numFlames);
    }
    for (int i = 0; i < flames.size(); ++i) {
      if (flames.get(i).decay.finished()) {
        removeModulator(flames.get(i).decay);
        if (flames.size() <= numFlames) {
          flames.set(i, new Flame(height, true));
        } else {
          flames.remove(i);
          i--;
        }
      }
    }

    for (LXPoint cube : model.points) {
      CubeData cdata = CubeManager.getCube(lx, cube.index);
      float yn = (cdata.localY - model.yMin) / model.yMax;
      float cBrt = 0;
      float cHue = 0;
      float flameWidth = flameSize.getValuef() / 2;
      for (int i = 0; i < flames.size(); ++i) {
        if (EntwinedUtils.abs(flames.get(i).theta - cdata.localTheta) < (flameWidth * (1- yn))) {
          cBrt = EntwinedUtils.min(
            100,
            EntwinedUtils.max(
              0,
              EntwinedUtils.max(
                cBrt,
                (100 - 2 * EntwinedUtils.abs(cdata.localY - flames.get(i).decay.getValuef()) - flames.get(i).decay.getBasisf() * 25) * EntwinedUtils.min(1, 2 * (1 - flames.get(i).decay.getBasisf()))
            )));
          cHue = EntwinedUtils.max(0,  (cHue + cBrt * 0.7f) * 0.5f);
        }
      }
      colors[cube.index] = LX.hsb(
        (cHue + hue.getValuef()) % 360,
        100,
        EntwinedUtils.min(100, cBrt + EntwinedUtils.pow(EntwinedUtils.max(0, (height - 0.3f) / 0.7f), 0.5f) * EntwinedUtils.pow(EntwinedUtils.max(0, 0.8f - yn), 2) * 75)
      );
    }

  }

  @Override
  public void onTriggered() {
    super.onTriggered();

    fireHeight.setRange(1,0.6f);
    fireHeight.reset().start();
  };

  @Override
  public void onReleased() {
    super.onReleased();

    fireHeight.setRange(height, 0);
    fireHeight.reset().start();
  }
}

