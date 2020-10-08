import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.parameter.BooleanParameter;

class NFCEngine {
  private class NFCEngineCardListener implements NFCCardListener {
    public void onReaderAdded(String reader) {
      VisualType nextPatternType = readerPatternTypeRestrictionArray.get(readerPatternTypeRestrictionIndex);
      readerPatternTypeRestrictionIndex++;
      readerToPatternTypeRestrictionMap.put(reader, nextPatternType);
    }

    public void onReaderRemoved(String reader) {
      readerPatternTypeRestrictionIndex = 0;
      readerToPatternTypeRestrictionMap.clear();
    }

    public void onCardAdded(String reader, String cardId) {
      NFCEngineVisual visual = cardToTriggerableMap.get(cardId);
      if (visual != null) {
        VisualType readerRestriction = readerToPatternTypeRestrictionMap.get(reader);
        if (disableVisualTypeRestrictions || readerRestriction == visual.VisualType) {
          if (visual.toggle != null) {
            visual.toggle.setValue(true);
          }
          addNFCEvent(new NFCTriggeredMessage(visual.triggerable));
        }
      }
      System.out.println(reader + " added card " + cardId);
    }
    
    public void onCardRemoved(String reader, String cardId) {
      NFCEngineVisual visual = cardToTriggerableMap.get(cardId);
      if (visual != null) {
        VisualType readerRestriction = readerToPatternTypeRestrictionMap.get(reader);
        if (disableVisualTypeRestrictions || readerRestriction == visual.VisualType) {
          if (visual.toggle != null) {
            visual.toggle.setValue(false);
          }
          addNFCEvent(new NFCReleaseMessage(visual.triggerable));
        }
      }
    }
  }

  private class NFCLoopTaskDispatch implements LXLoopTask {
    private final List<NFCMessage> localThreadEventQueue = new ArrayList<NFCMessage>();

    public void loop(double deltaMs) {
      localThreadEventQueue.clear();
      synchronized (nfcThreadEventQueue) {
        localThreadEventQueue.addAll(nfcThreadEventQueue);
        nfcThreadEventQueue.clear();
      }
      for (NFCMessage message : localThreadEventQueue) {
        message.dispatch();
      }
    }
  }

  private abstract class NFCMessage {
    Triggerable triggerable;

    NFCMessage(Triggerable triggerable) {
      this.triggerable = triggerable;
    }

    abstract void dispatch();
  }

  private class NFCReleaseMessage extends NFCMessage {
    NFCReleaseMessage(Triggerable triggerable) {
      super(triggerable);
    }

    void dispatch() {
      triggerable.onRelease();
    }
  }

  private class NFCTriggeredMessage extends NFCMessage {
    NFCTriggeredMessage(Triggerable triggerable) {
      super(triggerable);
    }
    
    void dispatch() {
      triggerable.onTriggered(1);
    }
  }

  private class NFCEngineVisual {
    Triggerable triggerable;
    VisualType VisualType;
    BooleanParameter toggle;

    NFCEngineVisual(Triggerable triggerable, VisualType VisualType, BooleanParameter toggle) {
      this.triggerable = triggerable;
      this.VisualType = VisualType;
      this.toggle = toggle;
    }
  }

  boolean disableVisualTypeRestrictions;

  private final LX lx;
  private LibNFC libNFC;
  private LibNFCMainThread libNFCMainThread;
  private final NFCEngineCardListener cardReader = new NFCEngineCardListener();
  private final Map<String, NFCEngineVisual> cardToTriggerableMap = new HashMap<String, NFCEngineVisual>();
  private final Map<String, VisualType> readerToPatternTypeRestrictionMap = new HashMap<String, VisualType>();
  private List<VisualType> readerPatternTypeRestrictionArray;
  private int readerPatternTypeRestrictionIndex = 0;
  private final List<NFCMessage> nfcThreadEventQueue = Collections.synchronizedList(new ArrayList<NFCMessage>());
  private final NFCLoopTaskDispatch nfcLoopTaskDispatch = new NFCLoopTaskDispatch();
  
  public NFCEngine(LX lx) {
    this.lx = lx;
    try {
      libNFC = new LibNFC();
      libNFCMainThread = new LibNFCMainThread(libNFC, cardReader);
      lx.engine.addLoopTask(nfcLoopTaskDispatch);
    } catch(Exception e) {
      System.out.println("nfc engine initialization error: " + e.toString());
      libNFC = null;
      libNFCMainThread = null;
    }
  }
  
  public void start() {
    if (libNFCMainThread != null) {
      libNFCMainThread.start();
    }
  }
  
  // Thread.stop() is deprecated
  // public void stop() {
  //   if (libNFCMainThread != null) {
  //     libNFCMainThread.stop();
  //   }
  // }
  
  public void registerTriggerable(String serialNumber, Triggerable triggerable, VisualType VisualType, BooleanParameter toggle) {
    cardToTriggerableMap.put(serialNumber, new NFCEngineVisual(triggerable, VisualType, toggle));
  }

  public void registerReaderPatternTypeRestrictions(List<VisualType> readerPatternTypeRestrictionArray) {
    this.readerPatternTypeRestrictionArray = readerPatternTypeRestrictionArray;
  }

  private void addNFCEvent(NFCMessage message) {
    synchronized (nfcThreadEventQueue) {
      nfcThreadEventQueue.add(message);
    }
  }
}
