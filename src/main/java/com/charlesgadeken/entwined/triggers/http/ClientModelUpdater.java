package com.charlesgadeken.entwined.triggers.http;

import com.charlesgadeken.entwined.EngineController;
import com.charlesgadeken.entwined.effects.TSEffectController;
import com.charlesgadeken.entwined.patterns.EntwinedBasePattern;
import heronarts.lx.mixer.LXChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientModelUpdater {
    EngineController engineController;
    ClientCommunicator communicator;

    public ClientModelUpdater(EngineController engineController, ClientCommunicator communicator) {
        this.engineController = engineController;
        this.communicator = communicator;
    }

    void sendModel() {
        Map<String, Object> returnParams = new HashMap<String, Object>();

        returnParams.put("autoplay", engineController.isAutoplaying);

        List<Map> channelsParams = new ArrayList<>(engineController.numChannels);
        for (LXChannel channel : engineController.getChannels()) {
            Map<String, Object> channelParams = new HashMap<String, Object>();
            channelParams.put("index", channel.getIndex() - engineController.baseChannelIndex);
            int currentPatternIndex = channel.getNextPatternIndex();
            if (currentPatternIndex == 0) {
                currentPatternIndex = -1;
            } else {
                currentPatternIndex--;
            }
            channelParams.put("currentPatternIndex", currentPatternIndex);
            channelParams.put("visibility", channel.fader.getValue());

            List<Map<String, Object>> patternsParams = new ArrayList<>(channel.getPatterns().size());
            for (int i = 1; i < channel.getPatterns().size(); i++) {
                EntwinedBasePattern pattern = (EntwinedBasePattern) channel.getPatterns().get(i);
                Map<String, Object> patternParams = new HashMap<>();
                // TODO(meawoppl) wat VV
                patternParams.put("name", pattern.readableName);
                patternParams.put("index", i-1);
                patternsParams.add(patternParams);
            }
            channelParams.put("patterns", patternsParams);

            channelsParams.add(channelParams);
        }
        returnParams.put("channels", channelsParams);

        List<Map<String, Object>> effectsParams = new ArrayList<>(engineController.effectControllers.size());
        for (int i = 0; i < engineController.effectControllers.size(); i++) {
            TSEffectController effectController = engineController.effectControllers.get(i);
            Map<String, Object> effectParams = new HashMap<String, Object>();
            effectParams.put("index", i);
            effectParams.put("name", effectController.getName());
            effectsParams.add(effectParams);
        }
        returnParams.put("colorEffects", effectsParams);

        returnParams.put("activeColorEffectIndex", engineController.activeEffectControllerIndex);

        returnParams.put("speed", engineController.speedEffect.speed.getValue());
        returnParams.put("spin", engineController.spinEffect.spin.getValue());
        returnParams.put("blur", engineController.blurEffect.level.getValue());
        returnParams.put("scramble", engineController.scrambleEffect.amount.getValue());

        communicator.send("model", returnParams);
    }
}
