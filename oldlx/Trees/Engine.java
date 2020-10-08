import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import heronarts.lx.LX;
import heronarts.lx.LXAutomationRecorder;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.transition.DissolveTransition;
import heronarts.lx.transition.LXTransition;

abstract class Engine {

  static final int NUM_CHANNELS = 8;
  static final int NUM_IPAD_CHANNELS = 3;
  static final int NUM_KNOBS = 8;
  static final int NUM_AUTOMATION = 4;

  final String projectPath;
  final List<CubeConfig> cubeConfig;
  final List<TreeConfig> treeConfigs;
  final LX lx;
  final Model model;
  EngineController engineController;
  LXDatagramOutput output;
  LXDatagram[] datagrams;
  BPMTool bpmTool;
  InterfaceController uiDeck;
  MidiEngine midiEngine;
  TSDrumpad apc40Drumpad;
  NFCEngine nfcEngine;
  LXListenableNormalizedParameter[] effectKnobParameters;
  final ChannelTreeLevels[] channelTreeLevels = new ChannelTreeLevels[NUM_CHANNELS];
  final BasicParameter dissolveTime = new BasicParameter("DSLV", 400, 50, 1000);
  final BasicParameter drumpadVelocity = new BasicParameter("DVEL", 1);
  final TSAutomationRecorder[] automation = new TSAutomationRecorder[Engine.NUM_AUTOMATION];
  final BooleanParameter[] automationStop = new BooleanParameter[Engine.NUM_AUTOMATION];
  final DiscreteParameter automationSlot = new DiscreteParameter("AUTO", Engine.NUM_AUTOMATION);
  final BooleanParameter[][] nfcToggles = new BooleanParameter[6][9];
  final BooleanParameter[] previewChannels = new BooleanParameter[Engine.NUM_CHANNELS];
  final BasicParameterProxy outputBrightness = new BasicParameterProxy(1);

  Engine(String projectPath) {
    this.projectPath = projectPath;

    cubeConfig = loadCubeConfigFile();
    treeConfigs = loadTreeConfigFile();
    model = new Model(treeConfigs, cubeConfig);

    lx = createLX();


    engineController = new EngineController(lx);

    lx.engine.addParameter(drumpadVelocity);

    for (int i=0; i<NUM_CHANNELS; i++){
      channelTreeLevels[i] = new ChannelTreeLevels(model.trees.size());
    }

    configureChannels();

    if (Config.enableNFC) {
      configureNFC();
      // this line to allow any nfc reader to read any cube
      nfcEngine.disableVisualTypeRestrictions = true;
    }

    configureTriggerables();
    lx.engine.addLoopTask(new ModelTransformTask(model));

    configureBMPTool();
    configureAutomation();

    if (Config.enableOutputBigtree) {
      lx.addEffect(new TurnOffDeadPixelsEffect(lx));
      configureExternalOutput();
    }
    if (Config.enableOutputMinitree) {
      configureFadeCandyOutput();
    }

    postCreateLX();

    if (Config.enableAPC40) {
      configureMIDI();
    }
    if (Config.enableIPad) {
      engineController.setAutoplay(Config.autoplayBMSet, true);
      configureServer();
    }

    // bad code I know
    // (shouldn't mess with engine internals)
    // maybe need a way to specify a deck shouldn't be focused?
    // essentially this lets us have extra decks for the drumpad
    // patterns without letting them be assigned to channels
    // -kf
    lx.engine.focusedChannel.setRange(Engine.NUM_CHANNELS);
  }

  void start() {
    lx.engine.start();
  }

  abstract LX createLX();

  void postCreateLX() {
  }

  void registerIPadPatterns() {
    registerPatternController("None", new NoPattern(lx));
    registerPatternController("Twister", new Twister(lx));
    registerPatternController("Lottor", new MarkLottor(lx));
    registerPatternController("Ripple", new Ripple(lx));
    registerPatternController("Stripes", new Stripes(lx));
    registerPatternController("Lattice", new Lattice(lx));
    registerPatternController("Fumes", new Fumes(lx));
    registerPatternController("Voronoi", new Voronoi(lx));
    registerPatternController("Candy Cloud", new CandyCloud(lx));
    // registerPatternController("Galaxy Cloud", new GalaxyCloud(lx));

    registerPatternController("Color Strobe", new ColorStrobe(lx));
    registerPatternController("Strobe", new Strobe(lx));
    registerPatternController("Sparkle Takeover", new SparkleTakeOver(lx));
    registerPatternController("Multi-Sine", new MultiSine(lx));
    registerPatternController("Seesaw", new SeeSaw(lx));
    registerPatternController("Cells", new Cells(lx));
    registerPatternController("Fade", new Fade(lx));

    registerPatternController("Ice Crystals", new IceCrystals(lx));
    registerPatternController("Fire", new Fire(lx));

    registerPatternController("Acid Trip", new AcidTrip(lx));
    registerPatternController("Rain", new Rain(lx));
    registerPatternController("Bass Slam", new BassSlam(lx));

    registerPatternController("Fireflies", new Fireflies(lx));
    registerPatternController("Bubbles", new Bubbles(lx));
    registerPatternController("Lightning", new Lightning(lx));
    registerPatternController("Wisps", new Wisps(lx));
    registerPatternController("Fireworks", new Explosions(lx));
  }

  void registerIPadEffects() {
    ColorEffect colorEffect = new ColorEffect2(lx);
    ColorStrobeTextureEffect colorStrobeTextureEffect = new ColorStrobeTextureEffect(lx);
    FadeTextureEffect fadeTextureEffect = new FadeTextureEffect(lx);
    AcidTripTextureEffect acidTripTextureEffect = new AcidTripTextureEffect(lx);
    CandyTextureEffect candyTextureEffect = new CandyTextureEffect(lx);
    CandyCloudTextureEffect candyCloudTextureEffect = new CandyCloudTextureEffect(lx);
    // GhostEffect ghostEffect = new GhostEffect(lx);
    // RotationEffect rotationEffect = new RotationEffect(lx);

    SpeedEffect speedEffect = engineController.speedEffect = new SpeedEffect(lx);
    SpinEffect spinEffect = engineController.spinEffect = new SpinEffect(lx);
    BlurEffect blurEffect = engineController.blurEffect = new TSBlurEffect2(lx);
    ScrambleEffect scrambleEffect = engineController.scrambleEffect = new ScrambleEffect(lx);
    // StaticEffect staticEffect = engineController.staticEffect = new StaticEffect(lx);

    lx.addEffect(blurEffect);
    lx.addEffect(colorEffect);
    // lx.addEffect(staticEffect);
    lx.addEffect(spinEffect);
    lx.addEffect(speedEffect);
    lx.addEffect(colorStrobeTextureEffect);
    lx.addEffect(fadeTextureEffect);
    // lx.addEffect(acidTripTextureEffect);
    lx.addEffect(candyTextureEffect);
    lx.addEffect(candyCloudTextureEffect);
    // lx.addEffect(ghostEffect);
    lx.addEffect(scrambleEffect);
    // lx.addEffect(rotationEffect);

    registerEffectController("Rainbow", candyCloudTextureEffect, candyCloudTextureEffect.amount);
    registerEffectController("Candy Chaos", candyTextureEffect, candyTextureEffect.amount);
    registerEffectController("Color Strobe", colorStrobeTextureEffect, colorStrobeTextureEffect.amount);
    registerEffectController("Fade", fadeTextureEffect, fadeTextureEffect.amount);
    registerEffectController("Monochrome", colorEffect, colorEffect.mono);
    registerEffectController("White", colorEffect, colorEffect.desaturation);
  }

  void addPatterns(ArrayList<LXPattern> patterns) {
    // Add patterns here.
    // The order here is the order it shows up in the patterns list
    // patterns.add(new SolidColor(lx));
    // patterns.add(new ClusterLineTest(lx));
    // patterns.add(new OrderTest(lx));
    patterns.add(new Twister(lx));
    patterns.add(new CandyCloud(lx));
    patterns.add(new MarkLottor(lx));
    patterns.add(new SolidColor(lx));
    // patterns.add(new DoubleHelix(lx));
    patterns.add(new SparkleHelix(lx));
    patterns.add(new Lightning(lx));
    patterns.add(new SparkleTakeOver(lx));
    patterns.add(new MultiSine(lx));
    patterns.add(new Ripple(lx));
    patterns.add(new SeeSaw(lx));
    patterns.add(new SweepPattern(lx));
    patterns.add(new IceCrystals(lx));
    patterns.add(new ColoredLeaves(lx));
    patterns.add(new Stripes(lx));
    patterns.add(new AcidTrip(lx));
    patterns.add(new Springs(lx));
    patterns.add(new Lattice(lx));
    patterns.add(new Fire(lx));
    patterns.add(new Fireflies(lx));
    patterns.add(new Fumes(lx));
    patterns.add(new Voronoi(lx));
    patterns.add(new Cells(lx));
    patterns.add(new Bubbles(lx));
    patterns.add(new Pulleys(lx));

    patterns.add(new Wisps(lx));
    patterns.add(new Explosions(lx));
    patterns.add(new BassSlam(lx));
    patterns.add(new Rain(lx));
    patterns.add(new Fade(lx));
    patterns.add(new Strobe(lx));
    patterns.add(new Twinkle(lx));
    patterns.add(new VerticalSweep(lx));
    patterns.add(new RandomColor(lx));
    patterns.add(new ColorStrobe(lx));
    patterns.add(new Pixels(lx));
    patterns.add(new Wedges(lx));
    patterns.add(new Parallax(lx));
  }

  LXPattern[] getPatternListForChannels() {
    ArrayList<LXPattern> patterns = new ArrayList<LXPattern>();
    addPatterns(patterns);
    for (LXPattern pattern : patterns) {
      LXTransition t = new DissolveTransition(lx).setDuration(dissolveTime);
      pattern.setTransition(t);
    }
    return patterns.toArray(new LXPattern[patterns.size()]);
  }

  void registerPatternTriggerables() {
    // The 2nd parameter is the NFC tag serial number
    // Specify a blank string to only add it to the apc40 drumpad
    // The 3rd parameter is which row of the apc40 drumpad to add it to.
    // defaults to the 3rd row
    // the row parameter is zero indexed

    registerPattern(new Twister(lx), "3707000050a8fb");
    registerPattern(new MarkLottor(lx), "3707000050a8d5");
    registerPattern(new Ripple(lx), "3707000050a908");
    registerPattern(new Stripes(lx), "3707000050a8ad");
    registerPattern(new Lattice(lx), "3707000050a8b9");
    registerPattern(new Fumes(lx), "3707000050a9b1");
    registerPattern(new Voronoi(lx), "3707000050a952");
    registerPattern(new CandyCloud(lx), "3707000050aab4");
    registerPattern(new GalaxyCloud(lx), "3707000050a91d");

    registerPattern(new ColorStrobe(lx), "3707000050a975", 3);
    registerPattern(new Explosions(lx, 20), "3707000050a8bf", 3);
    registerPattern(new Strobe(lx), "3707000050ab3a", 3);
    registerPattern(new SparkleTakeOver(lx), "3707000050ab68", 3);
    registerPattern(new MultiSine(lx), "3707000050ab38", 3);
    registerPattern(new SeeSaw(lx), "3707000050ab76", 3);
    registerPattern(new Cells(lx), "3707000050abca", 3);
    registerPattern(new Fade(lx), "3707000050a8b0", 3);
    registerPattern(new Pixels(lx), "3707000050ab38", 3);

    registerPattern(new IceCrystals(lx), "3707000050a89b", 5);
    registerPattern(new Fire(lx), "-", 5); // Make red

    // registerPattern(new DoubleHelix(lx), "");
    registerPattern(new AcidTrip(lx), "3707000050a914");
    registerPattern(new Rain(lx), "3707000050a937");

    registerPattern(new Wisps(lx, 1, 60, 50, 270, 20, 3.5, 10), "3707000050a905"); // downward yellow wisp
    registerPattern(new Wisps(lx, 30, 210, 100, 90, 20, 3.5, 10), "3707000050ab1a"); // colorful wisp storm
    registerPattern(new Wisps(lx, 1, 210, 100, 90, 130, 3.5, 10), "3707000050aba4"); // multidirection colorful wisps
    registerPattern(new Wisps(lx, 3, 210, 10, 270, 0, 3.5, 10), ""); // rain storm of wisps
    registerPattern(new Wisps(lx, 35, 210, 180, 180, 15, 2, 15), "3707000050a8ee"); // twister of wisps
  }

  void registerOneShotTriggerables() {
    registerOneShot(new Pulleys(lx), "3707000050a939");
    registerOneShot(new StrobeOneshot(lx), "3707000050abb0");
    registerOneShot(new BassSlam(lx), "3707000050a991");
    registerOneShot(new Fireflies(lx, 70, 6, 180), "3707000050ab2e");
    registerOneShot(new Fireflies(lx, 40, 7.5f, 90), "3707000050a92b");

    registerOneShot(new Fireflies(lx), "3707000050ab56", 5);
    registerOneShot(new Bubbles(lx), "3707000050a8ef", 5);
    registerOneShot(new Lightning(lx), "3707000050ab18", 5);
    registerOneShot(new Wisps(lx), "3707000050a9cd", 5);
    registerOneShot(new Explosions(lx), "3707000050ab6a", 5);
  }

  void registerEffectTriggerables() {
    BlurEffect blurEffect = new TSBlurEffect(lx);
    ColorEffect colorEffect = new ColorEffect(lx);
    GhostEffect ghostEffect = new GhostEffect(lx);
    ScrambleEffect scrambleEffect = new ScrambleEffect(lx);
    StaticEffect staticEffect = new StaticEffect(lx);
    RotationEffect rotationEffect = new RotationEffect(lx);
    SpinEffect spinEffect = new SpinEffect(lx);
    SpeedEffect speedEffect = new SpeedEffect(lx);
    ColorStrobeTextureEffect colorStrobeTextureEffect = new ColorStrobeTextureEffect(lx);
    FadeTextureEffect fadeTextureEffect = new FadeTextureEffect(lx);
    AcidTripTextureEffect acidTripTextureEffect = new AcidTripTextureEffect(lx);
    CandyTextureEffect candyTextureEffect = new CandyTextureEffect(lx);
    CandyCloudTextureEffect candyCloudTextureEffect = new CandyCloudTextureEffect(lx);

    lx.addEffect(blurEffect);
    lx.addEffect(colorEffect);
    lx.addEffect(ghostEffect);
    lx.addEffect(scrambleEffect);
    lx.addEffect(staticEffect);
    lx.addEffect(rotationEffect);
    lx.addEffect(spinEffect);
    lx.addEffect(speedEffect);
    lx.addEffect(colorStrobeTextureEffect);
    lx.addEffect(fadeTextureEffect);
    lx.addEffect(acidTripTextureEffect);
    lx.addEffect(candyTextureEffect);
    lx.addEffect(candyCloudTextureEffect);

    registerEffectControlParameter(speedEffect.speed, "3707000050abae", 1, 0.4);
    registerEffectControlParameter(speedEffect.speed, "3707000050a916", 1, 5);
    registerEffectControlParameter(colorEffect.rainbow, "3707000050a98f");
    registerEffectControlParameter(colorEffect.mono, "3707000050aafe");
    registerEffectControlParameter(colorEffect.desaturation, "3707000050a969");
    registerEffectControlParameter(colorEffect.sharp, "3707000050aafc");
    registerEffectControlParameter(blurEffect.amount, "3707000050a973", 0.65);
    registerEffectControlParameter(spinEffect.spin, "3707000050ab2c", 0.65);
    registerEffectControlParameter(ghostEffect.amount, "3707000050aaf2", 0, 0.16, 1);
    registerEffectControlParameter(scrambleEffect.amount, "3707000050a8cc", 0, 1, 1);
    registerEffectControlParameter(colorStrobeTextureEffect.amount, "3707000050a946", 0, 1, 1);
    registerEffectControlParameter(fadeTextureEffect.amount, "3707000050a967", 0, 1, 1);
    registerEffectControlParameter(acidTripTextureEffect.amount, "3707000050a953", 0, 1, 1);
    registerEffectControlParameter(candyCloudTextureEffect.amount, "3707000050a92d", 0, 1, 1);
    registerEffectControlParameter(staticEffect.amount, "3707000050a8b3", 0, .3, 1);
    registerEffectControlParameter(candyTextureEffect.amount, "3707000050aafc", 0, 1, 5);

    effectKnobParameters = new LXListenableNormalizedParameter[]{
        colorEffect.hueShift,
        colorEffect.mono,
        colorEffect.desaturation,
        colorEffect.sharp,
        blurEffect.amount,
        speedEffect.speed,
        spinEffect.spin,
        candyCloudTextureEffect.amount
    };
  }

  VisualType[] readerPatternTypeRestrictions() {
    return new VisualType[]{
        VisualType.Pattern,
        VisualType.Pattern,
        VisualType.Pattern,
        VisualType.OneShot,
        VisualType.OneShot,
        VisualType.OneShot,
        VisualType.Effect,
        VisualType.Effect,
        VisualType.Effect,
        VisualType.Pattern,
    };
  }

  String sketchPath(String filename) {
    return projectPath + "/" + filename;
  }

  List<CubeConfig> loadCubeConfigFile() {
    return loadJSONFile(Config.CUBE_CONFIG_FILE, new TypeToken<List<CubeConfig>>() {
    }.getType());
  }
  List<TreeConfig> loadTreeConfigFile() {
    return loadJSONFile(Config.TREE_CONFIG_FILE, new TypeToken<List<TreeConfig>>() {
    }.getType());
  }

  JsonArray loadSavedSetFile(String filename) {
    return loadJSONFile(filename, JsonArray.class);
  }

  <T> T loadJSONFile(String filename, Type typeToken) {
    Reader reader = null;
    try {
      reader = new BufferedReader(new FileReader(sketchPath(filename)));
      return new Gson().fromJson(reader, typeToken);
    } catch (IOException ioe) {
      System.out.println("Error reading json file: ");
      System.out.println(ioe);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ioe) {
        }
      }
    }
    return null;
  }

  void saveCubeConfigs(){
    List<CubeConfig> cubeConfigs = new ArrayList();
    for (Cube cube: model.cubes){
      if (cube.config.isActive){
        cubeConfigs.add(cube.config);
      }
    }
    String data = new Gson().toJson(cubeConfigs);
    saveJSONToFile(data, Config.CUBE_CONFIG_FILE);
  }

  void saveJSONToFile(String data, String filename) {
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new BufferedWriter(new FileWriter(sketchPath(filename))));
      writer.write(data);
    } catch (IOException ioe) {
      System.out.println("Error writing json file.");
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  /* configureChannels */

  void setupChannel(final LXChannel channel, boolean noOpWhenNotRunning) {
    channel.setFaderTransition(new TreesTransition(lx, channel, model, channelTreeLevels));
    channel.addListener(new LXChannel.AbstractListener() {
      LXTransition transition;

      public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
        if (!channel.enabled.isOn()) {
          transition = nextPattern.getTransition();
          nextPattern.setTransition(null);
        }
      }

      public void patternDidChange(LXChannel channel, LXPattern pattern) {
        if (transition != null) {
          pattern.setTransition(transition);
          transition = null;
        }
      }
    });

    if (noOpWhenNotRunning) {
      channel.enabled.setValue(channel.getFader().getValue() != 0);
      channel.getFader().addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter parameter) {
          channel.enabled.setValue(channel.getFader().getValue() != 0);
        }
      });
    }
  }

  ArrayList<TSPattern> patterns;

  void configureChannels() {
    for (int i = 0; i < Engine.NUM_CHANNELS; ++i) {
      LXChannel channel = lx.engine.addChannel(getPatternListForChannels());
      setupChannel(channel, true);
      if (i == 0) {
        channel.getFader().setValue(1);
      }
      channel.goIndex(i);
    }
    engineController.baseChannelIndex = lx.engine.getChannels().size() - 1;

    if (Config.enableIPad) {
      for (int i = 0; i < Engine.NUM_IPAD_CHANNELS; ++i) {
        patterns = new ArrayList<TSPattern>();
        registerIPadPatterns();

        LXChannel channel = lx.engine.addChannel(patterns.toArray(new TSPattern[0]));
        setupChannel(channel, true);
        channel.getFader().setValue(1);

        if (i == 0) {
          channel.goIndex(1);
        }
      }
      patterns = null;
      engineController.numChannels = NUM_IPAD_CHANNELS;
    }

    lx.engine.removeChannel(lx.engine.getDefaultChannel());
  }

  void registerOneShot(TSPattern pattern, String nfcSerialNumber) {
    registerOneShot(pattern, nfcSerialNumber, 4);
  }

  void registerOneShot(TSPattern pattern, String nfcSerialNumber, int apc40DrumpadRow) {
    registerVisual(pattern, nfcSerialNumber, apc40DrumpadRow, VisualType.OneShot);
  }

  void registerPattern(TSPattern pattern, String nfcSerialNumber) {
    registerPattern(pattern, nfcSerialNumber, 2);
  }

  void registerPattern(TSPattern pattern, String nfcSerialNumber, int apc40DrumpadRow) {
    registerVisual(pattern, nfcSerialNumber, apc40DrumpadRow, VisualType.Pattern);
  }

  void registerVisual(TSPattern pattern, String nfcSerialNumber, int apc40DrumpadRow, VisualType visualType) {
    LXTransition t = new DissolveTransition(lx).setDuration(dissolveTime);
    pattern.setTransition(t);

    Triggerable triggerable = configurePatternAsTriggerable(pattern);
    BooleanParameter toggle = null;
    if (apc40Drumpad != null) {
      toggle = apc40DrumpadTriggerablesLists[apc40DrumpadRow].size() < 9 ? nfcToggles[apc40DrumpadRow][apc40DrumpadTriggerablesLists[apc40DrumpadRow].size()] : null;
      apc40DrumpadTriggerablesLists[apc40DrumpadRow].add(triggerable);
    }
    if (nfcEngine != null) {
      nfcEngine.registerTriggerable(nfcSerialNumber, triggerable, visualType, toggle);
    }
  }

  Triggerable configurePatternAsTriggerable(TSPattern pattern) {
    LXChannel channel = lx.engine.addChannel(new TSPattern[]{pattern});
    setupChannel(channel, false);

    pattern.onTriggerableModeEnabled();
    return pattern.getTriggerable();
  }

  void registerPatternController(String name, TSPattern pattern) {
    LXTransition t = new DissolveTransition(lx).setDuration(dissolveTime);
    pattern.setTransition(t);
    pattern.readableName = name;
    patterns.add(pattern);
  }

  /* configureEffects */

  void registerEffect(LXEffect effect, String nfcSerialNumber) {
    if (effect instanceof Triggerable) {
      Triggerable triggerable = (Triggerable) effect;
      BooleanParameter toggle = null;
      if (apc40Drumpad != null) {
        toggle = apc40DrumpadTriggerablesLists[0].size() < 9 ? nfcToggles[0][apc40DrumpadTriggerablesLists[0].size()] : null;
        apc40DrumpadTriggerablesLists[0].add(triggerable);
      }
      if (nfcEngine != null) {
        nfcEngine.registerTriggerable(nfcSerialNumber, triggerable, VisualType.Effect, toggle);
      }
    }
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter, String nfcSerialNumber) {
    registerEffectControlParameter(parameter, nfcSerialNumber, 0, 1, 0);
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter, String nfcSerialNumber, double onValue) {
    registerEffectControlParameter(parameter, nfcSerialNumber, 0, onValue, 0);
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter, String nfcSerialNumber, double offValue, double onValue) {
    registerEffectControlParameter(parameter, nfcSerialNumber, offValue, onValue, 0);
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter, String nfcSerialNumber, double offValue, double onValue, int row) {
    ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter, offValue, onValue);
    BooleanParameter toggle = null;
    if (apc40Drumpad != null) {
      toggle = apc40DrumpadTriggerablesLists[row].size() < 9 ? nfcToggles[row][apc40DrumpadTriggerablesLists[row].size()] : null;
      apc40DrumpadTriggerablesLists[row].add(triggerable);
    }
    if (nfcEngine != null) {
      nfcEngine.registerTriggerable(nfcSerialNumber, triggerable, VisualType.Effect, toggle);
    }
  }

  void registerEffectController(String name, LXEffect effect, LXListenableNormalizedParameter parameter) {
    ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter);
    TSEffectController effectController = new TSEffectController(name, effect, triggerable);

    engineController.effectControllers.add(effectController);
  }

  /* configureBMPTool */

  void configureBMPTool() {
    bpmTool = new BPMTool(lx, effectKnobParameters);
  }

  /* configureAutomation */

  void configureAutomation() {
    // Example automation message to change master fader
    // {
    //   "message": "master/0.5",
    //   "event": "MESSAGE",
    //   "millis": 0
    // },
    lx.engine.addMessageListener(new LXEngine.MessageListener() {
      public void onMessage(LXEngine engine, String message) {
        if (message.length() > 8 && message.substring(0, 7).equals("master/")) {
          double value = Double.parseDouble(message.substring(7));
          outputBrightness.setValue(value);
        }
      }
    });

    // Automation recorders
    for (int i = 0; i < automation.length; ++i) {
      final int ii = i;
      automation[i] = new TSAutomationRecorder(lx.engine);
      lx.engine.addLoopTask(automation[i]);
      automationStop[i] = new BooleanParameter("STOP", false);
      automationStop[i].addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter parameter) {
          if (parameter.getValue() > 0) {
            automation[ii].reset();
            automation[ii].armRecord.setValue(false);
          }
        }
      });
    }

    String filename = "data/Burning Man Playlist.json";
    JsonArray jsonArr = loadSavedSetFile(filename);
    automation[automationSlot.getValuei()].loadJson(jsonArr);
    // slotLabel.setLabel(labels[automationSlot.getValuei()] = filename);
    automation[automationSlot.getValuei()].looping.setValue(true);
    engineController.automation = automation[automationSlot.getValuei()];

    if (Config.autoplayBMSet) {
      automation[automationSlot.getValuei()].start();
    }
  }

  /* configureTriggerables */

  ArrayList<Triggerable>[] apc40DrumpadTriggerablesLists;
  Triggerable[][] apc40DrumpadTriggerables;

  @SuppressWarnings("unchecked")
  void configureTriggerables() {
    if (apc40Drumpad != null) {
      apc40DrumpadTriggerablesLists = new ArrayList[]{
          new ArrayList<Triggerable>(),
          new ArrayList<Triggerable>(),
          new ArrayList<Triggerable>(),
          new ArrayList<Triggerable>(),
          new ArrayList<Triggerable>(),
          new ArrayList<Triggerable>()
      };
    }

    registerPatternTriggerables();
    registerOneShotTriggerables();
    registerEffectTriggerables();

    if (Config.enableIPad) {
      engineController.startEffectIndex = lx.engine.getEffects().size();
      registerIPadEffects();
      engineController.endEffectIndex = lx.engine.getEffects().size();
    }

    if (apc40Drumpad != null) {
      apc40DrumpadTriggerables = new Triggerable[apc40DrumpadTriggerablesLists.length][];
      for (int i = 0; i < apc40DrumpadTriggerablesLists.length; i++) {
        ArrayList<Triggerable> triggerablesList = apc40DrumpadTriggerablesLists[i];
        apc40DrumpadTriggerables[i] = triggerablesList.toArray(new Triggerable[triggerablesList.size()]);
      }
      apc40DrumpadTriggerablesLists = null;
    }
  }

  /* configureMIDI */

  void configureMIDI() {
    apc40Drumpad = new TSDrumpad();
    apc40Drumpad.triggerables = apc40DrumpadTriggerables;

    // MIDI control
    midiEngine = new MidiEngine(lx, effectKnobParameters, apc40Drumpad, drumpadVelocity, previewChannels, bpmTool, uiDeck, nfcToggles, outputBrightness, automationSlot, automation, automationStop);
  }

  /* configureNFC */

  void configureNFC() {
    nfcEngine = new NFCEngine(lx);
    nfcEngine.start();

    for (int i = 0; i < 6; i++) {
      for (int j = 0; j < 9; j++) {
        nfcToggles[i][j] = new BooleanParameter("toggle");
      }
    }

    nfcEngine.registerReaderPatternTypeRestrictions(Arrays.asList(readerPatternTypeRestrictions()));
  }

  /* configureExternalOutput */

  void configureExternalOutput() {
    // Output stage
    try {
      output = new LXDatagramOutput(lx);
      datagrams = new LXDatagram[model.ipMap.size()];
      int ci = 0;
      for (Map.Entry<String, Cube[]> entry : model.ipMap.entrySet()) {
        String ip = entry.getKey();
        Cube[] cubes = entry.getValue();
        output.addDatagram(datagrams[ci++] = Output.clusterDatagram(cubes).setAddress(ip));
      }
      outputBrightness.parameters.add(output.brightness);
      output.enabled.setValue(true);
      lx.addOutput(output);
    } catch (Exception x) {
      System.out.println(x);
    }
  }

  /* configureFadeCandyOutput */

  void configureFadeCandyOutput() {
    int[] clusterOrdering = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    int numCubesInCluster = clusterOrdering.length;
    int numClusters = 48;
    int[] pixelOrder = new int[numClusters * numCubesInCluster];
    for (int cluster = 0; cluster < numClusters; cluster++) {
      for (int cube = 0; cube < numCubesInCluster; cube++) {
        pixelOrder[cluster * numCubesInCluster + cube] = cluster * numCubesInCluster + clusterOrdering[cube];
      }
    }
    try {
      FadecandyOutput fadecandyOutput = new FadecandyOutput(lx, "127.0.0.1", 7890, pixelOrder);
      outputBrightness.parameters.add(fadecandyOutput.brightness);
      lx.addOutput(fadecandyOutput);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  /* configureServer */

  void configureServer() {
    new AppServer(lx, engineController).start();
  }
}

class EngineController {
  LX lx;

  int baseChannelIndex;
  int numChannels;

  int startEffectIndex;
  int endEffectIndex;

  boolean isAutoplaying;
  TSAutomationRecorder automation;
  boolean[] previousChannelIsOn;

  ArrayList<TSEffectController> effectControllers = new ArrayList<TSEffectController>();
  int activeEffectControllerIndex = -1;

  SpeedEffect speedEffect;
  SpinEffect spinEffect;
  BlurEffect blurEffect;
  ScrambleEffect scrambleEffect;

  EngineController(LX lx) {
    this.lx = lx;
  }

  List<LXChannel> getChannels() {
    return lx.engine.getChannels().subList(baseChannelIndex, baseChannelIndex + numChannels);
  }

  void setChannelPattern(int channelIndex, int patternIndex) {
    if (patternIndex == -1) {
      patternIndex = 0;
    } else {
      patternIndex++;
    }
    lx.engine.getChannel(channelIndex).goIndex(patternIndex);
  }

  void setChannelVisibility(int channelIndex, double visibility) {
    lx.engine.getChannel(channelIndex).getFader().setValue(visibility);
  }

  void setActiveColorEffect(int effectIndex) {
    if (activeEffectControllerIndex == effectIndex) {
      return;
    }
    if (activeEffectControllerIndex != -1) {
      TSEffectController effectController = effectControllers.get(activeEffectControllerIndex);
      effectController.setEnabled(false);
    }
    activeEffectControllerIndex = effectIndex;
    if (activeEffectControllerIndex != -1) {
      TSEffectController effectController = effectControllers.get(activeEffectControllerIndex);
      effectController.setEnabled(true);
    }
  }

  void setSpeed(double amount) {
    speedEffect.speed.setValue(amount);
  }

  void setSpin(double amount) {
    spinEffect.spin.setValue(amount);
  }

  void setBlur(double amount) {
    blurEffect.amount.setValue(amount);
  }

  void setScramble(double amount) {
    scrambleEffect.amount.setValue(amount);
  }

  void setAutoplay(boolean autoplay) {
    setAutoplay(autoplay, false);
  }

  void setAutoplay(boolean autoplay, boolean forceUpdate) {
    if (autoplay != isAutoplaying || forceUpdate) {
      isAutoplaying = autoplay;
      automation.setPaused(!autoplay);

      if (previousChannelIsOn == null) {
        previousChannelIsOn = new boolean[lx.engine.getChannels().size()];
        for (LXChannel channel : lx.engine.getChannels()) {
          previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
        }
      }

      for (LXChannel channel : lx.engine.getChannels()) {
        boolean toEnable;
        if (channel.getIndex() < baseChannelIndex) {
          toEnable = autoplay;
        } else if (channel.getIndex() < baseChannelIndex + numChannels) {
          toEnable = !autoplay;
        } else {
          toEnable = autoplay;
        }

        if (toEnable) {
          channel.enabled.setValue(previousChannelIsOn[channel.getIndex()]);
        } else {
          previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
          channel.enabled.setValue(false);
        }
      }

      for (int i = 0; i < lx.engine.getEffects().size(); i++) {
        LXEffect effect = lx.engine.getEffects().get(i);
        if (i < startEffectIndex) {
          effect.enabled.setValue(autoplay);
        } else if (i < endEffectIndex) {
          effect.enabled.setValue(!autoplay);
        }
      }
    }
  }
}

class TreesTransition extends LXTransition {

  private final LXChannel channel;
  private final Model model;
  public final DiscreteParameter blendMode = new DiscreteParameter("MODE", 4);
  private LXColor.Blend blendType = LXColor.Blend.ADD;
  final ChannelTreeLevels[] channelTreeLevels;
  final BasicParameter fade = new BasicParameter("FADE", 1);

  TreesTransition(LX lx, LXChannel channel, Model model, ChannelTreeLevels[] channelTreeLevels) {
    super(lx);
    this.model = model;
    addParameter(blendMode);
    this.channel = channel;
    this.channelTreeLevels = channelTreeLevels;
    blendMode.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        switch (blendMode.getValuei()) {
          case 0:
            blendType = LXColor.Blend.ADD;
            break;
          case 1:
            blendType = LXColor.Blend.MULTIPLY;
            break;
          case 2:
            blendType = LXColor.Blend.LIGHTEST;
            break;
          case 3:
            blendType = LXColor.Blend.SUBTRACT;
            break;
        }
      }
    });
  }

  protected void computeBlend(int[] c1, int[] c2, double progress) {
    int treeIndex = 0;
    double treeLevel;
    for (Tree tree : model.trees) {
      treeLevel = this.channelTreeLevels[this.channel.getIndex()].getValue(treeIndex);
      float amount = (float) (progress * treeLevel);
      if (amount == 0) {
        for (LXPoint p : tree.points) {
          colors[p.index] = c1[p.index];
        }
      } else if (amount == 1) {
        for (LXPoint p : tree.points) {
          int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
          colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
        }
      } else {
        for (LXPoint p : tree.points) {
          int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
          colors[p.index] = LXColor.lerp(c1[p.index], LXColor.blend(c1[p.index], color2, this.blendType), amount);
        }
      }
      treeIndex++;
    }
  }
}

class ChannelTreeLevels{
  private BasicParameter[] levels;
  ChannelTreeLevels(int numTrees){
    levels = new BasicParameter[numTrees];
    for (int i=0; i<numTrees; i++){
      this.levels[i] = new BasicParameter("tree" + i, 1);
    }
  }
  public BasicParameter getParameter(int i){
    return this.levels[i];
  }
  public double getValue(int i){
    return this.levels[i].getValue();
  }
}

class TSAutomationRecorder extends LXAutomationRecorder {

  boolean isPaused;

  TSAutomationRecorder(LXEngine engine) {
    super(engine);
  }

  @Override
  protected void onStart() {
    super.onStart();
    isPaused = false;
  }

  public void setPaused(boolean paused) {
    if (!paused && !isRunning()) {
      start();
    }
    isPaused = paused;
  }

  @Override
  public void loop(double deltaMs) {
    if (!isPaused) {
      super.loop(deltaMs);
    }
  }
}

class BooleanParameterProxy extends BooleanParameter {

  final List<BooleanParameter> parameters = new ArrayList<BooleanParameter>();

  BooleanParameterProxy() {
    super("Proxy", true);
  }

  protected double updateValue(double value) {
    for (BooleanParameter parameter : parameters) {
      parameter.setValue(value);
    }
    return value;
  }
}

class BasicParameterProxy extends BasicParameter {

  final List<BasicParameter> parameters = new ArrayList<BasicParameter>();

  BasicParameterProxy(double value) {
    super("Proxy", value);
  }

  protected double updateValue(double value) {
    for (BasicParameter parameter : parameters) {
      parameter.setValue(value);
    }
    return value;
  }
}
