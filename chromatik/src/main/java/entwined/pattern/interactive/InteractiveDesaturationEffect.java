package entwined.pattern.interactive;

import java.util.HashMap;
import java.util.Map;

import entwined.pattern.anon.ColorEffect;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;

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
    for (int pieceIndex=0 ; pieceIndex<nPieces ; pieceIndex++) {
      pieceEffects[pieceIndex] = new InteractiveDesaturation(lx, pieceIndex);
    }
  }

  LXEffect[] getEffects() {
    return ( pieceEffects );
  }

  void onTriggeredPiece(String pieceId) {
    Integer pieceIndex_o = pieceIdMap.get(pieceId);
    if (pieceIndex_o == null) return;
    pieceEffects[pieceIndex_o ].onTriggered();
  }


  class InteractiveDesaturation extends ColorEffect {
    private boolean triggered;
    private long triggerEndMillis; // when to un-enable if enabled

    InteractiveDesaturation(LX lx, int pieceIndex) {
      super(lx);

      // turn the effect on 100%
      super.desaturation.setValue(1);

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
    }
  }
}
