final class Config {

  static final boolean autoplayBMSet = true;

  static final boolean enableAPC40 = false;
  static final boolean enableSoundSyphon = true;

  static final boolean enableOutputMinitree = false;
  static final boolean enableOutputBigtree = false;

  // these configure the mandated "pause" to keep crowds down
  // set either to 0 to disable
  static final double pauseRunMinutes = 1.0;
  static final double pausePauseMinutes = 0.25;
  static final double pauseFadeInSeconds = 3.0;
  static final double pauseFadeOutSeconds = 5.0;

  // the interaction server. Set to null to disable.
  static final String canopyServer = "";
  //static final String canopyServer = "http://localhost:3000/lx";
  //static final String canopyServer = "https://entwined-api.charliestigler.com/lx";

  static final String NDB_CONFIG_FILE = "data/entwinedNDBs.json";
  static final String CUBE_CONFIG_FILE = "data/entwinedCubes.json";
  static final String TREE_CONFIG_FILE = "data/entwinedTrees.json";
  static final String SHRUB_CUBE_CONFIG_FILE = "data/entwinedShrubCubes.json";
  static final String SHRUB_CONFIG_FILE = "data/entwinedShrubs.json";

  // if this file doesn't exist you get a crash
  static final String AUTOPLAY_FILE = "data/big2loveset.json";
}
