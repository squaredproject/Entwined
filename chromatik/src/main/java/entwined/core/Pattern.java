package entwined.core;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

abstract class TSPattern extends LXPattern {
  ParameterTriggerableAdapter parameterTriggerableAdapter;
  String readableName;

  protected final LXModel model;

  TSPattern(LX lx) {
    super(lx);
    model = lx.getModel();
  }

  void onTriggerableModeEnabled() {
    getChannel().fader.setValue(0);
    getChannelFade().setValue(0);
    parameterTriggerableAdapter = getParameterTriggerableAdapter();
    parameterTriggerableAdapter.addOutputTriggeredListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        setCallRun(parameter.getValue() != 0);
      }
    });
    setCallRun(false);
  }

  Triggerable getTriggerable() {
    return parameterTriggerableAdapter;
  }

  BoundedParameter getChannelFade() {
    // XXX
    return null;
    //return getFaderTransition(getChannel()).fade;
  }

  ParameterTriggerableAdapter getParameterTriggerableAdapter() {
    return new ParameterTriggerableAdapter(lx, getChannelFade());
  }

  protected void setCallRun(boolean callRun) {
    getChannel().enabled.setValue(callRun);
  }

  boolean getEnabled() {
    return getTriggerable().isTriggered();
  }

  double getVisibility() {
    return getChannel().fader.getValue();
  }

  /* XXX
  TreesTransition getFaderTransition(LXChannel channel) {
    return (TreesTransition) channel.getFaderTransition();
  }
  */
}

class ChannelTreeLevels {
  private BoundedParameter[] levels;
  ChannelTreeLevels(int numTrees) {
    levels = new BoundedParameter[numTrees];
    for (int i=0; i<numTrees; i++) {
      this.levels[i] = new BoundedParameter("tree" + i, 1);
    }
  }
  public BoundedParameter getParameter(int i) {
    return this.levels[i];
  }
  public double getValue(int i) {
    return this.levels[i].getValue();
  }
}

class ChannelShrubLevels {
    private BoundedParameter[] levels;
    ChannelShrubLevels(int numShrubs) {
      levels = new BoundedParameter[numShrubs];
      for (int i=0; i<numShrubs; i++){
        this.levels[i] = new BoundedParameter("shrub" + i, 1);
      }
    }
    public BoundedParameter getParameter(int i) {
      return this.levels[i];
    }
    public double getValue(int i) {
      return this.levels[i].getValue();
    }
  }

class ChannelFairyCircleLevels {
    private BoundedParameter[] levels;
    ChannelFairyCircleLevels(int numFairyCircles) {
      levels = new BoundedParameter[numFairyCircles];
      for (int i=0; i<numFairyCircles; i++){
        this.levels[i] = new BoundedParameter("fc" + i, 1);
      }
    }
    public BoundedParameter getParameter(int i){
      return this.levels[i];
    }
    public double getValue(int i) {
      return this.levels[i].getValue();
    }
  }

/*
class TreesTransition extends LXTransition {

  private final LXChannel channel;
  private final LXModel model;
  public final DiscreteParameter blendMode = new DiscreteParameter("MODE", 4);
  private LXColor.Blend blendType = LXColor.Blend.ADD;
  final ChannelTreeLevels[] channelTreeLevels;
  final ChannelShrubLevels[] channelShrubLevels;
  final ChannelFairyCircleLevels[] channelFairyCircleLevels;
  final BoundedParameter fade = new BoundedParameter("FADE", 1);

  TreesTransition(LX lx, LXChannel channel, LXModel model, ChannelTreeLevels[] channelTreeLevels, ChannelShrubLevels[] channelShrubLevels, ChannelFairyCircleLevels[] channelFairyCircleLevels) {
    super(lx);
    this.model = model;
    addParameter(blendMode);
    this.channel = channel;
    this.channelTreeLevels = channelTreeLevels;
    this.channelShrubLevels = channelShrubLevels;
    this.channelFairyCircleLevels = channelFairyCircleLevels;

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
    for (LXModel tree : model.sub("TREE")) {
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
    for (LXModel shrub : model.sub("SHRUB")) {
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

    int fcIndex = 0;
    double fcLevel;
    for (LXModel fairyCircle : model.sub("FAIRY_CIRCLE")) {
      float amount = 1.0f; // default value if there is no extra level
      if (this.channel.getIndex() < this.channelFairyCircleLevels.length) {
        fcLevel = this.channelFairyCircleLevels[this.channel.getIndex()].getValue(fcIndex);
        amount = (float) (progress * fcLevel);
      }
      if (amount == 0.0f) {
        for (LXPoint p : fairyCircle.points) {
          colors[p.index] = c1[p.index];
        }
      } else if (amount == 1.0f) {
        for (LXPoint p : fairyCircle.points) {
          int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
          colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
        }
      } else {
        for (LXPoint p : fairyCircle.points) {
          int color2 = (blendType == LXColor.Blend.SUBTRACT) ? LX.hsb(0, 0, LXColor.b(c2[p.index])) : c2[p.index];
          colors[p.index] = LXColor.lerp(c1[p.index], LXColor.blend(c1[p.index], color2, this.blendType), amount);
        }
      }
      fcIndex++;
    }
  }
}
*/


