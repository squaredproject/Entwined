package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.levels.ChannelShrubLevels;
import com.charlesgadeken.entwined.levels.ChannelTreeLevels;
import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.model.Shrub;
import com.charlesgadeken.entwined.model.Tree;
import heronarts.lx.LX;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.color.LXColor;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class TreesTransition extends LXBlend {

    private final LXChannel channel;
    private final Model model;
    public final DiscreteParameter blendMode = new DiscreteParameter("MODE", 4);
    private LXColor.Blend blendType = LXColor.Blend.ADD;
    final ChannelTreeLevels[] channelTreeLevels;
    final ChannelShrubLevels[] channelShrubLevels;
    final BoundedParameter fade = new BoundedParameter("FADE", 1);

    TreesTransition(
            LX lx,
            LXChannel channel,
            Model model,
            ChannelTreeLevels[] channelTreeLevels,
            ChannelShrubLevels[] channelShrubLevels) {
        super(lx);
        this.model = model;
        addParameter(blendMode);
        this.channel = channel;
        this.channelTreeLevels = channelTreeLevels;
        this.channelShrubLevels = channelShrubLevels;
        blendMode.addListener(
                new LXParameterListener() {
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

    @Override
    public void blend(int[] c1, int[] c2, double progress, int[] colors) {
        int treeIndex = 0;
        double treeLevel;
        for (Tree tree : model.trees) {
            treeLevel = this.channelTreeLevels[this.channel.getIndex()].getValue(treeIndex);
            float amount = (float) (progress * treeLevel);
            if (amount == 0) {
                for (LXPoint p : tree.points) {
                    colors[p.index] = c1[p.index];
                }
            } else if (amount == 1) {
                for (LXPoint p : tree.points) {
                    int color2 =
                            (blendType == LXColor.Blend.SUBTRACT)
                                    ? LX.hsb(0, 0, LXColor.b(c2[p.index]))
                                    : c2[p.index];
                    colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
                }
            } else {
                for (LXPoint p : tree.points) {
                    int color2 =
                            (blendType == LXColor.Blend.SUBTRACT)
                                    ? LX.hsb(0, 0, LXColor.b(c2[p.index]))
                                    : c2[p.index];
                    colors[p.index] =
                            LXColor.lerp(
                                    c1[p.index],
                                    LXColor.blend(c1[p.index], color2, this.blendType),
                                    amount);
                }
            }
            treeIndex++;
        }

        int shrubIndex = 0;
        double shrubLevel;
        for (Shrub shrub : model.shrubs) {
            shrubLevel = this.channelShrubLevels[this.channel.getIndex()].getValue(shrubIndex);
            float amount = (float) (progress * shrubLevel);
            if (amount == 0) {
                for (LXPoint p : shrub.points) {
                    colors[p.index] = c1[p.index];
                }
            } else if (amount == 1) {
                for (LXPoint p : shrub.points) {
                    int color2 =
                            (blendType == LXColor.Blend.SUBTRACT)
                                    ? LX.hsb(0, 0, LXColor.b(c2[p.index]))
                                    : c2[p.index];
                    colors[p.index] = LXColor.blend(c1[p.index], color2, this.blendType);
                }
            } else {
                for (LXPoint p : shrub.points) {
                    int color2 =
                            (blendType == LXColor.Blend.SUBTRACT)
                                    ? LX.hsb(0, 0, LXColor.b(c2[p.index]))
                                    : c2[p.index];
                    colors[p.index] =
                            LXColor.lerp(
                                    c1[p.index],
                                    LXColor.blend(c1[p.index], color2, this.blendType),
                                    amount);
                }
            }
            shrubIndex++;
        }
    }
}
