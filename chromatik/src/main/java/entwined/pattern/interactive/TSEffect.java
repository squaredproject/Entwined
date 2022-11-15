package entwined.pattern.interactive;

import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;


public abstract class TSEffect extends LXEffect
{
  public TSEffect(LX lx) {
    super(lx);
  }

  public void setColors(int[] colors) {
    this.colors = colors;
  }

  public void triggeredRun(double timeMs, double strength) {
    run(timeMs, strength);
  }
}
