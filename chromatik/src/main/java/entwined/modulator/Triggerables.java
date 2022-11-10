package entwined.modulator;

import entwined.core.Triggerable;
import heronarts.glx.ui.component.UIButton;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.midi.surface.APC40;
import heronarts.lx.mixer.LXChannel;
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
  // the API
  // And then we get rid of this to unify the interfaces...
  /*
  public static interface Triggerable {
    public void onTriggered();
    public void onReleased();
    public void onTimeout();
  }
  */
  /*
  BlurEffect blurEffect = new TSBlurEffect(lx);
  ColorEffect colorEffect = new ColorEffect(lx);
  HueFilterEffect hueFilterEffect = new HueFilterEffect(lx);
  GhostEffect ghostEffect = new GhostEffect(lx);
  ScrambleEffect scrambleEffect = new ScrambleEffect(lx);
  StaticEffect staticEffect = new StaticEffect(lx);
  // RotationEffect rotationEffect = new RotationEffect(lx);
  // SpinEffect spinEffect = new SpinEffect(lx);
  SpeedEffect speedEffect = new SpeedEffect(lx);
  ColorStrobeTextureEffect colorStrobeTextureEffect = new ColorStrobeTextureEffect(lx);
  FadeTextureEffect fadeTextureEffect = new FadeTextureEffect(lx);
  AcidTripTextureEffect acidTripTextureEffect = new AcidTripTextureEffect(lx);
  CandyTextureEffect candyTextureEffect = new CandyTextureEffect(lx);
  CandyCloudTextureEffect candyCloudTextureEffect = new CandyCloudTextureEffect(lx);

  lx.addEffect(blurEffect);
  lx.addEffect(colorEffect);
  lx.addEffect(hueFilterEffect);
  lx.addEffect(ghostEffect);
  lx.addEffect(scrambleEffect);
  lx.addEffect(staticEffect);
  //lx.addEffect(rotationEffect);
  //lx.addEffect(spinEffect);
  lx.addEffect(speedEffect);
  lx.addEffect(colorStrobeTextureEffect);
  lx.addEffect(fadeTextureEffect);
  lx.addEffect(acidTripTextureEffect);
  lx.addEffect(candyTextureEffect);
  lx.addEffect(candyCloudTextureEffect);

  // Okay. So I have 12 effects for a thing with 8 buttons. wtf.
  // No, the control parameters are getting mapped, not the effects themselves.
  // so... how do I trigger the effect? And why are is one parameter mapped to a single controller?

  // These controls are on the first row...
  registerEffectControlParameter(speedEffect.speed, 1, 0.4);
  registerEffectControlParameter(speedEffect.speed, 1, 5);
  registerEffectControlParameter(colorEffect.rainbow);
  registerEffectControlParameter(colorEffect.mono);
  registerEffectControlParameter(colorEffect.desaturation);
  registerEffectControlParameter(colorEffect.sharps);
  registerEffectControlParameter(hueFilterEffect.hueFilter);
  registerEffectControlParameter(hueFilterEffect.amount);
  // registerEffectControlParameter(blurEffect.amount, 0.65);  // XXX no amount paramter on current heronarts blur effect
  // registerEffectControlParameter(spinEffect.spin, 0.65);
  // The following are on the second row
  registerEffectControlParameter(ghostEffect.amount, 0, 0.16, 1);
  registerEffectControlParameter(scrambleEffect.amount, 0.0, 1.0, 1.0);
  registerEffectControlParameter(colorStrobeTextureEffect.amount 0, 1, 1);
  registerEffectControlParameter(fadeTextureEffect.amount, 0, 1, 1);
  registerEffectControlParameter(acidTripTextureEffect.amount, 0, 1, 1);
  registerEffectControlParameter(candyCloudTextureEffect.amount, 0, 1, 1);
  registerEffectControlParameter(staticEffect.amount, 0, .3, 1);

  // except for this one, which appears to be on the 6th row. Of course I don't have a 6th row now.
  registerEffectControlParameter(candyTextureEffect.amount, 0, 1, 5);


  // colorEffect.mono is pretty good, but has been kicked off the island compared to hueFilterEffect

  // And then we have these effect knob parameters...

  effectKnobParameters = new LXListenableNormalizedParameter[]{
      colorEffect.hueShift,
      colorEffect.desaturation,
      hueFilterEffect.hueFilter,
      hueFilterEffect.amount,
      blurEffect.amount,
      speedEffect.speed,
      spinEffect.spin,
      candyCloudTextureEffect.amount
  };
  */

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

