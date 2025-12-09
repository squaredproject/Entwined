package entwined.pattern.kyle_fleming;

import java.util.ArrayList;
import java.util.Iterator;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class GhostEffect extends LXEffect {

  final CompoundParameter amount = new CompoundParameter("GHOS", 0, 0, 1).setExponent(2);

  public GhostEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);
    addLayer(new GhostEffectsLayer(lx));
  }

  @Override
  protected void run(double deltaMs, double amount) {
  }

  public float getAmount() {
    return amount.getValuef();
  }

  class GhostEffectsLayer extends LXLayer {

    GhostEffectsLayer(LX lx) {
      super(lx);
      //addParameter("amount", amount);
    }

    float timer = 0;
    ArrayList<GhostEffectLayer> ghosts = new ArrayList<GhostEffectLayer>();

    @Override
    public void run(double deltaMs) {
      if (amount.getValue() != 0) {
        timer += deltaMs;
        float lifetime = (float)amount.getValue() * 2000;
        if (timer >= lifetime) {
          timer = 0;
          GhostEffectLayer ghost = new GhostEffectLayer(lx);
          ghost.lifetime = lifetime * 3;
          addLayer(ghost);
          ghosts.add(ghost);
        }
      }
      if (ghosts.size() > 0) {
        Iterator<GhostEffectLayer> iter = ghosts.iterator();
        while (iter.hasNext()) {
          GhostEffectLayer ghost = iter.next();
          if (!ghost.running) {
            removeLayer(ghost);
            iter.remove();
          }
        }
      }
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
      if (parameter == amount && parameter.getValue() == 0) {
        timer = 0;
      }
    }
  }

  class GhostEffectLayer extends LXLayer {

    float lifetime;
    boolean running = true;

    private int[] ghostColors = null;
    float timer = 0;

    GhostEffectLayer(LX lx) {
      super(lx);
    }

    @Override
    public void run(double deltaMs) {
      if (running) {
        timer += (float)deltaMs;
        if (timer >= lifetime) {
          running = false;
        } else {
          if (ghostColors == null) {
            ghostColors = new int[colors.length];
            for (int i = 0; i < colors.length; i++) {
              ghostColors[i] = colors[i];
            }
          }

          for (int i = 0; i < colors.length; i++) {
            ghostColors[i] = LXColor.blend(ghostColors[i], LX.hsb(0, 0, 100 * EntwinedUtils.max(0, (float)(1 - deltaMs / lifetime))), LXColor.Blend.MULTIPLY);
            blendColor(i, ghostColors[i], LXColor.Blend.LIGHTEST);
          }
        }
      }
    }
  }
}
