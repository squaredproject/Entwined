package entwined.plugin;

import java.util.Map;
import static java.util.Map.entry;
import java.util.HashMap;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;


// NFC server...
// The point of this code is to allow interactivity from NFC cards. For Elder Mother, there are
// 4 NFC readers located on the controller stump. Participants can grab an NFC card and place it on
// the reader, where a raspberry pi will transmit the information on the card back to the NFC server.
//
// The information sent by the Raspberry Pi is as follows:
// channel/<channelid>/pattern/<patternid> ,[T|F]
// This largely follows the OSC spec
//
// There are three types of patterns:
//   1) Base patterns, which play as long as the NFC card is near the reader.
//   2) One-shot effect patterns, which fire once when the NFC card comes near the reader.
//   3) Global effects, which modify the running base and one-shot patterns. These effects
//      are active as long as the NFC card is near the reader.
//
// The NFC card reading system and the this Chromatik server must agree ahead of time
// on the names of the patterns. At the moment, I'm not going to use the fully qualified
// class name as the pattern name, instead, I'll either hard code the association between
// class name and name alias (if I run out of time), or read this information from the config
// file.

public class NFCServer implements LXLoopTask {
  // XXX - why final on some of these things?
  TSServer server;
  EngineController engineController;
  HashMap<String, NFCPattern> patternMap;
  NFCPattern[] activities; // XXX must make it size 4, or whatever the size is
  LX lx;
  private boolean enabled = false;

  public enum NFCPatternType {
    BASE_PATTERN,
    ONE_SHOT,
    GLOBAL_MODIFIER
  }


  public class NFCPattern {
    public String patternClass;
    public NFCPatternType type;
    public int idx;

    public NFCPattern(String patternClass, NFCPatternType type, int idx) {
      this.patternClass = patternClass;
      this.type = type;
      this.idx = idx;
    }
  }


  public class NFCTrigger {
    public int channelIdx;
    public boolean onOff;
    public NFCPattern pattern;

    public NFCTrigger(int channelIdx, boolean onOff, NFCPattern pattern) {
      this.channelIdx = channelIdx;
      this.onOff = onOff;
      this.pattern = pattern;
      System.out.println("Create new NFC trigger, onoff is " + onOff);
    }
  }

  public void start() {
    if (this.enabled) {
      lx.engine.addLoopTask(this);
    }
  }


  public void shutdown() {
    if (this.enabled) {
      lx.engine.removeLoopTask(this);
      this.server.stop();
    }
  }


  NFCServer(LX lx, EngineController engineController) {
    if (!Config.NFCServerEnable) {
      return;
    }
    this.enabled = true;
    this.engineController = engineController;
    this.lx = lx;
    short port = Config.NFCPort != -1 ? Config.NFCPort : 7777;
    int numActivities = Config.NFCNumActivities != -1 ? Config.NFCNumActivities : 4;
    this.activities = new NFCPattern[numActivities];
    setupPatternMap();

    this.server = new TSServer(this, port);
  }


  private void setupPatternMap() {
    this.patternMap = new HashMap<String, NFCPattern>();
    this.patternMap.put("rainbow", new NFCPattern("com.entwined.rainbow", NFCPatternType.BASE_PATTERN, 4));
    this.patternMap.put("fire", new NFCPattern("com.entwined.fire", NFCPatternType.BASE_PATTERN, 5));
    this.patternMap.put("clouds", new NFCPattern("com.entwined.clouds", NFCPatternType.BASE_PATTERN, 10));
    this.patternMap.put("red", new NFCPattern("red", NFCPatternType.GLOBAL_MODIFIER, 0));
    this.patternMap.put("lightning", new NFCPattern("lightning", NFCPatternType.ONE_SHOT, 0));
  }


  public void loop(double deltaMs) {
    TSClient client = server.available();
    if (client == null) return;

    String command = client.readStringUntil('\n');
    if (command == null) {
      return;
    }

    NFCTrigger trigger = parseNFCCommand(command);
    if (trigger == null) {
      return;
    }

    if (!trigger.onOff) {
      this.activities[trigger.channelIdx] = null;
    } else {
      if (trigger.pattern.type == NFCPatternType.BASE_PATTERN) {
        this.activities[trigger.channelIdx] = trigger.pattern;
        int channelIdx = engineController.baseChannelIndex + trigger.channelIdx;
        engineController.setChannelPattern(
            channelIdx,
            trigger.pattern.idx);
      } else if (trigger.pattern.type == NFCPatternType.ONE_SHOT) {
        // TODO - trigger effect on effects channels in engine
        // engine.interactiveHSVEffect.resetPiece(pieceId);
        // engine.interactiveDesaturationEffect.onTriggeredPiece(pieceId);
      } else if (trigger.pattern.type == NFCPatternType.GLOBAL_MODIFIER) {
        if (trigger.pattern.patternClass == "color1") {
          engineController.setHue(1.0); // XXX - read, or something..
        } else if (trigger.pattern.patternClass == "spin") {
          //engineController.setSpin(10.0); // XXX - I don't actually have a spin..
        } else if (trigger.pattern.patternClass == "speed") {
          engineController.setSpeed(1.0); // XXX what is a good value?
        }
      }
    }
    modifyIntensities();
  }

  private void modifyIntensities() {
    int numBasePatterns = 0;
    float intensityModifier = 1.0f;
    for (NFCPattern pattern : this.activities) {
      if (pattern != null &&
          pattern.type == NFCPatternType.BASE_PATTERN) {
        numBasePatterns++;
      }
    }
    if (numBasePatterns > 0) {
      intensityModifier = 1.0f/numBasePatterns;
    }
    int idx = 0;
    for (NFCPattern pattern : this.activities) {
      int channelIdx = engineController.baseChannelIndex + idx;
      if (pattern == null) {
        engineController.setChannelVisibility(channelIdx, 0.0);
      } else {
        engineController.setChannelVisibility(channelIdx, intensityModifier);
      }
      idx++;
    }
  }

  private NFCTrigger parseNFCCommand(String command){
    int channelIdx;
    boolean onOff;
    NFCPattern pattern;
    // Format of the command is channel/<channelid>/pattern/<patternName> ,[T|F]
    try {
      if (!command.startsWith("channel/")) {
        throw new RuntimeException("Does not start with 'channel/'");
      }

      channelIdx = Character.getNumericValue(command.charAt(8));
      if (channelIdx > 9 || channelIdx < 0) {
        throw new RuntimeException("Channel Idx OOB");
      }

      if (!command.substring(9,18).equals("/pattern/")) {
        throw new RuntimeException("Does not contain '/pattern/' at correct location");
      }

      int spacerIdx = command.indexOf(" ,");
      if (spacerIdx < 0 || spacerIdx < 19) {
        throw new RuntimeException("Does not contain spacer characters");
      }

      String patternName = command.substring(18, spacerIdx);

      char tf = command.charAt(spacerIdx + 2);
      if (tf != 'T'&& tf != 'F') {
        throw new RuntimeException("Does not contain T|F signal ");
      }
      System.out.println("Parse NFC command, onoff is " + tf);
      onOff = (tf == 'T');

      // Okay, string believed to be parseable
      // Check semantic validity
      if (channelIdx > 3 || channelIdx < 0) {
        throw new RuntimeException("Channel must be in [0,3]");
      }

      pattern = this.patternMap.get(patternName);
      if (pattern == null) {
        throw new RuntimeException("Pattern name " + patternName + " not found");
      }

    } catch(Exception e) {
      System.out.println("Could not parse NFC command " + command + ", " + e.getMessage());
      return null;
    }

    return new NFCTrigger(channelIdx, onOff, pattern);
  }
} // NFCServer
