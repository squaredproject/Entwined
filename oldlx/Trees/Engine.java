import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import heronarts.lx.LX;
import heronarts.lx.LXAutomationRecorder;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.LXLoopTask;
import heronarts.lx.color.LXColor;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXPoint;
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

  static final int NUM_BASE_CHANNELS = 8;
  static final int NUM_SERVER_CHANNELS = 3;
  static final int NUM_TOTAL_CHANNELS = NUM_BASE_CHANNELS + NUM_SERVER_CHANNELS;
  static final int NUM_KNOBS = 8;
  static final int NUM_AUTOMATION = 4;

  final String projectPath;
  final List<NDBConfig> ndbConfig; // note: just for the trees, they're special
  final List<TreeCubeConfig> cubeConfig;
  final List<TreeConfig> treeConfigs;
  final List<ShrubCubeConfig> shrubCubeConfig;
  final List<ShrubConfig> shrubConfigs;
  final LX lx;
  final Model model;
  EngineController engineController;
  LXDatagramOutput treeOutput;
  LXDatagram[] treeDatagrams;
  LXDatagramOutput shrubOutput;
  LXDatagram[] shrubDatagrams;
  BPMTool bpmTool;
  InterfaceController uiDeck;
  MidiEngine midiEngine;
  TSDrumpad apc40Drumpad;

  ZoneId localZone = ZoneId.of("America/Los_Angeles");

  LXListenableNormalizedParameter[] effectKnobParameters;
  final ChannelTreeLevels[] channelTreeLevels = new ChannelTreeLevels[Engine.NUM_TOTAL_CHANNELS];
  final ChannelShrubLevels[] channelShrubLevels = new ChannelShrubLevels[Engine.NUM_TOTAL_CHANNELS];
  final BasicParameter dissolveTime = new BasicParameter("DSLV", 400, 50, 1000);
  final BasicParameter drumpadVelocity = new BasicParameter("DVEL", 1);
  
  final TSAutomationRecorder[] automation = new TSAutomationRecorder[Engine.NUM_AUTOMATION];
  final BooleanParameter[] automationStop = new BooleanParameter[Engine.NUM_AUTOMATION];
  final DiscreteParameter automationSlot = new DiscreteParameter("AUTO", Engine.NUM_AUTOMATION);
  final BooleanParameter[] previewChannels = new BooleanParameter[Engine.NUM_BASE_CHANNELS];

  final BasicParameterProxy outputBrightness = new BasicParameterProxy(1);
  final BrightnessScaleEffect masterBrightnessEffect;

  // breadcrumb regarding channelTreeLevels and channelShrubLevels
  // these are controllers which should be used on a shrub-by-shrub basis to allow
  // setting the overall output. There _were_ UI elements for this, but I'm taking them
  // out in this checkin because there are too many to be shown. However, at least
  // for now, I'm leaving the Levels parameters, so we can write code to control
  // output that way, someday. It would be better to collapse this code into a per-output
  // slider, but there's a lot about Trees and Shrubs that could be made more common.
  // maybe another day.

  Engine(String projectPath) {
    this.projectPath = projectPath;

    ndbConfig = loadNDBConfigFile();
    cubeConfig = loadCubeConfigFile();
    treeConfigs = loadTreeConfigFile();
    shrubCubeConfig = loadShrubCubeConfigFile();
    shrubConfigs = loadShrubConfigFile();
    model = new Model(ndbConfig, treeConfigs, cubeConfig, shrubConfigs, shrubCubeConfig);


    lx = createLX();

    // log that we are trying to start, even without a log
    System.out.println( " Starting Entwined: " + 
      ZonedDateTime.now( localZone ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME )
    );

    lx.engine.addLoopTask(new FrameRateLogTask(lx.engine) );



    // this is the TCP channel
    engineController = new EngineController(lx);
    masterBrightnessEffect = new BrightnessScaleEffect(lx);

    lx.engine.addParameter(drumpadVelocity);

    for (int i=0; i<NUM_TOTAL_CHANNELS; i++){
      channelTreeLevels[i] = new ChannelTreeLevels(model.trees.size());
      channelShrubLevels[i] = new ChannelShrubLevels(model.shrubs.size());
    }

    configureChannels();


    configureTriggerables();
    lx.engine.addLoopTask(new ModelTransformTask(model));

    configureBMPTool();
    configureAutomation();


    // last

    if (Config.enableOutputBigtree) {
      lx.addEffect(new TurnOffDeadPixelsEffect(lx));
      configureExternalOutput();
    }
    if (Config.enableOutputMinitree) {
      configureFadeCandyOutput();
    }

    lx.addEffect(masterBrightnessEffect);

    // last

    enableEffects();

    postCreateLX();

    if (Config.enableAPC40) {
      configureMIDI();
    }

  	// see below for what this confusing function really means
  	if (Config.autoplayBMSet) {
  		engineController.setAutoplay(Config.autoplayBMSet, true/*force*/);
  	}
  	configureServer(); // turns on the TCP listener



    // bad code I know
    // (shouldn't mess with engine internals)
    // maybe need a way to specify a deck shouldn't be focused?
    // essentially this lets us have extra decks for the drumpad
    // patterns without letting them be assigned to channels
    // -kf
    lx.engine.focusedChannel.setRange(Engine.NUM_BASE_CHANNELS);
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
    registerPatternController("TwisterGlobal", new TwisterGlobal(lx));
    registerPatternController("Candy Cloud", new CandyCloud(lx));
    registerPatternController("BeachBall", new BeachBall(lx));
    registerPatternController("Breath", new Breath(lx));

    registerPatternController("Lottor", new MarkLottor(lx));
    registerPatternController("Ripple", new Ripple(lx));
    registerPatternController("Stripes", new Stripes(lx));
    registerPatternController("Lattice", new Lattice(lx));

    registerPatternController("Voronoi", new Voronoi(lx));
    registerPatternController("Parallax", new Parallax(lx));
    registerPatternController("Burst", new Burst(lx));

    registerPatternController("Peppermint", new Peppermint(lx));

    registerPatternController("Ice Crystals", new IceCrystals(lx));
    registerPatternController("Fire", new Fire(lx));
    registerPatternController("Acid Trip", new AcidTrip(lx));
    registerPatternController("Rain", new Rain(lx));

    registerPatternController("Pond", new Pond(lx));
    registerPatternController("Planes", new Planes(lx));
    registerPatternController("Growth", new Growth(lx));

    registerPatternController("Lightning", new Lightning(lx));
    registerPatternController("Sparkle Takeover", new SparkleTakeOver(lx));
    registerPatternController("SparkleHelix", new SparkleHelix(lx));

    registerPatternController("Multi-Sine", new MultiSine(lx));
    registerPatternController("Seesaw", new SeeSaw(lx));
    registerPatternController("Ripple", new MultiSine(lx));
    registerPatternController("Cells", new Cells(lx));
    registerPatternController("Fade", new Fade(lx));
    registerPatternController("Springs", new Springs(lx));

    registerPatternController("Bass Slam", new BassSlam(lx));

    registerPatternController("Fireflies", new Fireflies(lx));
    registerPatternController("Bubbles", new Bubbles(lx));

    registerPatternController("Wisps", new Wisps(lx));
    registerPatternController("Fireworks", new Explosions(lx));

    registerPatternController("ColorWave", new ColorWave(lx));
    registerPatternController("Wedges", new Wedges(lx));

    // Misko's patterns
    registerPatternController("Circles", new Circles(lx));
    registerPatternController("LineScan", new LineScan(lx));
    registerPatternController("WaveScan", new WaveScan(lx));
    //registerPatternController("Stringy", new Stringy(lx));
    registerPatternController("RainbowWaveScan", new RainbowWaveScan(lx));
    registerPatternController("SyncSpinner", new SyncSpinner(lx));
    registerPatternController("LightHouse", new LightHouse(lx));
    registerPatternController("ShrubRiver", new ShrubRiver(lx));
    registerPatternController("ColorBlast", new ColorBlast(lx));
    registerPatternController("Vertigo", new Vertigo(lx));

    registerPatternController("Fumes", new Fumes(lx));
    registerPatternController("Color Strobe", new ColorStrobe(lx));
    registerPatternController("Strobe", new Strobe(lx));

  }

  void registerIPadEffects() {
    ColorEffect colorEffect = new ColorEffect(lx);
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

    engineController.masterBrightnessEffect = masterBrightnessEffect;
    engineController.outputBrightness = outputBrightness;


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
    patterns.add(new Twister(lx));
    patterns.add(new TwisterGlobal(lx));
    patterns.add(new CandyCloud(lx));
    patterns.add(new MarkLottor(lx));
    patterns.add(new SolidColor(lx));

    // Colin Hunt Patterns
    patterns.add(new ColorWave(lx));
    patterns.add(new BeachBall(lx));
    patterns.add(new Breath(lx));
    patterns.add(new Peppermint(lx));
    patterns.add(new ChristmasTree(lx));
    patterns.add(new Wreathes(lx));

    // Grant Patterson Patterns
    patterns.add(new Pond(lx));
    patterns.add(new Planes(lx));
    patterns.add(new Growth(lx));

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

    // Charlie Stigler Patterns
    patterns.add(new Burst(lx));

    //Miskos - worried, removing, sorry
    //patterns.add(new Stringy(lx));  // takes too much memory
    patterns.add(new Circles(lx));
    patterns.add(new LineScan(lx));
    patterns.add(new WaveScan(lx));
    patterns.add(new RainbowWaveScan(lx));
    patterns.add(new SyncSpinner(lx));
    patterns.add(new LightHouse(lx));
    patterns.add(new ShrubRiver(lx));
    patterns.add(new ColorBlast(lx));
    patterns.add(new Vertigo(lx));

    // Test patterns
    patterns.add(new ClusterLineTest(lx));
    patterns.add(new TestShrubSweep(lx));
    patterns.add(new TestShrubLayers(lx));
    //patterns.add(new OrderTest(lx));

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

    registerPattern(new Twister(lx));
    registerPattern(new TwisterGlobal(lx));
    registerPattern(new MarkLottor(lx));
    registerPattern(new Ripple(lx));
    registerPattern(new Stripes(lx));
    registerPattern(new Lattice(lx));
    registerPattern(new Fumes(lx));
    registerPattern(new Voronoi(lx));
    registerPattern(new CandyCloud(lx));
    registerPattern(new GalaxyCloud(lx));

    registerPattern(new ColorStrobe(lx), 3);
    registerPattern(new Explosions(lx, 20), 3);
    registerPattern(new Strobe(lx), 3);
    registerPattern(new SparkleTakeOver(lx), 3);
    registerPattern(new MultiSine(lx), 3);
    registerPattern(new SeeSaw(lx), 3);
    registerPattern(new Cells(lx), 3);
    registerPattern(new Fade(lx), 3);
    registerPattern(new Pixels(lx), 3);

    registerPattern(new IceCrystals(lx), 5);
    registerPattern(new Fire(lx), 5); // Make red

    // registerPattern(new DoubleHelix(lx), "");
    registerPattern(new AcidTrip(lx));
    registerPattern(new Rain(lx));

    registerPattern(new Wisps(lx, 1, 60, 50, 270, 20, 3.5, 10)); // downward yellow wisp
    registerPattern(new Wisps(lx, 30, 210, 100, 90, 20, 3.5, 10)); // colorful wisp storm
    registerPattern(new Wisps(lx, 1, 210, 100, 90, 130, 3.5, 10)); // multidirection colorful wisps
    registerPattern(new Wisps(lx, 3, 210, 10, 270, 0, 3.5, 10)); // rain storm of wisps
    registerPattern(new Wisps(lx, 35, 210, 180, 180, 15, 2, 15)); // twister of wisps

    registerPattern(new Pond(lx));
    registerPattern(new Planes(lx));
    registerPattern(new Growth(lx));
  }

  void registerOneShotTriggerables() {
    registerOneShot(new Pulleys(lx));
    registerOneShot(new StrobeOneshot(lx));
    registerOneShot(new BassSlam(lx));
    registerOneShot(new Fireflies(lx, 70, 6, 180));
    registerOneShot(new Fireflies(lx, 40, 7.5f, 90));

    registerOneShot(new Fireflies(lx), 5);
    registerOneShot(new Bubbles(lx), 5);
    registerOneShot(new Lightning(lx), 5);
    registerOneShot(new Wisps(lx), 5);
    registerOneShot(new Explosions(lx), 5);
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

    registerEffectControlParameter(speedEffect.speed, 1, 0.4);
    registerEffectControlParameter(speedEffect.speed, 1, 5);
    registerEffectControlParameter(colorEffect.rainbow);
    registerEffectControlParameter(colorEffect.mono);
    registerEffectControlParameter(colorEffect.desaturation);
    registerEffectControlParameter(colorEffect.sharp);
    registerEffectControlParameter(blurEffect.amount, 0.65);
    registerEffectControlParameter(spinEffect.spin, 0.65);
    registerEffectControlParameter(ghostEffect.amount, 0, 0.16, 1);
    registerEffectControlParameter(scrambleEffect.amount, 0, 1, 1);
    registerEffectControlParameter(colorStrobeTextureEffect.amount, 0, 1, 1);
    registerEffectControlParameter(fadeTextureEffect.amount, 0, 1, 1);
    registerEffectControlParameter(acidTripTextureEffect.amount, 0, 1, 1);
    registerEffectControlParameter(candyCloudTextureEffect.amount, 0, 1, 1);
    registerEffectControlParameter(staticEffect.amount, 0, .3, 1);
    registerEffectControlParameter(candyTextureEffect.amount, 0, 1, 5);

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

  List<NDBConfig> loadNDBConfigFile() {
    return loadJSONFile(Config.NDB_CONFIG_FILE, new TypeToken<List<NDBConfig>>() {
    }.getType());
  }

  List<TreeCubeConfig> loadCubeConfigFile() {
    return loadJSONFile(Config.CUBE_CONFIG_FILE, new TypeToken<List<TreeCubeConfig>>() {
    }.getType());
  }
  List<TreeConfig> loadTreeConfigFile() {
    return loadJSONFile(Config.TREE_CONFIG_FILE, new TypeToken<List<TreeConfig>>() {
    }.getType());
  }

    List<ShrubCubeConfig> loadShrubCubeConfigFile() {
        return loadJSONFile(Config.SHRUB_CUBE_CONFIG_FILE, new TypeToken<List<ShrubCubeConfig>>() {
      }.getType());
  }

    List<ShrubConfig> loadShrubConfigFile() {
        return loadJSONFile(Config.SHRUB_CONFIG_FILE, new TypeToken<List<ShrubConfig>>() {
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
    List<TreeCubeConfig> cubeConfigs = new ArrayList();
    for (Cube cube: model.cubes){
      if (cube.config.isActive){
        cubeConfigs.add(cube.config);
      }
    }
    String data = new Gson().toJson(cubeConfigs);
    saveJSONToFile(data, Config.CUBE_CONFIG_FILE);
  }

  void saveShrubCubeConfigs(){
      List<ShrubCubeConfig> shrubCubeConfigs = new ArrayList();
      for (ShrubCube shrubCube: model.shrubCubes){
          shrubCubeConfigs.add(shrubCube.config);
      }
      String data = new Gson().toJson(shrubCubeConfigs);
      saveJSONToFile(data, Config.SHRUB_CUBE_CONFIG_FILE);
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
    channel.setFaderTransition(new TreesTransition(lx, channel, model, channelTreeLevels, channelShrubLevels));
    channel.addListener(new LXChannel.AbstractListener() {
      LXTransition transition;

      @Override
            public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
        if (!channel.enabled.isOn()) {
          transition = nextPattern.getTransition();
          nextPattern.setTransition(null);
        }
      }

      @Override
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
        @Override
        public void onParameterChanged(LXParameter parameter) {
          channel.enabled.setValue(channel.getFader().getValue() != 0);
        }
      });
    }
  }

  ArrayList<TSPattern> patterns;

  void configureChannels() {
    for (int i = 0; i < Engine.NUM_BASE_CHANNELS; ++i) {
      LXChannel channel = lx.engine.addChannel(getPatternListForChannels());
      setupChannel(channel, true);
      if (i == 0) {
        channel.getFader().setValue(1);
      }
      channel.goIndex(i); // set to pattern of given index
    }
    engineController.baseChannelIndex = lx.engine.getChannels().size() - 1;

  for (int i = 0; i < Engine.NUM_SERVER_CHANNELS; ++i) {
    patterns = new ArrayList<TSPattern>();
    registerIPadPatterns();

    LXChannel channel = lx.engine.addChannel(patterns.toArray(new TSPattern[0]));
    setupChannel(channel, true);
    // I don't know quite what this does, but setting to 1.0f means
    // reguarl patterns don't work --- wtf?
    channel.getFader().setValue(0.0f);

    if (i == 0) {
      channel.goIndex(1); // sets the pattern
    }
      patterns = null;
      engineController.numChannels = NUM_SERVER_CHANNELS;
    }

    lx.engine.removeChannel(lx.engine.getDefaultChannel());
  }

  void registerOneShot(TSPattern pattern) {
    registerOneShot(pattern, 4);
  }

  void registerOneShot(TSPattern pattern, int apc40DrumpadRow) {
    registerVisual(pattern, apc40DrumpadRow, VisualType.OneShot);
  }

  void registerPattern(TSPattern pattern) {
    registerPattern(pattern, 2);
  }

  void registerPattern(TSPattern pattern, int apc40DrumpadRow) {
    registerVisual(pattern, apc40DrumpadRow, VisualType.Pattern);
  }

  void registerVisual(TSPattern pattern, int apc40DrumpadRow, VisualType visualType) {
    LXTransition t = new DissolveTransition(lx).setDuration(dissolveTime);
    pattern.setTransition(t);

    Triggerable triggerable = configurePatternAsTriggerable(pattern);

    if (Config.enableAPC40) {
      apc40DrumpadTriggerablesLists[apc40DrumpadRow].add(triggerable);
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

  void registerEffect(LXEffect effect) {
    if (effect instanceof Triggerable) {
      Triggerable triggerable = (Triggerable) effect;
      if (Config.enableAPC40) {
        apc40DrumpadTriggerablesLists[0].add(triggerable);
      }
    }
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter) {
    registerEffectControlParameter(parameter, 0, 1, 0);
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter, double onValue) {
    registerEffectControlParameter(parameter, 0, onValue, 0);
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter, double offValue, double onValue) {
    registerEffectControlParameter(parameter, offValue, onValue, 0);
  }

  void registerEffectControlParameter(LXListenableNormalizedParameter parameter, double offValue, double onValue, int row) {
    ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter, offValue, onValue);
    if (Config.enableAPC40) {
      apc40DrumpadTriggerablesLists[row].add(triggerable);
    }
  }

  void registerEffectController(String name, LXEffect effect, LXListenableNormalizedParameter parameter) {
    ParameterTriggerableAdapter triggerable = new ParameterTriggerableAdapter(lx, parameter);
    TSEffectController effectController = new TSEffectController(name, effect, triggerable);

    engineController.effectControllers.add(effectController);
  }

  void enableEffects() {
    for (LXEffect effect : lx.getEffects()) {
      effect.enabled.setValue(true);
    }
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
      @Override
      public void onMessage(LXEngine engine, String message) {
        if (message.length() > 8 && message.substring(0, 7).equals("master/")) {
          
          double value = Double.parseDouble(message.substring(7));
          masterBrightnessEffect.getParameter().setValue(value);
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
        @Override
        public void onParameterChanged(LXParameter parameter) {
          if (parameter.getValue() > 0) {
            automation[ii].reset();
            automation[ii].armRecord.setValue(false);
          }
        }
      });
    }

    JsonArray jsonArr = loadSavedSetFile(Config.AUTOPLAY_FILE);
    automation[automationSlot.getValuei()].loadJson(jsonArr);
    // slotLabel.setLabel(labels[automationSlot.getValuei()] = filename);
    automation[automationSlot.getValuei()].looping.setValue(true);
    engineController.automation = automation[automationSlot.getValuei()];

    if (Config.autoplayBMSet) {
      automation[automationSlot.getValuei()].start();
    }
  }

  /* configureTriggerables */

  ArrayList<Triggerable>[] apc40DrumpadTriggerablesLists; // this is temporary
  Triggerable[][] apc40DrumpadTriggerables; // this is forever

  @SuppressWarnings("unchecked")
  void configureTriggerables() {

    if (Config.enableAPC40) {
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

    engineController.startEffectIndex = lx.engine.getEffects().size();
    registerIPadEffects();
    engineController.endEffectIndex = lx.engine.getEffects().size();

    // 
    if (Config.enableAPC40) {
      // create a two-dimensional array, and copy... looks like an attempt to use static arrays instead
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
    midiEngine = new MidiEngine(lx, effectKnobParameters, apc40Drumpad, drumpadVelocity, previewChannels, bpmTool, uiDeck,  masterBrightnessEffect.getParameter(), automationSlot, automation, automationStop);
  }

  /* configureExternalOutput */

  void configureExternalOutput() {
    // Output stage
    try {
      treeOutput = new LXDatagramOutput(lx);
      treeDatagrams = new LXDatagram[model.ipMap.size()];
      int ci = 0;
      for (Map.Entry<String, Cube[]> entry : model.ipMap.entrySet()) {
        String ip = entry.getKey();
        Cube[] cubes = entry.getValue();
        treeOutput.addDatagram(treeDatagrams[ci++] = Output.treeClusterDatagram(cubes).setAddress(ip));
      }
      outputBrightness.parameters.add(treeOutput.brightness);
      treeOutput.enabled.setValue(true);
      lx.addOutput(treeOutput);
    } catch (Exception x) {
      System.out.println(x);
    }
    try {
        shrubOutput = new LXDatagramOutput(lx);
        shrubDatagrams = new LXDatagram[model.shrubIpMap.size()];
        int ci = 0;
        for (Entry<String, ShrubCube[]> entry : model.shrubIpMap.entrySet()) {
          String shrubIp = entry.getKey();
          ShrubCube[] shrubCubes = entry.getValue();
          shrubOutput.addDatagram(shrubDatagrams[ci++] = Output.shrubClusterDatagram(shrubCubes).setAddress(shrubIp));
        }
        outputBrightness.parameters.add(shrubOutput.brightness);
        shrubOutput.enabled.setValue(true);
        lx.addOutput(shrubOutput);
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

  // Log Helper
  void log(String s) {
  	  System.out.println(
  		ZonedDateTime.now( localZone ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) + " " + s );
  }

  class FrameRateLogTask implements LXLoopTask {

  	public final LXEngine engine;

  	long lastCheckTime = System.currentTimeMillis();
  	long lastPrintTime = System.currentTimeMillis();

  	FrameRateLogTask(LXEngine engine) {
  		this.engine = engine;
  	};

  	@Override
  	public void loop(double deltaMs) {

  		long now = System.currentTimeMillis();

        // how often do we have frame rates under 2 seconds?
  		if (lastCheckTime + 2000 < now) {
  			double fr = this.engine.frameRate();
  			if (fr < 2.0f) {
  				log( " low frame rate: " + fr );
  			}
  			lastCheckTime = now;
  		}

        // 120 seconds apart - 2 minutes
  		if (lastPrintTime + 120000 < now) {
  			double fr = this.engine.frameRate();
  			log( " frame rate: " + fr );
  			lastPrintTime = now;

  		}
  	}

  }


}

// this is the controller used by the TCP connection system
// it effects the 'Server channels' only

class EngineController {
  LX lx;

  int baseChannelIndex; // the starting channel that the engine controls - ie, 8
  int numChannels;      // the number of channels controlled by this controller is 3

  int startEffectIndex; // these are the limits of the IPAD EFFECTS
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
  BrightnessScaleEffect masterBrightnessEffect;
  BasicParameterProxy outputBrightness;
  AutoPauseTask autoPauseTask;

  EngineController(LX lx) {
    this.lx = lx;

    System.out.println("creating auto pause task");
    this.autoPauseTask = new AutoPauseTask();
    lx.engine.addLoopTask(this.autoPauseTask);
  }

  // this gets the 'iPad channels' only
  List<LXChannel> getChannels() {
    return lx.engine.getChannels().subList(baseChannelIndex, baseChannelIndex + numChannels);
  }

  // The indexes here are real indexes, because when we gave the channel, we gave the actual index
  void setChannelPattern(int channelIndex, int patternIndex) {
    if (patternIndex == -1) {
      patternIndex = 0;
    } else {
      patternIndex++;
    }
    lx.engine.getChannel(channelIndex).goIndex(patternIndex);
  }

  void setChannelVisibility(int channelIndex, double visibility) {
    // have to be sure
    LXChannel channel = lx.engine.getChannel(channelIndex);
    //channel.enabled.setValue(true);
    channel.getFader().setValue(visibility);
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

  // this controlls the OUTPUT brightness for controlling the amount
  // of power consumed, it will NOT effect what you see in the model 
  // on processing
  void setMasterBrightness(double amount) {
    masterBrightnessEffect.amount.setValue(amount);
  }

  double getMasterBrightness() {
    double ret = masterBrightnessEffect.getValue();
    return( ret );

  }

  void setHue(double amount) {
    System.out.println("Set Master Hue: "+amount+" not implemented yet");
  }

  double getHue() {
    System.out.println("Get Master Hue: stub");
    return(0.0f);
  }


  void setAutoplay(boolean autoplay) {
    setAutoplay(autoplay, false);
  }

  // If true, this enables the base channels and starts the autoplay.
  // if false, this disables the base channels and enables the IPad channels
  //    and keeps the prior channel state and resets to that

  void setAutoplay(boolean autoplay, boolean forceUpdate) {
    if (autoplay != isAutoplaying || forceUpdate) {
      isAutoplaying = autoplay;
      automation.setPaused(!autoplay);

      // I think this should only effect base channels? bb
      if (previousChannelIsOn == null) {
        previousChannelIsOn = new boolean[lx.engine.getChannels().size()];
        for (LXChannel channel : lx.engine.getChannels()) {
          previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
        }
      }

      for (LXChannel channel : lx.engine.getChannels()) {
        boolean toEnable;
        if (channel.getIndex() < baseChannelIndex) {
          toEnable = autoplay; // base channels
        } else if (channel.getIndex() < baseChannelIndex + numChannels) {
          toEnable = !autoplay; // server channels
        } else {
          toEnable = autoplay; // others
        }

        if (toEnable) {
          channel.enabled.setValue(previousChannelIsOn[channel.getIndex()]);
          //System.out.println(" setAutoplay: toEnable true: channel "+channel.getIndex()+" setting to "+previousChannelIsOn[channel.getIndex()]);
        } else {
          previousChannelIsOn[channel.getIndex()] = channel.enabled.isOn();
          channel.enabled.setValue(false);
          //System.out.println(" setAutoplay: toEnable false: channel "+channel.getIndex()+" setting to false");
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

    /* force pauses whenever autoplay is playing (only then) */
    /* moved this into EngineController because it seems more right, it controls things
       and also we need to bang it from the network, which has an Engine Controller but
       no easy link to Engine */
  class AutoPauseTask implements LXLoopTask {

  	long startTime = System.currentTimeMillis() / 1000;
  	boolean lightsOn = true;

    boolean fadeing = false;
    long    fadeStart;
    Long    fadeEnd;
    boolean fadeIn = false; // or its is a fade out

    // can't use lightson / lightsoff because that takes into account whether we are autoplay
    boolean pauseStateRunning() {

    	// if not configured, running
    	if (Config.pauseRunMinutes == 0.0 || Config.pausePauseMinutes == 0.0) return(true);

    	double timeRemaining;
    	long now = ( System.currentTimeMillis() / 1000);
    	long totalPeriod = (long) ((Config.pauseRunMinutes + Config.pausePauseMinutes) * 60.0);
  		long secsIntoPeriod = (now - startTime) % totalPeriod;

  		// paused
  		if ((Config.pauseRunMinutes * 60.0) <= secsIntoPeriod) {
  			log("pauseStateRunning: false");
  			return(false);
  		}
  		log("pauseStateRunning: true");
  		return(true);
    }

    // number of seconds left in current state
    // does NOT include fade
    // does NOT account for whether we are in auto-play
    double pauseTimeRemaining() {

    	if (Config.pauseRunMinutes == 0.0 || Config.pausePauseMinutes == 0.0) return(0.0);
    	final double pauseRunSeconds = Config.pauseRunMinutes * 60.0;
    	final double pausePauseSeconds = Config.pausePauseMinutes * 60.0;

    	double timeRemaining;
    	long now = ( System.currentTimeMillis() / 1000);
    	long totalPeriod = (long) (pauseRunSeconds + pausePauseSeconds);
  		long secsIntoPeriod = (now - startTime) % totalPeriod;

  		// we're in paused
  		if (pauseRunSeconds <= secsIntoPeriod) {
  			timeRemaining = pausePauseSeconds  - (secsIntoPeriod - pauseRunSeconds);
  		}
  		// we're in running
  		else {
  			timeRemaining = pauseRunSeconds - secsIntoPeriod;
  		}

    	log("pauseTimeRemaining: "+timeRemaining);

  		return(timeRemaining);
    }

    // reset to beginning of running - next loop around will do the right thing
    void pauseResetRunning() {
    	//log("ResetRunning: ");
    	startTime = System.currentTimeMillis() / 1000;
    }

    // reset to beginning of pause (which is in the past), this is a little counter intuitive but the start of Pause is Run in the past
    void pauseResetPaused() {
      //log("ResetPaused: ");
    	startTime = ( System.currentTimeMillis() / 1000 ) - (long)Math.floor(Config.pauseRunMinutes * 60.0);
    }

    // these nows are in miliseconds
    void startFadeIn() {
      fadeStart = System.currentTimeMillis();
      fadeEnd = fadeStart + (long)( Config.pauseFadeInSeconds * 1000 );
      fadeIn = true;
      fadeing = true;
      //log(" start Fade In ");
      // no point in trying to set not, hasn't changed enough
    }

    // these nows are in miliseconds
    void startFadeOut() {
      fadeStart = System.currentTimeMillis();
      fadeEnd = fadeStart + (long)( Config.pauseFadeOutSeconds * 1000 );
      fadeIn = false;
      fadeing = true;
      //log(" start fade out ");
      // no point in trying to set not, hasn't changed enough
    }

    void setFadeValue() {
      long now = System.currentTimeMillis();
      if (now > fadeEnd) {
        fadeing = false;
        outputBrightness.setValue(fadeIn ? 1.0f : 0.0f);
        lightsOn = fadeIn ? true : false;
        //log(" fade over ");
        return;
      }
      double value = ((double)(now - fadeStart)) / (double)((fadeEnd - fadeStart));
      // log(" fade value: fadeStart "+fadeStart+" fadeEnd "+fadeEnd+" now "+now);
      if (fadeIn == false) { value = 1.0f - value; }
      outputBrightness.setValue(value);
      //log(" fadeing: "+(fadeIn?"in ":"out ")+" value: "+value);
    }

  	@Override
  	public void loop(double deltaMs) {

		  // if not configured just quit (allows for on-the-fly-config-change)
  		if (Config.pauseRunMinutes == 0.0 || Config.pausePauseMinutes == 0.0) {
  			return;
  		}

      // no matter what, if we start fading, finish it
      if (fadeing) {
        setFadeValue();
        return;
      }

  		// if we are not autoplaying, the ipad has us, and we trust the ipad
  		if (! isAutoplaying ) {
  			if (lightsOn == false) {
  				log( " PauseTask: not autoplaying, lightson unconditionally " );
  				startFadeIn();
  			}
  			return;
  		}

       // move these to seconds for better scale
      long now = ( System.currentTimeMillis() / 1000);

  		// check if I should be on or off
  		boolean shouldLightsOn = true;
  		long totalPeriod = (long) ((Config.pauseRunMinutes + Config.pausePauseMinutes) * 60.0);
  		long secsIntoPeriod = (now - startTime) % totalPeriod;
  		if ((Config.pauseRunMinutes * 60.0) <= secsIntoPeriod) shouldLightsOn = false;

      //log( " PauseTask: totalPeriod "+totalPeriod+" timeIntoPeriod "+secsIntoPeriod+" should: "+shouldLightsOn );
      //log( " PauseTask: now  "+now+" startTime "+startTime );

  		if (shouldLightsOn && lightsOn == false) {
  			log( " PauseTask: lightson: for "+Config.pauseRunMinutes+" minutes" );
        	startFadeIn();
  		}
  		else if (shouldLightsOn == false && lightsOn) {
  			log(" PauseTask: lightsoff: for "+Config.pausePauseMinutes+" minutes" );
        	startFadeOut();
  		}
  	}
  }

   // Log Helper
  ZoneId localZone = ZoneId.of("America/Los_Angeles");
  void log(String s) {
  	  System.out.println(
  		ZonedDateTime.now( localZone ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) + " " + s );
  }

}


class TreesTransition extends LXTransition {

  private final LXChannel channel;
  private final Model model;
  public final DiscreteParameter blendMode = new DiscreteParameter("MODE", 4);
  private LXColor.Blend blendType = LXColor.Blend.ADD;
  final ChannelTreeLevels[] channelTreeLevels;
  final ChannelShrubLevels[] channelShrubLevels;
  final BasicParameter fade = new BasicParameter("FADE", 1);

  TreesTransition(LX lx, LXChannel channel, Model model, ChannelTreeLevels[] channelTreeLevels, ChannelShrubLevels[] channelShrubLevels) {
    super(lx);
    this.model = model;
    addParameter(blendMode);
    this.channel = channel;
    this.channelTreeLevels = channelTreeLevels;
    this.channelShrubLevels = channelShrubLevels;

    blendMode.addListener(new LXParameterListener() {
      @Override
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

  // I think this modifies the outputs of trees and shrubs specifically, using the tree and shrub
  // sliders. Currently, we don't have shrub sliders, and there's no way to set a level on a tree
  // that would effect triggerables

  // it appears the functionlity is to blend c1 and c2 into the colors output, mediated
  // by the channelTreeLevels.
  @Override
  protected void computeBlend(int[] c1, int[] c2, double progress) {

    // these levels only exist on channels that show up in the screen, because they're
    // tied to the screen. Bypass if it's a channel without this slider
    //if (this.channel.getIndex() >= this.channelTreeLevels.length) {
    //  System.out.println(" computeBlend: channel index too high "+this.channel.getIndex() );
    //  return;
    //}
    int treeIndex = 0;
    double treeLevel;
    for (Tree tree : model.trees) {
      float amount = 1.0f; // default value if there is no extra level
      if (this.channel.getIndex() < this.channelTreeLevels.length) {
        treeLevel = this.channelTreeLevels[this.channel.getIndex()].getValue(treeIndex);
        amount = (float) (progress * treeLevel);
      }
      if (amount == 0.0f) {
        for (LXPoint p : tree.points) {
          colors[p.index] = c1[p.index];
        }
      } else if (amount == 1.0f) {
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

    int shrubIndex = 0;
    double shrubLevel;
    for (Shrub shrub : model.shrubs) {
      float amount = 1.0f; // default value if there is no extra level
      if (this.channel.getIndex() < this.channelShrubLevels.length) {
        shrubLevel = this.channelShrubLevels[this.channel.getIndex()].getValue(shrubIndex);
        amount = (float) (progress * shrubLevel);
      }
      if (amount == 0.0f) {
        for (LXPoint p : shrub.points) {
          colors[p.index] = c1[p.index];
        }
      } else if (amount == 1.0f) {
        for (LXPoint p : shrub.points) {
          int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
          colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
        }
      } else {
        for (LXPoint p : shrub.points) {
          int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
          colors[p.index] = LXColor.lerp(c1[p.index], LXColor.blend(c1[p.index], color2, this.blendType), amount);
        }
      }
      shrubIndex++;
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

class ChannelShrubLevels{
    private BasicParameter[] levels;
    ChannelShrubLevels(int numShrubs){
      levels = new BasicParameter[numShrubs];
      for (int i=0; i<numShrubs; i++){
        this.levels[i] = new BasicParameter("shrub" + i, 1);
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

  @Override
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

  @Override
    protected double updateValue(double value) {
    for (BasicParameter parameter : parameters) {
      parameter.setValue(value);
    }
    return value;
  }
}
