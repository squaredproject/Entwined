package entwined.pattern.interactive;

import java.util.HashMap;
import java.util.Map;

import entwined.pattern.kyle_fleming.CandyCloudTextureEffect;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class InteractiveRainbowEffect {
  final public InteractiveRainbow pieceEffects[];

  final int nPieces;
  final Map<String, Integer> pieceIdMap;

  // constructor
  public InteractiveRainbowEffect(LX lx, LXModel model) {

    // Need to know the different pieces that exist, and be able to look them up by name
    //
    int nPieces = model.children.length;
    this.nPieces = nPieces;
    this.pieceIdMap = new HashMap<String, Integer>();
    // XXX - the pieceIdMap is really something that a lot of patterns use, and probably should be in CubeManager,
    // or something core.
    int pieceIdx = 0;
    pieceEffects = new InteractiveRainbow[nPieces];
    for (LXModel child : model.children) {
      String pieceName = child.meta("name");
      this.pieceIdMap.put(pieceName, pieceIdx);
      pieceEffects[pieceIdx] = new InteractiveRainbow(lx, pieceIdx);
      pieceEffects[pieceIdx].label.setValue("CandyCloud " + pieceName);
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
  public class InteractiveRainbow extends CandyCloudTextureEffect {
    public final BooleanParameter onOff;   // This is really for debugging - allows us to turn the effect on and off from the UI
    private boolean triggered;
    private long triggerEndMillis; // when to un-enable if enabled

    InteractiveRainbow(LX lx, int pieceIndex) {
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

      // turn the effect on 100%
      super.amount.setValue(1);

      this.pieceIndex = pieceIndex;
      this.triggered = false;
    }

    @Override
    public void run(double deltaMs, double amount) {
      if (triggered == false) return;
      if (System.currentTimeMillis() > triggerEndMillis) onRelease();

      super.run(deltaMs, amount);
    }

    public void onTriggered() {
      triggered = true;
      triggerEndMillis = System.currentTimeMillis() + 3000;
      this.onOff.setValue(true);
    };

    public void onRelease() {
      triggered = false;
      this.onOff.setValue(false);
    }
  }
}


