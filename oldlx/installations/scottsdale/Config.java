final class Config {
  // Change this when setting up a new installation!
  // it's used for Canopy and must be unique for each installation
  static final String installationId = "scottsdale";

  static final boolean autoplayBMSet = true;

  static final boolean enableAPC40 = false;
  static final boolean enableSoundSyphon = true;

  static final boolean enableOutputMinitree = false;
  static final boolean enableOutputBigtree = true;

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
  //static final String canopyServer = "";
  //static final String canopyServer = "http://localhost:3000/lx";
  static final String canopyServer = "https://entwined-api.charliestigler.com/lx";

  static final String NDB_CONFIG_FILE = "data/entwinedNDBs.json";
  static final String CUBE_CONFIG_FILE = "data/entwinedCubes.json";
  static final String TREE_CONFIG_FILE = "data/entwinedTrees.json";
  static final String SHRUB_CUBE_CONFIG_FILE = "data/entwinedShrubCubes.json";
  static final String SHRUB_CONFIG_FILE = "data/entwinedShrubs.json";
  static final String FAIRY_CIRCLE_CONFIG_FILE = "data/entwinedFairyCircles.json";


  // if this file doesn't exist you get a crash
  static final String AUTOPLAY_FILE = "data/entwined2021.json";
}
