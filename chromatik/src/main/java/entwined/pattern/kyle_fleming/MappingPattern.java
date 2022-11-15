package entwined.pattern.kyle_fleming;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

public class MappingPattern extends LXPattern {

  int numBits;
  int count;
  int numCompleteResetCycles = 10;
  int numCyclesToShowFrame = 4;
  int numResetCycles = 3;
  int numCyclesBlack = 2;
  int cycleCount = 0;
  int stepTimeMs = 500;
  int elapsedTimeMs = 0;
  public MappingPattern(LX lx) {
    super(lx);
    count = 0;

    numBits = model.points.length;
  }

  @Override
  public void run(double deltaMs) {
    elapsedTimeMs += deltaMs;
    if (count >= numBits) {
      if (numBits + numCyclesBlack <= count && count < numBits + numCyclesBlack + numCompleteResetCycles) {
        setColors(LXColor.WHITE);
      } else {
        setColors(LXColor.BLACK);
      }
    } else if (cycleCount >= numCyclesToShowFrame) {
      if (numCyclesToShowFrame + numCyclesBlack <= cycleCount && cycleCount < numCyclesToShowFrame + numCyclesBlack + numResetCycles) {
        setColors(LXColor.WHITE);
      } else {
        setColors(LXColor.BLACK);
      }
    } else {
      for (LXPoint cube : model.points) {
        setColor(cube.index, cube.index == count ? LXColor.WHITE : LXColor.BLACK);
      }
    }
    if (elapsedTimeMs > stepTimeMs) {
      elapsedTimeMs = 0;
      cycleCount = (cycleCount + 1) % (numCyclesToShowFrame + numResetCycles + 2*numCyclesBlack);
      if (cycleCount == 0) {
        count = (count + 1) % (numBits + numCompleteResetCycles + 2*numCyclesBlack);
      }
    }
  }
}

