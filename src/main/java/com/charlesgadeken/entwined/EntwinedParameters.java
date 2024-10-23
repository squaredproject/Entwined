package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.config.ConfigLoader;
import com.charlesgadeken.entwined.levels.ChannelShrubLevels;
import com.charlesgadeken.entwined.levels.ChannelTreeLevels;
import com.charlesgadeken.entwined.model.Model;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;

public class EntwinedParameters extends LXComponent {
    public final BooleanParameter[] previewChannels =
            new BooleanParameter[ConfigLoader.NUM_CHANNELS];
    public final BasicParameterProxy outputBrightness = new BasicParameterProxy(1);
    public final BooleanParameter[][] nfcToggles = new BooleanParameter[6][9];
    public final DiscreteParameter automationSlot =
            new DiscreteParameter("AUTO", ConfigLoader.NUM_AUTOMATION);
    public final BoundedParameter drumpadVelocity = new BoundedParameter("DVEL", 1);
    public final BooleanParameter[] automationStop =
            new BooleanParameter[ConfigLoader.NUM_AUTOMATION];

    public LXListenableNormalizedParameter[] effectKnobParameters;
    final ChannelTreeLevels[] channelTreeLevels;
    final ChannelShrubLevels[] channelShrubLevels;

    public EntwinedParameters(LX lx, Model model) {
        super(lx);
        addParameter(drumpadVelocity);

        for (int i = 0; i < ConfigLoader.NUM_CHANNELS; i++) {
            previewChannels[i] = new BooleanParameter("PRV" + i);
            addParameter(previewChannels[i]);
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                nfcToggles[i][j] = new BooleanParameter(String.format("toggle-%d-%d", i, j));
                addParameter(nfcToggles[i][j]);
            }
        }

        // NOTE(meawoppl) I think this is a mistake. Each contains NUM_CHANNELS containers
        // each with NUM_CHANNELS params contained? IDK
        channelTreeLevels = new ChannelTreeLevels[ConfigLoader.NUM_CHANNELS];
        channelShrubLevels = new ChannelShrubLevels[ConfigLoader.NUM_CHANNELS];
        for (int i = 0; i < ConfigLoader.NUM_CHANNELS; i++) {
            int finalI = i;

            channelTreeLevels[i] = new ChannelTreeLevels(model.trees.size());
            channelTreeLevels[i]
                    .getLevels()
                    .forEach(p -> addParameter(p.getLabel() + "-" + finalI, p));

            channelShrubLevels[i] = new ChannelShrubLevels(model.shrubs.size());
            channelShrubLevels[i]
                    .getLevels()
                    .forEach(p -> addParameter(p.getLabel() + "-" + finalI, p));
        }
    }
}
