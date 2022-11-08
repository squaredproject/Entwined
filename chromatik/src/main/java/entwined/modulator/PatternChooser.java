package entwined.modulator;

import heronarts.glx.ui.component.UIComponentLabel;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.mixer.LXBus;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIPatternList;
import heronarts.lx.studio.ui.modulation.UIModulator;
import heronarts.lx.studio.ui.modulation.UIModulatorControls;

@LXCategory("Entwined")
@LXModulator.Global("Pattern Chooser")
public class PatternChooser extends LXModulator implements UIModulatorControls<PatternChooser> {

  public PatternChooser(LX lx) {
    super("Pattern Chooser");
  }

  @Override
  public void buildModulatorControls(UI ui, UIModulator uiModulator, PatternChooser modulator) {
    uiModulator.addListener(ui.lx.engine.mixer.focusedChannel, p -> {
      uiModulator.removeAllChildren();
      uiModulator.setLayout(UIModulator.Layout.VERTICAL);
      uiModulator.setChildSpacing(4);
      LXBus bus = ui.lx.engine.mixer.getFocusedChannel();
      if (bus instanceof LXChannel) {
        final LXChannel channel = (LXChannel) bus;

        new UIComponentLabel(0, 0, uiModulator.getContentWidth(), 18)
        .setCanonical(false)
        .setComponent(channel)
        .setBorderRounding(4)
        .setBackgroundColor(ui.theme.controlBackgroundColor)
        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE)
        .setTextOffset(4, 0)
        .addToContainer(uiModulator);

        new UIPatternList(ui, 0, 0, (int) uiModulator.getContentWidth(), 320, channel)
        .addToContainer(uiModulator);
      }
    }, true);

  }


  @Override
  protected double computeValue(double deltaMs) {
    return 0;
  }

}
