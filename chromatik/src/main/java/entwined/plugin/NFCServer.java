package entwined.plugin;

import java.util.Map;
import static java.util.Map.entry;
import java.util.HashMap;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;

import entwined.core.Triggerable;
import entwined.core.TSTriggerablePattern;
import entwined.pattern.ray_sykes.Lightning;
import entwined.pattern.irene_zhou.Bubbles;
import entwined.pattern.kyle_fleming.BassSlam;
import entwined.pattern.kyle_fleming.ColorStrobe;
import entwined.pattern.kyle_fleming.Wisps;
import entwined.pattern.kyle_fleming.Rain;
import entwined.utils.EntwinedUtils;

// NFC server...
// The point of this code is to allow interactivity from NFC cards. For Elder Mother, there are
// 4 NFC readers located on the controller stump. Participants can grab an NFC card and place it on
// the reader, where a raspberry pi will transmit the information on the card back to the NFC server.
//
// The information sent by the Raspberry Pi is as follows:
// channel/<channelid>/pattern/<patternid> ,s [T|F]
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
  Boolean autoPlaying = false;
  HashMap<String, NFCPattern> patternMap;
  HashMap<String, NFCOneShot> oneShotMap;
  NFCPattern[] activities; // XXX must make it size 4, or whatever the size is
  LX lx;
  private boolean enabled = false;
  static final int ONE_SHOT_TIMEOUT_MS = 4000;

  private int ATTRACT_MODE_DELTA_MS = 4000;  // Default value, read from config
  public int attractModeTimeout = -1;
  public boolean attractModeEnable = false;
  private int numActivities;

  // just an engineController.setAutoplay(T/F)

  // Base structures:
  // The system enables a number of NFCPatterns that can be triggered by the NFC cards.
  // NFC patterns are either one-shots, global modifiers, or patterns running on one of
  // the server pattern channels.
  // A OSC style trigger will generate an NFCTrigger object, specifying which NFCPattern
  // being effected, whether it's being turned on or off, and which channel it's being
  // run on.
  // One shot patterns will only be active for a short period of time. They are managed by
  // a hashmap of NFCOneShots, which maintain active/inactive state.

  // NFCPattern - the pattern that we're going to be running, with some metadata
  public enum NFCPatternType {
    BASE_PATTERN,
    ONE_SHOT,
    GLOBAL_MODIFIER
  }

  public class NFCPattern {
    public NFCPatternType type;
    public Triggerable triggerable;
    public String patternName;

    public NFCPattern(String patternName, NFCPatternType type) {
      this.type = type;
      this.triggerable = null;
      this.patternName = patternName;
    }

    public NFCPattern(String patternName, NFCPatternType type, Triggerable triggerable) {
      this.type = type;
      this.triggerable = triggerable;
      this.patternName = patternName;
    }
  }

  // NFCTrigger - turn the specified pattern on or off on the specified channel
  public class NFCTrigger {
    public int channelIdx;
    public boolean onOff;
    public NFCPattern pattern;

    public NFCTrigger(int channelIdx, boolean onOff, NFCPattern pattern) {
      this.channelIdx = channelIdx;
      this.onOff = onOff;
      this.pattern = pattern;
    }
  }

  // NFCOneShot - for managing one shots
  public class NFCOneShot {
    public int timeout;
    public Triggerable triggerable;
    public boolean enabled;

    public NFCOneShot(Triggerable triggerable) {
      this.triggerable = triggerable;
      this.timeout = -1;
      this.enabled = false;
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
    this.attractModeEnable = Config.attractModeEnable;
    this.ATTRACT_MODE_DELTA_MS = Config.attractModeEnable ? Config.attractModeTimeout : 0;
    this.engineController = engineController;
    this.lx = lx;
    short port = Config.NFCPort != -1 ? Config.NFCPort : 7777;
    this.numActivities = Config.NFCNumActivities != -1 ? Config.NFCNumActivities : 4;
    this.activities = new NFCPattern[this.numActivities];
    this.patternMap = new HashMap<String, NFCPattern>();
    this.oneShotMap = new HashMap<String, NFCOneShot>();
    setupPatternMap();

    this.server = new TSServer(this, port);
    System.out.println("NFC Server Init, Attract Mode " + this.attractModeEnable + ", timeout " + this.ATTRACT_MODE_DELTA_MS);
  }

  private NFCPattern createOneShot(String name, Class clazz) {
    NFCPattern pattern = new NFCPattern(name, NFCPatternType.ONE_SHOT, engineController.setupPatternEffect(clazz));
    this.oneShotMap.put(name, new NFCOneShot(pattern.triggerable));
    return pattern;
  }

  private void setupPatternMap() {
    // Standard patterns are at known locations on the patterns channel
    this.patternMap.put("TwisterGlobal", new NFCPattern("TwisterGlobal", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("MarkLottor", new NFCPattern("MarkLottor", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("Ripple", new NFCPattern("Ripple",  NFCPatternType.BASE_PATTERN));
    this.patternMap.put("Lattice", new NFCPattern("Lattice", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("Voronoi", new NFCPattern("Voranoi", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("GalaxyCloud", new NFCPattern("GalaxyCloud", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("Fire", new NFCPattern("Fire", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("AcidTrip", new NFCPattern("AcidTrip", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("SparkleHelix", new NFCPattern("SparkleHelix",  NFCPatternType.BASE_PATTERN));
    this.patternMap.put("Cells", new NFCPattern("Cells", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("RingoDown", new NFCPattern("RingoDown", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("Explosions", new NFCPattern("Explosions", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("ColoredLeaves", new NFCPattern("ColoredLeaves", NFCPatternType.BASE_PATTERN));
    this.patternMap.put("Fumes", new NFCPattern("Fumes", NFCPatternType.BASE_PATTERN));
    // One shots
    this.patternMap.put("Lightning", createOneShot("Lightning", Lightning.class));
    this.patternMap.put("Wisps", createOneShot("Wisps", Wisps.class));
    this.patternMap.put("BassSlam", createOneShot("BassSlam", BassSlam.class));
    this.patternMap.put("Bubbles", createOneShot("Bubbles", Bubbles.class));
    this.patternMap.put("ColorStrobe", createOneShot("ColorStrobe", ColorStrobe.class));
    this.patternMap.put("Rain", createOneShot("Rain", Rain.class));
    // Globals
    this.patternMap.put("Static", new NFCPattern("static", NFCPatternType.GLOBAL_MODIFIER));
    this.patternMap.put("Blur", new NFCPattern("blur", NFCPatternType.GLOBAL_MODIFIER));
    this.patternMap.put("Speed", new NFCPattern("speed", NFCPatternType.GLOBAL_MODIFIER));
    this.patternMap.put("Scramble", new NFCPattern("scramble", NFCPatternType.GLOBAL_MODIFIER));

    // XXX - the parameters for the various effects should be set up in the project file...
    // XXX - make absolutely sure (again) that the effects in the effects channel can happen simultaneously
  }

  protected void handleOneShots() {
    int curTime = EntwinedUtils.millis();
    for (NFCOneShot oneShot : oneShotMap.values()) {
      if (oneShot.enabled && oneShot.timeout < curTime) {
        oneShot.triggerable.onReleased();
        System.out.println("Releasing one shot, timeout  " + oneShot.timeout + " cur time " + curTime);
        oneShot.enabled = false;
      }
    }
  }

  protected void triggerOneShot(String patternName) {
    NFCOneShot oneShot = this.oneShotMap.get(patternName);
    if (oneShot.enabled != true) {
      System.out.println("Triggering one shot " + patternName);
      oneShot.triggerable.onTriggered();  // XXX better understand what's going on here... XX
    }
    oneShot.enabled = true;
    oneShot.timeout = EntwinedUtils.millis() + ONE_SHOT_TIMEOUT_MS;
  }

  protected Boolean runningInteractive() {
    for (NFCPattern activity : this.activities) {
      if (activity != null) {
        return true;
      }
    }
    return false;
  }

  private void disableGlobalActivity(NFCPattern activity) {
    if (activity != null && activity.type == NFCPatternType.GLOBAL_MODIFIER) {
      if (activity.patternName == "speed") {
        engineController.setSpeed(1.0);
      }else if (activity.patternName == "static") {
        engineController.setStatic(0.0);
      } else if (activity.patternName == "blur") {
        engineController.setBlur(0.0);
      } else if (activity.patternName == "scramble") {
        engineController.setScramble(0.0);
      }
    }
  }

  public void loop(double deltaMs) {
    // Turn one shots off, if  they've timed out
    handleOneShots();

    if (runningInteractive()) {
      attractModeTimeout = EntwinedUtils.millis() + ATTRACT_MODE_DELTA_MS;
    }

    // Turn on attract mode, if we've hit the timeout
    if (attractModeEnable && attractModeTimeout < EntwinedUtils.millis()) {
      if (!autoPlaying) {
        System.out.println("Attract mode enable!!!");
        engineController.setAutoplay(true);
        autoPlaying = true;
      }
    }
    TSClient client = server.available(); // XXX  this needs not to block!
    if (client == null) return;

    String command = client.readStringUntil('\n');
    if (command == null) {
      return;
    }
    System.out.println("Have OSC command " + command);

    NFCTrigger trigger = parseNFCCommand(command);
    if (trigger == null) {
      System.out.println("could not find pattern for command " + command);
      return;
    }
    System.out.println("Trigger received, turn off autoplay");
    engineController.setAutoplay(false);
    autoPlaying = false;
    attractModeTimeout = EntwinedUtils.millis() + ATTRACT_MODE_DELTA_MS;
    System.out.println("Attract mode enable is " + this.attractModeEnable + ", timeout is " + this.attractModeTimeout + ", currentTime is " + EntwinedUtils.millis());
    disableGlobalActivity(this.activities[trigger.channelIdx]);
    if (!trigger.onOff) {  // Turn off
        this.activities[trigger.channelIdx] = null;
    } else {  // Turn on
      if (trigger.pattern.type == NFCPatternType.BASE_PATTERN) {
        this.activities[trigger.channelIdx] = trigger.pattern;
        int channelIdx = engineController.baseChannelIndex + trigger.channelIdx;
        engineController.setChannelPattern(
            channelIdx,
            trigger.pattern.patternName);
      } else if (trigger.pattern.type == NFCPatternType.ONE_SHOT) {
        // nb - turn on effects channel in case it got turned off by the
        // autoplay
        engineController.setChannelVisibility(engineController.baseChannelIndex + this.numActivities, 1.0);
        triggerOneShot(trigger.pattern.patternName);
      } else if (trigger.pattern.type == NFCPatternType.GLOBAL_MODIFIER) {
        this.activities[trigger.channelIdx] = trigger.pattern;
        if (trigger.pattern.patternName == "static") {
          engineController.setStatic(1.0);
        } else if (trigger.pattern.patternName == "blur") {
          engineController.setBlur(1.0); // XXX what is a good value here?
        } else if (trigger.pattern.patternName == "scramble") {
          engineController.setScramble(1.0);
        } else if (trigger.pattern.patternName == "speed1") {
          engineController.setSpeed(5.0);
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
    if (numBasePatterns == 2) {
      intensityModifier = 0.80f;
    } else if (numBasePatterns == 3) {
      intensityModifier = 0.66f;
    } else if (numBasePatterns >= 4) {
      intensityModifier = 0.50f;
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
    String patternName;
    // Format of the command is /channel/<channelid>/pattern/<patternName> ,s [T|F]
    try {
      int channelStarterIdx = command.indexOf("/channel/");
      if (channelStarterIdx < 0) {
        throw new RuntimeException("Could not find '/channel/'");
      }

      channelIdx = Character.getNumericValue(command.charAt(channelStarterIdx + 9));

      if (!command.substring(channelStarterIdx + 10,channelStarterIdx + 19).equals("/pattern/")) {
        throw new RuntimeException("Does not contain '/pattern/' at correct location");
      }

      int commaIdx = command.indexOf(",");
      if (commaIdx < 19) {
        throw new RuntimeException("Does not contain comma characters");
      }

      int enabledIdx = command.indexOf("/enable");
      patternName = command.substring(channelStarterIdx + 19, enabledIdx);

      String tfStr = command.substring(commaIdx);

      int isT = tfStr.indexOf('T');
      onOff = false;
      if (isT >= 0) {
          onOff = true;
      }

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
