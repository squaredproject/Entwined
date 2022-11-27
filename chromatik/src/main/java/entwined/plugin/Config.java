package entwined.plugin;

import java.util.HashMap;

public final class Config {
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

  public static final HashMap<String, String[]> groups = new HashMap<String, String[]>();
  static {
    groups.put("shrubies", new String[] {"shrub-1", "shrub-2"});
  }

  // These are the patterns available on the ipad, in the order that they are displayed on the device
  // Change this if you want to change the ipad display
  static final Class<?>[] iPadPatterns = {
     entwined.pattern.kyle_fleming.NoPattern.class,
     entwined.pattern.anon.Twister.class,
     entwined.pattern.anon.TwisterGlobal.class,
     entwined.pattern.kyle_fleming.CandyCloud.class,
     entwined.pattern.colin_hunt.BeachBall.class,
     entwined.pattern.colin_hunt.Breath.class,
     entwined.pattern.mark_lottor.MarkLottor.class,
     entwined.pattern.ray_sykes.Ripple.class,
     entwined.pattern.ray_sykes.Stripes.class,
     entwined.pattern.irene_zhou.Lattice.class,
     entwined.pattern.anon.ColoredLeaves.class,
     entwined.pattern.irene_zhou.Voronoi.class,
     entwined.pattern.kyle_fleming.GalaxyCloud.class,
     entwined.pattern.geoff_schmidt.Parallax.class,
     entwined.pattern.charlie_stigler.Burst.class,
     entwined.pattern.colin_hunt.Peppermint.class,
     entwined.pattern.ray_sykes.IceCrystals.class,
     entwined.pattern.irene_zhou.Fire.class,
     entwined.pattern.jake_lampack.AcidTrip.class,
     entwined.pattern.kyle_fleming.Rain.class,
     entwined.pattern.grant_patterson.Pond.class,
     entwined.pattern.grant_patterson.Planes.class,
     entwined.pattern.grant_patterson.Growth.class,
     entwined.pattern.ray_sykes.Lightning.class,
     entwined.pattern.ray_sykes.SparkleTakeOver.class,
     entwined.pattern.ray_sykes.SparkleHelix.class,
     entwined.pattern.ray_sykes.MultiSine.class,
     entwined.pattern.anon.SeeSaw.class,
     entwined.pattern.irene_zhou.Cells.class,
     entwined.pattern.kyle_fleming.Fade.class,
     entwined.pattern.irene_zhou.Springs.class,
     //entwined.pattern.kyle_fleming.BaseSlam.class, tbd
     entwined.pattern.irene_zhou.Fireflies.class,
     entwined.pattern.irene_zhou.Bubbles.class,
     entwined.pattern.kyle_fleming.Wisps.class,
     entwined.pattern.kyle_fleming.Explosions.class,
     entwined.pattern.colin_hunt.ColorWave.class,
     entwined.pattern.geoff_schmidt.Wedges.class,
     entwined.pattern.lindsay_jason.SparkleWave.class,
     entwined.pattern.mattaniah.OscillatingDarkRing.class,
     entwined.pattern.mattaniah.RadialGradiant.class,
     entwined.pattern.quinn_keck.ButterflyEffect.class,
     entwined.pattern.bbulkow.MultiColor.class,
     entwined.pattern.bbulkow.MultiColor2.class,
     entwined.pattern.bbulkow.StripeStatic.class,
     entwined.pattern.misko.Circles.class,
     entwined.pattern.misko.LineScan.class,
     // entwined.pattern.misko.Stringy.class,  oob exception on ctor, to debug.
     entwined.pattern.misko.WaveScanRainbow.class,
     // entwined.pattern.misko.SyncSpinner.class, tbd
     entwined.pattern.misko.LightHouse.class,
     // entwined.pattern.misko.ShrubRiver.class,
     entwined.pattern.misko.ColorBlast.class,
     entwined.pattern.misko.Vertigo.class,
     entwined.pattern.adam_n_katie.ExpandingCircles.class,
     // entwined.pattern.adam_n_katie.SpiralArms.class,
     entwined.pattern.adam_n_katie.Sparks.class,
     entwined.pattern.adam_n_katie.Blooms.class,
     entwined.pattern.adam_n_katie.MovingPoint.class,
     //entwined.pattern.adam_n_katie.WavesToMainTree.class,
     entwined.pattern.adam_n_katie.Undulation.class,
     entwined.pattern.adam_n_katie.HueRibbons.class,
     entwined.pattern.adam_n_katie.VerticalColorWaves.class,
     entwined.pattern.adam_n_katie.FlockingPoints.class,
     entwined.pattern.evy.CircleBreath.class,
     entwined.pattern.evy.FirefliesNcase.class,
     entwined.pattern.sydney_parcell.RoseGarden.class,
     entwined.pattern.lorenz.Fountain.class,
     entwined.pattern.irene_zhou.Fumes.class,
     entwined.pattern.kyle_fleming.ColorStrobe.class,
     entwined.pattern.kyle_fleming.Strobe.class

  };

}