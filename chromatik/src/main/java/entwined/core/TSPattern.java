package entwined.core;


import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;

public abstract class TSPattern extends LXPattern implements Triggerable {
  Boolean triggerable = false;
  Boolean triggered = false;
  double triggeredTime = 0.0;
  // ParameterTriggerableAdapter parameterTriggerableAdapter;
  // String readableName;  // XXX CSW - handled by LXPattern.label, if anyone wants to override

  // protected final LXModel model;  // XXX handled in by LXPattern

  public TSPattern(LX lx) {
    super(lx);
    //model = lx.getModel();
  }


  public void triggeredRun(double deltaMs) {
    run(deltaMs);
  }

  public void setColors(int[] colors) {
    this.colors = colors;
  }

  // And that works for most of the standard  trigger patterns
  public void onTriggered() {
    if (triggerable) {
      this.enabled.setValue(true);
      // turn effect on
      this.triggered = true;
      this.triggeredTime = EntwinedUtils.millis();
    }
  }

  // XXX maybe I just override on disable. Easy enough.
  public void onReleased() {
    if (triggerable) {
      // turn effect off
      this.enabled.setValue(false);
      this.triggered = false;
    }
  }

  public boolean isTriggered() {
    return this.triggered;
  }

  public void onTimeout() {
    onReleased();  // By default, just turn the thing off.
  }

  // For the ones that are timed triggers, they'll override onDisable. I'll make something for that
  //

  public void enableTriggerMode() {
    triggerable = true;
    //void onTriggerableModeEnabled() {
    // getChannel().fader.setValue(0);
    //getChannelFade().setValue(0);
    // parameterTriggerableAdapter = getParameterTriggerableAdapter();
    // parameterTriggerableAdapter.addOutputTriggeredListener(new LXParameterListener() {
      //public void onParameterChanged(LXParameter parameter) {
      //  setCallRun(parameter.getValue() != 0);
      //}
    //});
    //setCallRun(false);
  }

  //Triggerable getTriggerable() {
  //  return parameterTriggerableAdapter;
  //}

  //BoundedParameter getChannelFade() {
    // XXX
  //  return null;
    //return getFaderTransition(getChannel()).fade;
  //}

  //ParameterTriggerableAdapter getParameterTriggerableAdapter() {
  //  return new ParameterTriggerableAdapter(lx, getChannelFade());
  //}

  //protected void setCallRun(boolean callRun) {
  //  getChannel().enabled.setValue(callRun);
  //}

  //boolean getEnabled() {
  //  return getTriggerable().isTriggered();
  //}

  //double getVisibility() {
  //  return getChannel().fader.getValue();
  //}

  /* XXX
  TreesTransition getFaderTransition(LXChannel channel) {
    return (TreesTransition) channel.getFaderTransition();
  }
  */
}

/*
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
  */

/*
 Okay - this is this weird trees transition thing that use channel tree levels and shrub levels and
 I have no idea what else. And it derives from a class that no longer exists.
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

    // Okay, when someone changes the blend mode parameter, update the internals.
    // Now, I don't know how someone sees the blend mode parameter in new lx, but, moving on...
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
  // XXX this seems like something that is very specific to Entwined, and I'm not sure that I'd want
  // it as part of the core, at least not at this level. One of the interesting questions to ask is,
  // what are the key parts of the sculpture, how do I identify them, and how do I control them separately,
  // but that seems like a config file thing that feeds into an abstraction at this level, rather than
  // separate sliders for trees and shrubs and fairy circles and god only knows what else.

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


