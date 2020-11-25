package com.charlesgadeken.entwined.triggers.http;

import com.charlesgadeken.entwined.EngineController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import heronarts.lx.LXLoopTask;

import java.util.HashMap;
import java.util.Map;

public class ParseClientTask implements LXLoopTask {
    Gson gson = new Gson();

    EngineController engineController;
    Server server;
    ClientModelUpdater clientModelUpdater;

    ParseClientTask(EngineController engineController, Server server, ClientModelUpdater clientModelUpdater) {
        this.engineController = engineController;
        this.server = server;
        this.clientModelUpdater = clientModelUpdater;
    }

    public void loop(double deltaMs) {
        try {
            Client client = server.available();
            if (client == null) return;

            String whatClientSaid = client.readStringUntil('\n');
            if (whatClientSaid == null) return;

            System.out.print("Request: " + whatClientSaid);

            Map<String, Object> message = null;
            try {
                message = gson.fromJson(whatClientSaid.trim(), new TypeToken<Map<String, Object>>() {}.getType());
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Got: " + message);
                return;
            }

            if (message == null) return;

            String method = (String)message.get("method");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map)message.get("params");

            if (method == null) return;
            if (params == null) params = new HashMap<String, Object>();

            if (method.equals("loadModel")) {
                clientModelUpdater.sendModel();
            } else if (method.equals("setAutoplay")) {
                Boolean autoplay = (Boolean)params.get("autoplay");
                if (autoplay == null) return;
                engineController.setAutoplay(autoplay.booleanValue());
            } else if (method.equals("setChannelPattern")) {
                Double channelIndex = (Double)params.get("channelIndex");
                Double patternIndex = (Double)params.get("patternIndex");
                if (channelIndex == null || patternIndex == null) return;
                engineController.setChannelPattern(channelIndex.intValue() + engineController.baseChannelIndex, patternIndex.intValue());
            } else if (method.equals("setChannelVisibility")) {
                Double channelIndex = (Double)params.get("channelIndex");
                Double visibility = (Double)params.get("visibility");
                if (channelIndex == null || visibility == null) return;
                engineController.setChannelVisibility(channelIndex.intValue() + engineController.baseChannelIndex, visibility);
            } else if (method.equals("setActiveColorEffect")) {
                Double effectIndex = (Double)params.get("effectIndex");
                if (effectIndex == null) return;
                engineController.setActiveColorEffect(effectIndex.intValue());
            } else if (method.equals("setSpeed")) {
                Double amount = (Double)params.get("amount");
                if (amount == null) return;
                engineController.setSpeed(amount);
            } else if (method.equals("setSpin")) {
                Double amount = (Double)params.get("amount");
                if (amount == null) return;
                engineController.setSpin(amount);
            } else if (method.equals("setBlur")) {
                Double amount = (Double)params.get("amount");
                if (amount == null) return;
                engineController.setBlur(amount);
            } else if (method.equals("setScramble")) {
                Double amount = (Double)params.get("amount");
                if (amount == null) return;
                engineController.setScramble(amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}