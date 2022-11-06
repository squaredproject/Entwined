package entwined.plugin;

final class Config {
  // Change this when setting up a new installation!
  // it's used for Canopy and must be unique for each installation
  static final String installationId = "ggp";

  static final boolean autoplayBMSet = true;

  static final boolean enableAPC40 = true;
  static final boolean enableSoundSyphon = false;

  static final boolean enableOutputMinitree = false;
  static final boolean enableOutputBigtree = false;

  // these configure the mandated "pause" to keep crowds down
  // set either to 0 to disable
  static final double pauseRunMinutes = 0.0;
  static final double pausePauseMinutes = 0.0;
  static final double pauseFadeInSeconds = 0.0;
  static final double pauseFadeOutSeconds = 0.0;

  // Initial Value of the Autoplay Brightness setting, to allow
  // the sculpture to start with a different value
  static final double autoplayBrightness = 1.0;


  // the interaction server. Set to null to disable.
  static final String canopyServer = "";
  //static final String canopyServer = "http://localhost:3000/lx";
  //static final String canopyServer = "https://entwined-api.charliestigler.com/lx";

  static final String NDB_CONFIG_FILE = "data/entwinedNDBs.json";
  static final String CUBE_CONFIG_FILE = "data/entwinedCubes.json";
  static final String TREE_CONFIG_FILE = "data/entwinedTrees.json";
  static final String SHRUB_CUBE_CONFIG_FILE = "data/entwinedShrubCubes.json";
  static final String SHRUB_CONFIG_FILE = "data/entwinedShrubs.json";
  static final String FAIRY_CIRCLE_CONFIG_FILE = "data/entwinedFairyCircles.json";
  static final String SPOT_CONFIG_FILE = "data/entwinedSpots.json";

  // if this file doesn't exist you get a crash
  static final String AUTOPLAY_FILE = "data/entwinedSetDec2021.json";

  static final int NUM_BASE_CHANNELS = 8;
  static final int NUM_SERVER_CHANNELS = 3;

  // These are the patterns available on the ipad, in the order that they are displayed on the ipad
  // Change this if you want to change the ipad display
  // Any pattern here *must* be listed and registered in the initialization code; consider these
  // to be references to already registered patterns.
  static final String[] iPadPatterns = {"entwined.patters.kyle_fleming.NoPattern"};
  /*
                                        "Twister",
                                        "TwisterGlobal",
                                        "Candy Cloud",
                                        "BeachBall",
                                        "Breath",
                                        "MarkLottor",
                                        "Ripple",
                                        "Stripes",
                                        "Lattice",
                                        "ColoredLeaves",
                                        "Voronoi",
                                        "GalaxyCloud",
                                        "Parallax",
                                        "Burst",
                                        "Peppermint",
                                        "IceCrystals",
                                        "Fire",
                                        "AcidTrip",
                                        "Rain",
                                        "Pond",
                                        "Planes",
                                        "Growth",
                                        "Lightning",
                                        "SparkleTakeOver",
                                        "SparkleHelix",
                                        "MultiSine",
                                        "SeeSaw",
                                        "Cells",
                                        "Fade",
                                        "Springs",
                                        "BassSlam",
                                        "Fireflies",
                                        "Bubbles",
                                        "Wisps",
                                        "Explosions",
                                        "ColorWave",
                                        "Wedges",
                                        "SparkleWave",
                                        "OscillatingDarkRing",
                                        "RadialGradiant",
                                        "ButterflyEffect",
                                        "MultiColor",
                                        "MultiColor2",
                                        "StripeStatic",
                                        "Circles",
                                        "LineScan",
                                        "Stringy",
                                        "RainbowWaveScan",
                                        "SyncSpinner",
                                        "LightHouse",
                                        // "ShrubRiver",
                                        "ColorBlast",
                                        "Vertigo",
                                        "ExpandingCircles",
                                        // "SpiralArms",
                                        "Sparks",
                                        "Blooms",
                                        "MovingPoint",
                                        //"WavesToMainTree",
                                        "Undulation",
                                        "HueRibbons",
                                        "VerticalColorWaves",
                                        "FlockingPoints",
                                        "CircleBreath",
                                        "FirefliesNcase",
                                        "RoseGarden",
                                        "Fountain",
                                        "Fumes",
                                        "ColorStrobe",
                                        "Strobe"
                                        };
                                        */

}