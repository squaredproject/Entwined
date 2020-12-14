package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.config.ConfigLoader;
import com.charlesgadeken.entwined.effects.EntwinedBaseEffect;
import com.charlesgadeken.entwined.effects.TurnOffDeadPixelsEffect;
import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.model.ModelTransformTask;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import com.charlesgadeken.entwined.triggers.drumpad.APC40mk1;
import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.blend.DissolveBlend;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.color.LXDynamicColor;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.component.UIKnob;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import org.reflections.Reflections;
import processing.core.PApplet;
import processing.core.PFont;

public class EntwinedGui extends PApplet implements LXPlugin {
    private static final String WINDOW_TITLE = "Entwined";

    private static int WIDTH = 1280;
    private static int HEIGHT = 800;
    private static boolean FULLSCREEN = false;

    Reflections reflections = new Reflections("com.charlesgadeken");
    private EngineController engineController;

    private LX lx;
    private Model model;
    private EntwinedTriggers triggers;
    private EntwinedParameters parameters;

    private UIGlobalKnobs globalKnobs;

    @Override
    public void settings() {
        if (FULLSCREEN) {
            fullScreen(PApplet.P3D);
        } else {
            size(WIDTH, HEIGHT, PApplet.P3D);
        }
    }

    @Override
    public void setup() {
        LXStudio.Flags flags = new LXStudio.Flags(this);
        flags.resizable = false;
        flags.useGLPointCloud = false;
        flags.startMultiThreaded = true;

        model = Model.fromConfigs();
        lx = new LXStudio(this, flags, model);
        parameters = new EntwinedParameters(lx, model);
        this.surface.setTitle(WINDOW_TITLE);

        engineController = new EngineController(lx);
        configureChannels();

        triggers = new EntwinedTriggers(lx, model, engineController, parameters);
        triggers.configureTriggerables();
        if (triggers.colorEffect != null) {
          globalKnobs.addKnob(triggers.colorEffect.hueShift);
        }

        lx.engine.addLoopTask(new ModelTransformTask(model));

        EntwinedOutput output = new EntwinedOutput(lx, model, parameters.outputBrightness);

        if (ConfigLoader.enableOutputBigtree) {
            lx.addEffect(new TurnOffDeadPixelsEffect(lx));
            output.configureExternalOutput();
        }

        if (APC40mk1.hasACP40(lx) && ConfigLoader.enableAPC40) {
            System.out.println("APC40 Detected");
            triggers.configureMIDI();
            System.out.println("ACP40 Configured");
        } else {
            System.out.println("APC40 Not Detected or not Enabled - Skipping");
        }

        if (ConfigLoader.enableIPad) {
            // TODO(meawoppl) Call fails
            // engineController.setAutoplay(ConfigLoader.autoplayBMSet, true);
            triggers.configureServer();
        }

        System.out.println("setup() completed");
    }

    private LXChannel addChannelsAudited(List<EntwinedBasePattern> patterns, String descr) {
        LXChannel channel = lx.engine.mixer.addChannel(patterns.toArray(new LXPattern[0]));
        channel.label.setValue(patterns.get(0).getLabel());
        channel.addListener(new LXChannel.Listener() {
          public void patternDidChange(LXChannel channel, LXPattern pattern) {
            channel.label.setValue(pattern.getLabel());
          }
        });
        System.out.printf(
                "Registered %d %s patterns to channel %d\n",
                patterns.size(), descr, channel.getIndex());
        return channel;
    }

    void configureChannels() {
        for (int i = 0; i < ConfigLoader.NUM_CHANNELS; ++i) {
            LXChannel channel = addChannelsAudited(EntwinedPatterns.getPatterns(lx), "BASE");
            setupChannel(channel, true);
            if (i == 0) {
                channel.fader.setValue(1);
            }
            channel.goPatternIndex(i);
        }
        engineController.baseChannelIndex = lx.engine.mixer.getChannels().size() - 1;

        if (ConfigLoader.enableIPad) {
            for (int i = 0; i < ConfigLoader.NUM_IPAD_CHANNELS; ++i) {
                LXChannel channel =
                        addChannelsAudited(EntwinedPatterns.registerIPadPatterns(lx), "iPad");

                setupChannel(channel, true);
                channel.fader.setValue(1);
                channel.blendMode.setObjects(new LXBlend[] {new DissolveBlend(lx)});

                if (i == 0) {
                    channel.goPatternIndex(1);
                }
            }
            engineController.numChannels = ConfigLoader.NUM_IPAD_CHANNELS;
        }

        // TODO(meawoppl) Right now this tosses an exception :(
        // lx.engine.mixer.removeChannel(lx.engine.mixer.getDefaultChannel());
    }

    void setupChannel(final LXChannel channel, boolean noOpWhenNotRunning) {
        channel.transitionBlendMode.setObjects(
                new LXBlend[] {
                    new TreesTransition(
                            lx,
                            channel,
                            model,
                            parameters.channelTreeLevels,
                            parameters.channelShrubLevels)
                });

        if (noOpWhenNotRunning) {
            channel.enabled.setValue(channel.fader.getValue() != 0);
            channel.fader.addListener(
                    (LXParameter parameter) ->
                            channel.enabled.setValue(channel.fader.getValue() != 0));
        }
    }

    private void loadPatterns(LX lx) {
        reflections.getSubTypesOf(EntwinedBasePattern.class).forEach(lx.registry::addPattern);
    }

    private void loadEffects(LX lx) {
        reflections.getSubTypesOf(EntwinedBaseEffect.class).forEach(lx.registry::addEffect);
    }

    @Override
    public void initialize(LX lx) {
        // Here is where you should register any custom components or make modifications
        // to the LX engine or hierarchy. This is also used in headless mode, so note that
        // you cannot assume you are working with an LXStudio class or that any UI will be
        // available.

        // Register custom pattern and effect types
        loadPatterns(lx);
        loadEffects(lx);
        
        // Set default palette to rainbow rotate every minute
        lx.engine.palette.color.mode.setValue(LXDynamicColor.Mode.CYCLE);
        lx.engine.palette.color.period.setValue(60);
        
    }

    public void initializeUI(LXStudio lx, LXStudio.UI ui) {
        // Here is where you may modify the initial settings of the UI before it is fully
        // built. Note that this will not be called in headless mode. Anything required
        // for headless mode should go in the raw initialize method above.

        // For faster load times, use Processing's helper to generate a VLW font file
        // ui.theme.setControlFont(ui.applet.loadFont("ArialUnicodeMS-12.vlw"));
        // PFont label = ui.applet.loadFont("Arial-Black-11.vlw");

        // These will look nasty, be warned!
        ui.theme.setControlFont(ui.applet.createFont("Arial", 11));
        PFont label = ui.applet.createFont("Arial Black", 10);
        ui.theme.setLabelFont(label);
        ui.theme.setWindowTitleFont(label);
    }

    public void onUIReady(LXStudio lx, LXStudio.UI ui) {
        // At this point, the LX Studio application UI has been built. You may now add
        // additional views and components to the Ui heirarchy.
        lx.ui.preview.pointCloud.setPointSize(6);
        ui.addLayer(this.globalKnobs = new UIGlobalKnobs(lx, ui));
    }
    
    private class UIGlobalKnobs extends UI2dContext {
      private UIGlobalKnobs(LXStudio lx, LXStudio.UI ui) {
        super(ui, heronarts.lx.studio.ui.UILeftPane.WIDTH, heronarts.lx.studio.ui.toolbar.UIToolbar.HEIGHT, 600, UIKnob.HEIGHT + 4);        
        setContentTarget(
          (UI2dContainer) UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 2)
          .setPadding(2)
          .setBorderColor(ui.theme.getControlBorderColor())
          .setBackgroundColor(ui.theme.getDeviceBackgroundColor())
        );
        addKnob(lx.engine.speed);
      }
      
      public void addKnob(LXListenableNormalizedParameter p) {
        new UIKnob(0, 2, p).addToContainer(this);
      }
    }

    @Override
    public void draw() {
        // All handled by core LX engine, do not modify, method exists only so that Processing
        // will run a draw-loop.
    }

    /**
     * Main interface into the program. Two modes are supported, if the --headless flag is supplied
     * then a raw CLI version of LX is used. If not, then we embed in a Processing 3 applet and run
     * as such.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        LX.log("Initializing LX version " + LXStudio.VERSION);
        boolean headless = false;
        File projectFile = null;
        for (int i = 0; i < args.length; ++i) {
            if ("--help".equals(args[i]) || "-h".equals(args[i])) {
                System.out.println(
                        "java -jar path/to/entwined/jar SOMEPROJECT.lxp --flags\n"
                                + "Flags:\n"
                                + "\t--headless run in headless mode\n"
                                + "\t--fullscreen | -f : Run in fullscreen\n"
                                + "\t--width | -w : Set the width of the screen\n"
                                + "\t--height | -h : Set the height of the screen \n");
                System.exit(0);
            } else if ("--headless".equals(args[i])) {
                headless = true;
            } else if ("--fullscreen".equals(args[i]) || "-f".equals(args[i])) {
                FULLSCREEN = true;
            } else if ("--width".equals(args[i]) || "-w".equals(args[i])) {
                try {
                    WIDTH = Integer.parseInt(args[++i]);
                } catch (Exception x) {
                    LX.error("Width command-line argument must be followed by integer");
                }
            } else if ("--height".equals(args[i]) || "-h".equals(args[i])) {
                try {
                    HEIGHT = Integer.parseInt(args[++i]);
                } catch (Exception x) {
                    LX.error("Height command-line argument must be followed by integer");
                }
            } else if (args[i].endsWith(".lxp")) {
                try {
                    projectFile = new File(args[i]);
                } catch (Exception x) {
                    LX.error(x, "Command-line project file path invalid: " + args[i]);
                }
            }
        }
        if (headless) {
            // We're not actually going to run this as a PApplet, but we need to explicitly
            // construct and set the initialize callback so that any custom components
            // will be run
            headlessInit(projectFile);
        } else {
            PApplet.main(new String[] {EntwinedGui.class.getName()});
        }
    }

    public static LX.Flags headlessInit(@Nullable File projectFile) {
        LX.Flags flags = new LX.Flags();
        flags.initialize = new EntwinedGui();
        if (projectFile == null) {
            LX.log("WARNING: No project filename was specified for headless mode!");
        }
        LX.headless(flags, projectFile);
        return flags;
    }
}
