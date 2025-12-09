package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class CandyTextureEffect extends LXEffect {

  public final CompoundParameter amount = new CompoundParameter("CAND");

  double time = 0;

  // if set to a value >= 0, the effects are limited to only
  // the piece with that index (used for interactive effects)
  // XXX - and how do we set this?
  protected int pieceIndex = -1;

  public CandyTextureEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);
  }

  public double getAmount() {
    return amount.getValue();
  }

  public void setAmount(double val) {
    amount.setValue(val);
  }

  @Override
  public void run(double deltaMs, double strength) {
    if (amount.getValue() > 0) {
      time += deltaMs;
//     int componentIdx = 0;

      for (LXModel component : model.children) {
//         if (componentIdx == pieceIndex) {
          for (LXPoint cube : component.points) {
            int oldColor = colors[cube.index];
            float newHue = cube.index * 127 + 9342 + (float)time % 360;
            int newColor = LX.hsb(newHue, 100, 100);
            int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
            colors[cube.index] = LX.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
          }
 //       }
 //       componentIdx++;
      }
    }
  }
}
