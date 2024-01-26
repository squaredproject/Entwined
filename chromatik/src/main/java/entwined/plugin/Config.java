package entwined.plugin;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import heronarts.lx.LX;

public final class Config {

  // Change this when setting up a new installation!
  // it's used for Canopy and must be unique for each installation
  public static String installationId = "transfix";

  // NB - the following 'pause' fields are vestigial, however, they are used in the
  // packet data exchanged between the iPad and the app, which I do not want to
  // change.  CSW, 11/2022
  static final double pauseRunMinutes = 0.0;
  static final double pausePauseMinutes = 0.0;
  static final double pauseFadeInSeconds = 0.0;
  static final double pauseFadeOutSeconds = 0.0;

  // Initial Value of the Autoplay Brightness setting, to allow
  // the sculpture to start with a different value
  static final double autoplayBrightness = 1.0;


  // the interaction server. Set to null to disable.
  public static String canopyServer = "";
  public static Boolean NFCServerEnable = false;
  public static int NFCNumActivities = -1;
  public static short NFCPort = -1;
  public static Boolean attractModeEnable = false;
  public static int attractModeTimeout = 0;

  //static final String canopyServer = "http://localhost:3000/lx";
  //static final String canopyServer = "https://entwined-api.charliestigler.com/lx";

  // if this file doesn't exist you get a crash
  // static final String AUTOPLAY_FILE = "data/entwinedSetDec2021.json";

  public static int NUM_BASE_CHANNELS = 8;
  public static int NUM_SERVER_CHANNELS = 3;

  public static HashMap<String, String[]> groups = new HashMap<String, String[]>();
  /* static {
    groups.put("shrubies", new String[] {"shrub-1", "shrub-2"});
  }
  */

  // These are the patterns available on the ipad, in the order that they are displayed on the device
  // Change this if you want to change the ipad display
  static final Class<?>[] iPadPatterns = {
     entwined.pattern.kyle_fleming.NoPattern.class,
     entwined.pattern.anon.Twister.class,
     entwined.pattern.anon.TwisterGlobal.class,
     entwined.pattern.kyle_fleming.CandyCloud.class,
     entwined.pattern.colin_hunt.BeachBall.class,
     entwined.pattern.colin_hunt.Breath.class,
     entwined.pattern.sam_brocchini.RingoDown.class,
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
     entwined.pattern.kyle_fleming.BassSlam.class,
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
     entwined.pattern.bbulkow.VideoPlayer.class,
     entwined.pattern.misko.Circles.class,
     entwined.pattern.misko.LineScan.class,
     // entwined.pattern.misko.Stringy.class,  oob exception on ctor, to debug.
     entwined.pattern.misko.WaveScanRainbow.class,
     entwined.pattern.misko.SyncSpinner.class,
     entwined.pattern.misko.LightHouse.class,
     // entwined.pattern.misko.ShrubRiver.class,
     entwined.pattern.misko.ColorBlast.class,
     entwined.pattern.misko.Vertigo.class,
     entwined.pattern.adam_n_katie.ExpandingCircles.class,
     entwined.pattern.adam_n_katie.SpiralArms.class,
     entwined.pattern.adam_n_katie.Sparks.class,
     entwined.pattern.adam_n_katie.Blooms.class,
     entwined.pattern.adam_n_katie.MovingPoint.class,
     entwined.pattern.adam_n_katie.WavesToMainTree.class,
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

  static void Init(String configFilename) {
    File configFile = new File(configFilename);
    if ((configFile == null) || !configFile.exists()) {
      return;
    }

    try (FileReader fr = new FileReader(configFile)) {
      JsonObject obj = new Gson().fromJson(fr, JsonObject.class);

      // Read the canopyserver address
      if (obj.has("canopyServer")) {
        canopyServer = obj.get("canopyServer").getAsString();
        System.out.println("Config: Canopy server set to " + canopyServer);
      }

      if (obj.has("installationId")) {
        installationId = obj.get("installationId").getAsString();
        System.out.println("Config: Installation id set to " + installationId);
      }

      // Read the interactive groupings
      if (obj.has("interactiveGroups")) {
        JsonObject interactiveGroups = obj.get("interactiveGroups").getAsJsonObject();
        for (String key : interactiveGroups.keySet()) {
          JsonArray componentsJson = interactiveGroups.get(key).getAsJsonArray();
          String[] components = new String[componentsJson.size()];
          for (int j=0; j<componentsJson.size(); ++j) {
            components[j] = componentsJson.get(j).getAsString();
            System.out.println("Adding component " + components[j] + " to group " + key);
          }
          groups.put(key, components);
        }
        System.out.println("Config: Interactive groups are " + groups);
      }
      if (obj.has("NFCServerEnable")) {
        NFCServerEnable = obj.get("NFCServerEnable").getAsBoolean();
        System.out.println("Config: NFC server set to " + NFCServerEnable);
      }
      if (obj.has("NFCPort")) {
        NFCPort = obj.get("NFCPort").getAsShort();
        System.out.println("Config: NFC port set to " + NFCPort);
      }
      if (obj.has("NFCNumActivities")) {
        NFCNumActivities = obj.get("NFCNumActivities").getAsInt();
        System.out.println("Config: NFC activities set to " + NFCNumActivities);
      }
      if (obj.has("attractModeEnable")) {
        attractModeEnable = obj.get("attractModeEnable").getAsBoolean();
        System.out.println("Config: attractModeEnable set to " + attractModeEnable);
      }
      if (obj.has("attractModeTimeout")) {
        attractModeTimeout = obj.get("attractModeTimeout").getAsInt();
        System.out.println("Config: attractModeTimeout set to " + attractModeTimeout);
      }
      if (obj.has("numBaseChannels")) {
        NUM_BASE_CHANNELS = obj.get("numBaseChannels").getAsInt();
        System.out.println("Config: NUM_BASE_CHANNELS set to " + NUM_BASE_CHANNELS);
      }

      if (obj.has("numServerChannels")) {
        NUM_SERVER_CHANNELS = obj.get("numServerChannels").getAsInt();
        System.out.println("Config: NUM_SERVER_CHANNELS set to " + NUM_SERVER_CHANNELS);
      }

    } catch (Throwable x) {
      LX.error(x, "Could not load config file: " + x.getMessage());
    }
  }
}
