final class Config {

  static final boolean autoplayBMSet = true;

  static final boolean enableAPC40 = false;
  static final boolean enableSoundSyphon = false;

  static final boolean enableOutputMinitree = false;
  static final boolean enableOutputBigtree = true;

  // these configure the mandated "pause" to keep crowds down
  // set either to 0 to disable
  static final double pauseRunMinutes = 0.0;
  static final double pausePauseMinutes = 0.0;
  static final double pauseFadeInSeconds = 0.0;
  static final double pauseFadeOutSeconds = 0.0;

  static final String NDB_CONFIG_FILE = "data/entwinedNDBs.json";
  static final String CUBE_CONFIG_FILE = "data/entwinedCubes.json";
  static final String TREE_CONFIG_FILE = "data/entwinedTrees.json";
  static final String SHRUB_CUBE_CONFIG_FILE = "data/entwinedShrubCubes.json";
  static final String SHRUB_CONFIG_FILE = "data/entwinedShrubs.json";

  // if this file doesn't exist you get a crash
  static final String AUTOPLAY_FILE = "data/ShrubPlusBurningManPlaylist.json";
}
