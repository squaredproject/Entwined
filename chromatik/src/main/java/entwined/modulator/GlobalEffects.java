package entwined.modulator;

import entwined.pattern.anon.ColorEffect;
import entwined.pattern.anon.HueFilterEffect;
import entwined.pattern.kyle_fleming.CandyCloudTextureEffect;
import entwined.pattern.kyle_fleming.SpeedEffect;
import entwined.plugin.Entwined;
import heronarts.glx.ui.component.UIKnob;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

@LXCategory("Entwined")
@LXModulator.Global("Global FX")
public class GlobalEffects extends LXModulator implements UIModulatorControls<GlobalEffects> {

  private static final int CONTROL_PADDING = 2;
  private static final int CONTROL_SPACING = 15;
  private static final int CONTROLS_Y = 4;
  private static final int CONTROLS_ROW_HEIGHT = UIKnob.HEIGHT + 4;
  private static final int CONTROLS_PER_ROW = 4;

  private final LX lx;
  private final UIKnob[] knobs = new UIKnob[8];

  public GlobalEffects(LX lx) {
    super("Global FX");
    this.lx = lx;
    for (int i = 0; i < this.knobs.length; ++i) {
      int controlX = CONTROL_PADDING + (i % CONTROLS_PER_ROW) * (UIKnob.WIDTH + CONTROL_SPACING);
      int controlY = CONTROLS_Y + (i/CONTROLS_PER_ROW) * CONTROLS_ROW_HEIGHT;
      this.knobs[i] = new UIKnob(controlX, controlY);
    }
    setKnobs();

    // Listen for project changes and re-set the knobs!
    lx.addProjectListener((file, change) -> { setKnobs(); });
  }

  private void setKnobs() {
    for (UIKnob knob : this.knobs) {
      knob.setParameter(null);
    }
    ColorEffect color = Entwined.findMasterEffect(this.lx, ColorEffect.class);
    if (color != null) {
      this.knobs[0].setParameter(color.hueShift);
      this.knobs[1].setParameter(color.desaturation);
    }
    HueFilterEffect hueFilter = Entwined.findMasterEffect(this.lx, HueFilterEffect.class);
    if (hueFilter != null) {
      this.knobs[2].setParameter(hueFilter.hueFilter);
      this.knobs[3].setParameter(hueFilter.amount);
    }
    BlurEffect blur = Entwined.findMasterEffect(this.lx, BlurEffect.class);
    if (blur != null) {
      this.knobs[4].setParameter(blur.level);
    }
    SpeedEffect speed = Entwined.findMasterEffect(this.lx, SpeedEffect.class);
    if (speed != null) {
      this.knobs[5].setParameter(speed.speed);
    }
    /*
    SpinEffect spin = findEffect(SpinEffect.class);
    if (spin != null) {
      this.knobs[6].setParameter(spin.spin);
    }
    */
    CandyCloudTextureEffect candyCloud = Entwined.findMasterEffect(this.lx, CandyCloudTextureEffect.class);
    if (candyCloud != null) {
      this.knobs[7].setParameter(candyCloud.amount);
    }
  }



  @Override
  public void buildModulatorControls(UI ui, UIModulator uiModulator, GlobalEffects modulator) {
    uiModulator.addChildren(this.knobs);
    uiModulator.setContentHeight(2*CONTROLS_ROW_HEIGHT);
  }

  @Override
  protected double computeValue(double deltaMs) {
    return 0;
  }

}