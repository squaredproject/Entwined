import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.LXUtils;
import heronarts.lx.Tempo;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.QuadraticEnvelope;
import heronarts.lx.modulator.LXRangeModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.SquareLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;

class BPMTool {
  
  private final LX lx;
  
  final BooleanParameter tapTempo = new BooleanParameter("Tap");
  final BooleanParameter nudgeUpTempo = new BooleanParameter("Nudge +");
  final BooleanParameter nudgeDownTempo = new BooleanParameter("Nudge -");
  
  final String[] bpmLabels = {"SIN", "SAW", "TRI", "QD", "SQR"};
  final DiscreteParameter tempoLfoType = new DiscreteParameter("Tempo LFO", bpmLabels.length);
  
  final String[] beatLabels = {"1", "\u00bd", "\u00bc", "1/16"}; // 1, 1/2, 1/4, 1/16
  final DiscreteParameter beatType = new DiscreteParameter("Beat", beatLabels.length);
  final double[] beatScale = { 1, 2, 4, 16 };
  
  final BooleanParameter addTempoLfo = new BooleanParameter("Add Tempo LFO");
  final BooleanParameter clearAllTempoLfos = new BooleanParameter("Clear All Tempo LFOs");
  
  
  private LXChannel currentActiveChannel = null;
  final private List<BPMParameterListener> parameterListeners = new ArrayList<BPMParameterListener>();
  final private List<BPMParameterListener> masterEffectParameterListeners = new ArrayList<BPMParameterListener>();
  
  final private ParameterModulatorController[] modulatorControllers;
  final ParameterModulationController modulationController;
  
  BPMTool(LX lx, LXListenableNormalizedParameter[] effectKnobParameters) {
    this.lx = lx;
    
    ParameterModulatorControllerFactory factory = new ParameterModulatorControllerFactory();
    
    modulatorControllers = new ParameterModulatorController[] {
        factory.makeSinLFOController(),
        factory.makeSawLFOController(),
        factory.makeTriangleLFOController(),
        factory.makeQuadraticEnvelopeController(),
        factory.makeSquareLFOController()
    };
    
    modulationController = new ParameterModulationController(lx, modulatorControllers);
    
    addActionListeners(effectKnobParameters);
  }
  
  public void addActionListeners(final LXListenableNormalizedParameter[] effectKnobParameters) {

    tapTempo.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (tapTempo.isOn()) {
          lx.tempo.tap();
        }
      }
    });

    nudgeUpTempo.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (nudgeUpTempo.isOn()) {
          lx.tempo.adjustBpm(1);
        }
      }
    });

    nudgeDownTempo.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (nudgeDownTempo.isOn()) {
          lx.tempo.adjustBpm(-1);
        }
      }
    });
    
    addTempoLfo.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (addTempoLfo.isOn()) {
          watchPatternParameters(lx.engine.getFocusedChannel().getActivePattern());
          watchMasterEffectParameters(effectKnobParameters);
        } else {
          unwatchPatternParameters();
          unwatchMasterEffectParameters();
        }
      }
    });
    
    clearAllTempoLfos.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (clearAllTempoLfos.isOn()) {
          modulationController.unbindAllParameters();
        }
      }
    });
    
    watchEngine(lx.engine);
  }

  private final LXChannel.AbstractListener bindPatternParametersListener = new LXChannel.AbstractListener() {
    @Override
    public void patternDidChange(LXChannel channel, LXPattern pattern) {
      watchPatternParameters(pattern);
    }
  };

  private void watchEngine(final LXEngine engine) {
    engine.focusedChannel.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        watchDeck(engine.getFocusedChannel());
      }
    });
    watchDeck(engine.getFocusedChannel());
  }

  private void watchDeck(LXChannel channel) {
    if (this.currentActiveChannel != channel) {
      if (this.currentActiveChannel != null) {
        this.currentActiveChannel.removeListener(this.bindPatternParametersListener);
      }
      this.currentActiveChannel = channel;
      this.currentActiveChannel.addListener(this.bindPatternParametersListener);
    }
    watchPatternParameters(channel.getActivePattern());
  }

  private void watchPatternParameters(LXPattern pattern) {
    unwatchPatternParameters();
    if (addTempoLfo.isOn()) {
      for (LXParameter parameter : pattern.getParameters()) {
        if (parameter instanceof LXListenableNormalizedParameter) {
          parameterListeners.add(new BPMParameterListener(this, (LXListenableNormalizedParameter)parameter));
        }
      }
    }
  }
  
  private void unwatchPatternParameters() {
    for (BPMParameterListener parameterListener : parameterListeners) {
      parameterListener.stopListening();
    }
    parameterListeners.clear();
  }
  
  private void watchMasterEffectParameters(LXListenableNormalizedParameter[] parameters) {
    for (LXListenableNormalizedParameter parameter : parameters) {
      masterEffectParameterListeners.add(new BPMParameterListener(this, parameter));
    }
  }
  
  private void unwatchMasterEffectParameters() {
    for (BPMParameterListener parameterListener : masterEffectParameterListeners) {
      parameterListener.stopListening();
    }
    masterEffectParameterListeners.clear();
  }
  
  public void bindParameter(LXListenableNormalizedParameter parameter, double minValue, double maxValue) {
    modulationController.bindParameter(getSelectedModulatorController(),
        parameter, minValue, maxValue, getSelectedModulatorScale());
  }
  
  private ParameterModulatorController getSelectedModulatorController() {
    return modulatorControllers[tempoLfoType.getValuei()];
  }
  
  private double getSelectedModulatorScale() {
    return beatScale[beatType.getValuei()];
  }
}

class BPMParameterListener {
  
  final private BPMTool bpmTool;
  final private LXListenableNormalizedParameter parameter;
  final private double startValue;
  
  BPMParameterListener(BPMTool bpmTool, LXListenableNormalizedParameter parameter) {
    this.bpmTool = bpmTool;
    this.parameter = parameter;
    startValue = parameter.getNormalized();
  }
  
  public void stopListening() {
    double endValue = parameter.getNormalized();
    if (startValue != endValue) {
      bpmTool.bindParameter(parameter, startValue, endValue);
    }
  }
}

class ParameterModulationController {
  
  final TempoAdapter tempoAdapter;
  
  final private ParameterModulatorController[] modulatorControllers;
  final private Map<LXListenableNormalizedParameter, ParameterModulatorController> parametersToControllers
      = new HashMap<LXListenableNormalizedParameter, ParameterModulatorController>();
  
  ParameterModulationController(LX lx, ParameterModulatorController[] modulatorControllers) {
    this.modulatorControllers = modulatorControllers;
    
    tempoAdapter = new TempoAdapter(lx.tempo);
    lx.addModulator(tempoAdapter).start();
    
    for (ParameterModulatorController modulatorController : modulatorControllers) {
      modulatorController.modulationController = this;
      tempoAdapter.ramp.addListener(modulatorController);
    }
  }
  
  public void bindParameter(ParameterModulatorController modulatorController,
      LXListenableNormalizedParameter parameter, double minValue, double maxValue, double scale) {
    if (!parametersToControllers.containsKey(parameter)) {
      parametersToControllers.put(parameter, modulatorController);
      modulatorController.startModulatingParameter(parameter, minValue, maxValue, scale);
    }
  }
  
  public void onParameterUnboundItself(LXListenableNormalizedParameter parameter) {
    parametersToControllers.remove(parameter);
  }
  
  public void unbindAllParameters() {
    for (ParameterModulatorController modulatorController : modulatorControllers) {
      modulatorController.stopModulatingAllParameters();
    }
    parametersToControllers.clear();
  }
}

class ParameterModulatorControllerFactory {
  public ParameterModulatorController makeQuadraticEnvelopeController() {
    return new ParameterModulatorController(new QuadraticEnvelope(0, 1, 1));
  }
  
  public ParameterModulatorController makeSawLFOController() {
    return new ParameterModulatorController(new SawLFO(0, 1, 1));
  }
  
  public ParameterModulatorController makeSinLFOController() {
    return new ParameterModulatorController(new SinLFO(0, 1, 1));
  }
  
  public ParameterModulatorController makeSquareLFOController() {
    return new ParameterModulatorController(new SquareLFO(0, 1, 1));
  }
  
  public ParameterModulatorController makeTriangleLFOController() {
    return new ParameterModulatorController(new TriangleLFO(0, 1, 1));
  }
}

class ParameterModulatorController implements LXParameterListener {
  
  private final LXRangeModulator modulator;
  ParameterModulationController modulationController;
  private final List<ParameterController> parameterControllers = new ArrayList<ParameterController>();
  
  ParameterModulatorController(LXRangeModulator modulator) {
    this.modulator = modulator;
  }
  
  public void startModulatingParameter(LXListenableNormalizedParameter parameter, double minValue, double maxValue, double scale) {
    parameterControllers.add(new ParameterController(parameter, minValue, maxValue, scale));
  }
  
  public void stopModulatingAllParameters() {
    parameterControllers.clear();
  }
  
  public void onParameterChanged(LXParameter parameter) {
    if (parameterControllers.size() > 0) {
      Iterator<ParameterController> iter = parameterControllers.iterator();
      while (iter.hasNext()) {
        ParameterController parameterController = iter.next();
        if (parameterController.parameterWasChanged()) {
          iter.remove();
          modulationController.onParameterUnboundItself(parameterController.parameter);
        } else {
          double scaledValue = (parameter.getValue() * parameterController.scale) % 1;
          double transformedValue = modulator.setBasis(scaledValue).getValue();
          parameterController.setValue(transformedValue);
        }
      }
    }
  }
}

class ParameterController {
  final LXListenableNormalizedParameter parameter;
  final private double minValue;
  final private double maxValue;
  final double scale;
  private double lastValue;
  
  ParameterController(LXListenableNormalizedParameter parameter, double minValue, double maxValue, double scale) {
    this.parameter = parameter;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.scale = scale;
    lastValue = parameter.getNormalized();
  }
  
  public void setValue(double value) {
    parameter.setNormalized(LXUtils.lerp(minValue, maxValue, value));
    lastValue = parameter.getNormalized();
  }
  
  public boolean parameterWasChanged() {
    return parameter.getNormalized() != lastValue;
  }
}

class TempoAdapter extends LXModulator {
  
  public final Tempo tempo;
  
  public final BasicParameter bpm;
  public final BasicParameter ramp;
  
  TempoAdapter(final Tempo tempo) {
    super("Tempo Listener");
    this.tempo = tempo;
    ramp = new BasicParameter("Tempo Ramp", tempo.ramp());
    bpm = new BasicParameter("Tempo BPM", tempo.bpm(), 30, 300);
    bpm.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        tempo.setBpm(parameter.getValue());
      }
    });
  }
  
  protected double computeValue(double deltaMs) {
    double progress = tempo.ramp();
    ramp.setValue(progress);
    bpm.setValue(tempo.bpm());
    return progress;
  }
}

