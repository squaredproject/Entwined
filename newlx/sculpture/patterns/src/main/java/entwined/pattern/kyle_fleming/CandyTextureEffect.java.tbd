package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

public class CandyTextureEffect extends LXEffect {

  protected final BoundedParameter amount = new BoundedParameter("CAND");

  double time = 0;

  // if set to a value >= 0, the effects are limited to only
  // the piece with that index (used for interactive effects)
  protected int pieceIndex = -1;

  public CandyTextureEffect(LX lx) {
    super(lx);
  }

  @Override
  public void run(double deltaMs, double strength) {
    if (amount.getValue() > 0) {
      time += deltaMs;
      for (int i = 0; i < colors.length; i++) {
        int oldColor = colors[i];
        LXPoint cube = model.points[i];

        // if we're only applying this effect to a given pieceIndex, filter out and don't set colors on other cubes
        if (pieceIndex >= 0 && cube.pieceIndex != pieceIndex) continue;

        float newHue = i * 127 + 9342 + (float)time % 360;
        int newColor = LX.hsb(newHue, 100, 100);
        int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
        colors[i] = LX.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
      }
    }
  }
}
