package entwined.modulator;

import entwined.core.Triggerable;
import heronarts.glx.ui.component.UIButton;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.midi.surface.APC40;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

// if I go move to triggerable effect rather than LX effect
// May want timeout in the class itself. Let everyone have their own timeout? Maybe...
// Okay. We set the things as triggerable, and we watch for the flag

@LXCategory("Entwined")
@LXModulator.Global("Triggerables")
public class Triggerables extends LXModulator implements UIModulatorControls<Triggerables> {

  // XXX - the previous code had a 'strength' in the onTrigger, which was the drumpad velocity,
  // or really, the midi note velocity. This was rarely used, but is a somewhat cool feature to have in
  // the APIs


  public static final int NUM_ROWS = APC40.CLIP_LAUNCH_ROWS + 1;
  public static final int NUM_COLS = APC40.NUM_CHANNELS + 1;

  public final BooleanParameter[][] grid;

  public final Triggerable[][] actions = new Triggerable[NUM_ROWS][NUM_COLS];

  private final double[][] timeouts = new double[NUM_ROWS][NUM_COLS];

  private static final double TIMEOUT_MS = 3000;

  // Construct Triggerables - an array of Boolean parameters that will
  // control triggering of effects and patterns.
  public Triggerables(LX lx) {
    super("Triggerables");
    this.grid = new BooleanParameter[NUM_ROWS][NUM_COLS];
    for (int i = 0; i < NUM_ROWS; ++i) {
      for (int j = 0; j < NUM_COLS; ++j) {
        this.grid[i][j] =
          new BooleanParameter("Grid[" + i + "][" + j + "]", false)
          .setMode(BooleanParameter.Mode.MOMENTARY)
          .setDescription("Grid button " + i + "/" + j);
        addParameter("grid-" + i + "-" + j, this.grid[i][j]);
      }
    }
  }

  public Triggerables setAction(int row, int col, Triggerable action) {
    this.actions[row][col] = action;
    return this;
  }

  public Triggerables createPatternAction(int row, int col, LXPattern pattern) {
    if (pattern == null) {
     return this;
    }
   return setAction(row, col, new Triggerable() {
     @Override
     public void onTriggered() {
       pattern.enabled.setValue(true);
     }
     @Override
     public void onReleased() {
       pattern.enabled.setValue(false);

     }
     @Override
     public void onTimeout() {
       pattern.enabled.setValue(false);

     }
   });
}

  // Okay, he's calling a timeout on each of the triggerables
  @Override
  public void onParameterChanged(LXParameter p) {
    for (int i = 0; i < NUM_ROWS; ++i) {
      for (int j = 0; j < NUM_COLS; ++j) {
        if (this.grid[i][j] == p) {
          final boolean isOn = this.grid[i][j].isOn();
          final Triggerable triggerable = this.actions[i][j];
          if (triggerable != null) {
            if (isOn) {
              this.timeouts[i][j] = TIMEOUT_MS;
              triggerable.onTriggered();
            } else {
              this.timeouts[i][j] = -1;
              triggerable.onReleased();
            }
          }
          return;
        }
      }
    }
  }

  @Override
  public void buildModulatorControls(UI ui, UIModulator uiModulator, Triggerables modulator) {
    for (int row = 0; row < this.grid.length; ++row) {
      for (int col = 0; col < this.grid[row].length; ++col) {
        new UIButton(3 + col*23, 3 + row*23, 19, 19).setParameter(grid[row][col]).addToContainer(uiModulator);
      }
    }
    uiModulator.setContentHeight(140);
  }

  @Override
  protected double computeValue(double deltaMs) {
    // Countdown timers for actions that need to expire...
    for (int i = 0; i < NUM_ROWS; ++i) {
      for (int j = 0; j < NUM_COLS; ++j) {
        if (this.timeouts[i][j] > 0) {
          this.timeouts[i][j] -= deltaMs;
          if (this.timeouts[i][j] <= 0) {
            final Triggerable triggerable = this.actions[i][j];
            if (triggerable != null) {
              triggerable.onTimeout();
            }
          }
        }
      }
    }
    return 0;
  }
}

