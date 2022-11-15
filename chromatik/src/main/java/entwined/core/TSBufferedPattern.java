package entwined.core;

import heronarts.lx.LX;
import heronarts.lx.ModelBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.pattern.LXPattern;

public abstract class TSBufferedPattern extends LXPattern {
  private final ModelBuffer myBuffer = new ModelBuffer(lx, LXColor.BLACK);

  protected TSBufferedPattern(LX lx) {
    super(lx);
  }

  @Override
  final protected void run(double deltaMs) {
    // Restore the previous frame content before updating pixels
    this.myBuffer.copyTo(getBuffer());
    bufferedRun(deltaMs);
    // Keep a copy of our rendered state around
    this.myBuffer.copyFrom(getBuffer());
  }

  abstract protected void bufferedRun(double deltaMs);
}