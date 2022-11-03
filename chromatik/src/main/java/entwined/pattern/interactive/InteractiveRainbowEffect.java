package entwined.pattern.interactive;

import java.util.HashMap;
import java.util.Map;

import entwined.pattern.kyle_fleming.CandyCloudTextureEffect;
import heronarts.lx.LX;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;

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

    pieceEffects = new InteractiveRainbow[nPieces];
    for (int pieceIndex=0 ; pieceIndex<nPieces ; pieceIndex++) {
      pieceEffects[pieceIndex] = new InteractiveRainbow(lx, pieceIndex);
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


  class InteractiveRainbow extends CandyCloudTextureEffect {
    private boolean triggered;
    private long triggerEndMillis; // when to un-enable if enabled

    InteractiveRainbow(LX lx, int pieceIndex) {
      super(lx);

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
    };

    public void onRelease() {
      triggered = false;
    }
  }
}


