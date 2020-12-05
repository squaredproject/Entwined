import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXLoopTask;

class AppServer {
  LX lx;
  EngineController engineController;

  AppServer(LX lx, EngineController engineController) {
    this.lx = lx;
    this.engineController = engineController;
  }

  void start() {
    Server server = new Server(5204);

    ClientCommunicator clientCommunicator = new ClientCommunicator(server);
    ClientModelUpdater clientModelUpdater = new ClientModelUpdater(engineController, clientCommunicator);
    ParseClientTask parseClientTask = new ParseClientTask(engineController, server, clientModelUpdater);
    lx.engine.addLoopTask(parseClientTask);
  }
}

class ParseClientTask implements LXLoopTask {
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

class ClientModelUpdater {
  EngineController engineController;
  ClientCommunicator communicator;

  ClientModelUpdater(EngineController engineController, ClientCommunicator communicator) {
    this.engineController = engineController;
    this.communicator = communicator;
  }

  void sendModel() {
    Map<String, Object> returnParams = new HashMap<String, Object>();

    returnParams.put("autoplay", engineController.isAutoplaying);

    List<Map> channelsParams = new ArrayList<Map>(engineController.numChannels);
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
      channelParams.put("visibility", channel.getFader().getValue());

      List<Map> patternsParams = new ArrayList<Map>(channel.getPatterns().size());
      for (int i = 1; i < channel.getPatterns().size(); i++) {
        TSPattern pattern = (TSPattern)channel.getPatterns().get(i);
        Map<String, Object> patternParams = new HashMap<String, Object>();
        patternParams.put("name", pattern.readableName);
        patternParams.put("index", i-1);
        patternsParams.add(patternParams);
      }
      channelParams.put("patterns", patternsParams);

      channelsParams.add(channelParams);
    }
    returnParams.put("channels", channelsParams);

    List<Map> effectsParams = new ArrayList<Map>(engineController.effectControllers.size());
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
    returnParams.put("blur", engineController.blurEffect.amount.getValue());
    returnParams.put("scramble", engineController.scrambleEffect.amount.getValue());

    communicator.send("model", returnParams);
  }
}

class ClientCommunicator {
  Gson gson = new Gson();

  Server server;

  ClientCommunicator(Server server) {
    this.server = server;
  }

  void send(String method, Map params) {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("method", method);
    json.put("params", params);
    System.out.println("Response: " + gson.toJson(json));
    server.write(gson.toJson(json) + "\r\n");
  }

  void disconnectClient(Client client) {
    client.dispose();
    server.disconnect(client);
  }
}
