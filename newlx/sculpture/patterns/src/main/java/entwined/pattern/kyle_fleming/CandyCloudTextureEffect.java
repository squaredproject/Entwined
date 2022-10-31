package entwined.pattern.kyle_fleming;

import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;

public class CandyCloudTextureEffect extends LXEffect {

  public final BoundedParameter amount = new BoundedParameter("CLOU");

  double time = 0;
  final double scale = 2400;
  final double speed = 1.0f / 5000;

  // if set to a value >= 0, the effects are limited to only
  // the piece with that index (used for interactive effects)
  protected int pieceIndex = -1;

  public CandyCloudTextureEffect(LX lx) {
    super(lx);
    addParameter("amount", amount);
  }

  @Override
  public void run(double deltaMs, double strength) {
    if (amount.getValue() > 0) {
      time += deltaMs;
      int componentIdx = 0;
      for (LXModel component : model.children) {
        if (pieceIndex >= 0 && componentIdx != pieceIndex) continue;
        for (LXPoint cube : component.points) {
          double adjustedX = cube.x / scale;
          double adjustedY = cube.y / scale;
          double adjustedZ = cube.z / scale;
          double adjustedTime = time * speed;
          int oldColor = colors[cube.index];


          float newHue = ((float)SimplexNoise.noise(adjustedX, adjustedY, adjustedZ, adjustedTime) + 1) / 2 * 1080 % 360;
          int newColor = LX.hsb(newHue, 100, 100);

          int blendedColor = LXColor.lerp(oldColor, newColor, amount.getValuef());
          colors[cube.index] = LX.hsb(LXColor.h(blendedColor), LXColor.s(blendedColor), LXColor.b(oldColor));
        }
        componentIdx++;
      }
    }
  }
}
