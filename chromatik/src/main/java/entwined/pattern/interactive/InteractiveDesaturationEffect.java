package entwined.pattern.interactive;

import java.util.HashMap;
import java.util.Map;

import entwined.pattern.anon.ColorEffect;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class InteractiveDesaturationEffect {
  final public InteractiveDesaturation pieceEffects[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  // constructor
  public InteractiveDesaturationEffect(LX lx, LXModel model) {

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.children.length;
    this.nPieces = nPieces;
    this.pieceIdMap = new HashMap<String, Integer>();
    pieceEffects = new InteractiveDesaturation[nPieces];

    // XXX - the pieceIdMap is really something that a lot of patterns use, and probably should be in CubeManager,
    // or something core.
    int pieceIdx = 0;
    for (LXModel child : model.children) {
      String pieceName = child.meta("name");
      pieceEffects[pieceIdx] = new InteractiveDesaturation(lx, pieceIdx);
      pieceEffects[pieceIdx].label.setValue("Desat " + pieceName);
      this.pieceIdMap.put(pieceName, pieceIdx);
      pieceIdx++;
    }
  }

  public LXEffect[] getEffects() {
    return ( pieceEffects );
  }

  public void onTriggeredPiece(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) return;
    pieceEffects[pieceIndex_o ].onTriggered();
  }

  @LXComponent.Hidden
  public class InteractiveDesaturation extends ColorEffect {
    private boolean triggered;
    private long triggerEndMillis; // when to un-enable if enabled
    public final BooleanParameter onOff;   // This is really for debugging - allows us to turn the effect on and off from the UI

    InteractiveDesaturation(LX lx, int pieceIndex) {
      super(lx);

      this.onOff = new BooleanParameter("ONOFF");
      this.onOff.setValue(false);
      this.onOff.addListener(new LXParameterListener() {
        @Override
        public void onParameterChanged(LXParameter parameter) {
          triggered = onOff.getValueb();
          if (triggered) {
            triggerEndMillis = System.currentTimeMillis() + 6000;
          }
        }
      });

      addParameter("onOff_" + pieceIndex, this.onOff);

      // Some desaturation. Values of either 1 or 0 are boring.
      super.desaturation.setValue(.66);

      this.pieceIndex = pieceIndex;
      this.triggered = false;
    }

    @Override
    public void run(double deltaMs, double strength) {
      if (triggered == false) return;
      if (System.currentTimeMillis() > triggerEndMillis) onRelease();

      super.run(deltaMs, strength);
    }

    public void onTriggered() {
      triggered = true;
      triggerEndMillis = System.currentTimeMillis() + 3000;
    };

    public void onRelease() {
      triggered = false;
      this.onOff.setValue(false);
    }
  }
}
