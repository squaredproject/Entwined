package entwined.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.mixer.LXAbstractChannel;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.pattern.LXPattern;


/*
 * IPadServer. Handles communication between the iPad and the iPadController.
 * This module handles the connectivity with the iPad (or other device speaking the
 * iPad protocol), vectors commands from the iPad to the appropriate endpoint
 * on the iPadController. It also communicates the capabilities of the iPadController to
 * the iPad itself.
 */

class AppServer {
  LX lx;
  EngineController engineController;
  TSServer server;
  ParseClientTask parseClientTask;

  AppServer(LX lx, EngineController engineController) {
    this.lx = lx;
    this.engineController = engineController;
  }

  void start() {
    server = new TSServer(lx, 5204);

    ClientCommunicator clientCommunicator = new ClientCommunicator(server);
    ClientModelUpdater clientModelUpdater = new ClientModelUpdater(engineController, clientCommunicator);
    ClientTimerUpdater clientTimerUpdater = new ClientTimerUpdater(engineController, clientCommunicator);
    parseClientTask = new ParseClientTask(engineController, server, clientModelUpdater, clientTimerUpdater);
    lx.engine.addLoopTask(parseClientTask);
  }

  public void shutdown() {
    lx.engine.removeLoopTask(parseClientTask);
    server.stop();
  }

  class ParseClientTask implements LXLoopTask {
    Gson gson = new Gson();

    EngineController engineController;
    TSServer server;
    ClientModelUpdater clientModelUpdater;
    ClientTimerUpdater clientTimerUpdater;

    boolean hasActiveClients = false;

    ParseClientTask(EngineController engineController, TSServer server, ClientModelUpdater clientModelUpdater, ClientTimerUpdater clientTimerUpdater) {
      this.engineController = engineController;
      this.server = server;
      this.clientModelUpdater = clientModelUpdater;
      this.clientTimerUpdater = clientTimerUpdater;
    }

    // we want to watch for the last client that disconnects
    // this method is called at the beginning of the main parse loop.
    public void checkClientsAllDisconnected() {

      if (hasActiveClients) {
        for (TSClient client : server.clients) {
          if (client.active()) {
            return; // there's an active one
          }
        }

        if ( server.clients.size() != 0)
          return;

        System.out.println(" detected all clients disconnected, forcing autoplay ");

        hasActiveClients = false;
      }
    }


    public void enableAutoplay() {
      if (false == engineController.isAutoplaying) {
        engineController.setAutoplay(true);
      }
      engineController.setMasterBrightness(1.0);
    }


    public void loop(double deltaMs) {
      try {
        // If the client has just disconnected, turn on autoplay
        boolean hadActiveClients = hasActiveClients;
        checkClientsAllDisconnected();
        if (hadActiveClients && !hasActiveClients) {
          enableAutoplay();
        }

        // Check for incoming requests
        TSClient client = server.available();
        if (client == null) return;

        hasActiveClients = true;

        // Read incoming request
        String whatClientSaid = client.readStringUntil('\n');
        if (whatClientSaid == null) return;

        // System.out.print("Request: " + whatClientSaid);

        Map<String, Object> message = null;
        try {
          message = gson.fromJson(whatClientSaid.trim(), new TypeToken<Map<String, Object>>() {}.getType());
        } catch (Exception e) {
          System.out.println(e);
          System.out.println("Server Exception: Got: " + message);
          return;
        }

        if (message == null) return;

        // Vector message to receivers. Most messages will get sent to the enginecontroller,
        // which can actually change things in the system.
        // Requests for status, such as 'loadModel' and 'getTimer'
        // are served by the ClientModelUpdater and the ClientTimerUpdater, which
        // for all intents and purposes, are just functions.
        // (Note that I do not see any provision for handling multiple simultaneous clients. Maybe state of
        // which client you're addressing is held in the TSServer; maybe everything explodes if you attempt
        // two simultaneous connections. Not that two simultaneous connections necessarily a good thing.)
        String method = (String)message.get("method");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>)message.get("params");

        if (method == null) return;
        if (params == null) params = new HashMap<String, Object>();

        if (method.equals("loadModel")) {
          clientModelUpdater.sendModel();
        } else if (method.equals("setAutoplay")) {
          Boolean autoplay = (Boolean)params.get("autoplay");
          if (autoplay == null) return;
          engineController.setAutoplay(autoplay.booleanValue());
        } else if (method.equals("setBrightness")) {
          Double brightness = (Double)params.get("brightness");
          if (brightness == null) return;
          engineController.setMasterBrightness(brightness);
        } else if (method.equals("setAutoplayBrightness")) {
          Double autoplayBrightness = (Double)params.get("autoplayBrightness");
          if (autoplayBrightness == null) return;
          engineController.setAutoplayBrightness(autoplayBrightness);
        } else if (method.equals("setHue")) {
          Double hue = (Double)params.get("hue");
          if (hue == null) return;
          engineController.setHue(hue);
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
        /* } else if (method.equals("setSpin")) {
          Double amount = (Double)params.get("amount");
          if (amount == null) return;
          engineController.setSpin(amount); */
        } else if (method.equals("setBlur")) {
          Double amount = (Double)params.get("amount");
          if (amount == null) return;
          engineController.setBlur(amount);
        } else if (method.equals("setScramble")) {
          Double amount = (Double)params.get("amount");
          if (amount == null) return;
          engineController.setScramble(amount);
        } else if (method.equals("getTimer")) {
          clientTimerUpdater.sendTimer();
        }
        else if (method.equals("resetTimerRun")) {  // No longer supporting autopause
          // engineController.autoPauseTask.pauseResetRunning();
        } else if (method.equals("resetTimerPause")) {
          // engineController.autoPauseTask.pauseResetPaused();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  /*
   * Client model updater
   * Invoked when an iPad when a client explicitly makes a
   * 'loadmodel'request (which it presumably does when it attaches)
   * Sends the current state of client-controllable features, including:
   *  -- AutoplayState
   *  -- BrightnessLevels
   *  -- Patterns on addressable channels (8-11)
   *  -- Effects registered for iPad usage
   *  -- Values of global effects - speed, blur, scramble, spin
   *  -- Pause/run state information
   *  (This is basically a function masquerading as a class.)
   */

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
      returnParams.put("brightness", engineController.getMasterBrightness());
      //returnParams.put("hue", engineController.hueEffect.amount.getValue());

      List<Map <String, Object>> channelsParams = new ArrayList<Map <String, Object>>(engineController.numServerChannels);
      for (LXAbstractChannel abstractChannel : engineController.getChannels()) {
        if (abstractChannel instanceof LXChannel) {
          LXChannel channel = (LXChannel)abstractChannel;
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

          List<Map <String, Object>> patternsParams = new ArrayList<Map<String, Object>>(channel.getPatterns().size());
          for (int i = 1; i < channel.getPatterns().size(); i++) {  // NB - intentionally skipping id 0, which is NoPattern
            LXPattern pattern = channel.getPatterns().get(i);
            Map<String, Object> patternParams = new HashMap<String, Object>();
            patternParams.put("name", pattern.getLabel());
            patternParams.put("index", i-1);
            patternsParams.add(patternParams);
          }
          channelParams.put("patterns", patternsParams);

          channelsParams.add(channelParams);
        }
      }
      returnParams.put("channels", channelsParams);

      List<Map<String, Object>> effectsParams = new ArrayList<Map<String,Object>>(engineController.effectControllers.size());
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
      // returnParams.put("spin", engineController.spinEffect.spin.getValue());
      returnParams.put("blur", engineController.blurEffect.level.getValue());
      returnParams.put("scramble", engineController.scrambleEffect.getAmount());

      // NB - Pause params really not used any more. Keeping this data here because client
      // may rely on it. And who knows, maybe one day we'll want to use the interface again.
      Map<String, Object> pauseParams = new HashMap<String, Object>();
      pauseParams.put("runSeconds", Config.pauseRunMinutes * 60.0 );
      pauseParams.put("pauseSeconds", Config.pausePauseMinutes * 60.0 );
      pauseParams.put("state",  "run");
      pauseParams.put("timeRemaining", 60);
      returnParams.put("pauseTimer", pauseParams);

      communicator.send("model", returnParams);
    }
  }

  /*
   * Update attached iPad client about the current run/pause state.
   * Called when the client makes a 'loadtimer' request.
   *
   * (Like ClientModelUpdater, this is largely a function masquerading as a class)
   */
  class ClientTimerUpdater {
    EngineController engineController;
    ClientCommunicator communicator;

    ClientTimerUpdater(EngineController engineController, ClientCommunicator communicator) {
      this.engineController = engineController;
      this.communicator = communicator;
    }

    // client can request the current status of the "timers"
    // there are 4 parts:
    // what mode "paused" or "running"
    // 3 times: period of run, period of pause (these are mostly the same all the time)
    // time remaining in period (changes rapidly)

    void sendTimer() {
      Map<String, Object> returnParams = new HashMap<String, Object>();

      // See comment about AutoPause in sendModel.
      returnParams.put("runSeconds", Config.pauseRunMinutes * 60.0 );
      returnParams.put("pauseSeconds", Config.pausePauseMinutes * 60.0 );
      returnParams.put("state",  "run");
      returnParams.put("timeRemaining", 60 );

      communicator.send("pauseTimer", returnParams);
    }
  }


  class ClientCommunicator {
    Gson gson = new Gson();

    TSServer server;

    ClientCommunicator(TSServer server) {
      this.server = server;
    }

    void send(String method, Map<String, Object> params) {
      Map<String, Object> json = new HashMap<String, Object>();
      json.put("method", method);
      json.put("params", params);
      // System.out.println("Response: " + gson.toJson(json));
      server.writeAll(gson.toJson(json) + "\r\n");
    }

    void disconnectClient(TSClient client) {
      System.out.println("Disconnect Client: ");
      client.dispose();
      server.disconnect(client);
    }
  }

  /*
  ** This is a Server class originally from processing, but it doesn't work
  ** well. Thus, pulling it in, and seeing to make it better. This shouldn't
  ** be hard, we're just reading from TCP connections until we get a NL
  **
  ** BB- no idea yet what the best parent is. LX? LXEngine? One of those
  ** will have stop methods.
  ** CSW - is there an issue with keeping it here, especially since we no longer control lx or lxengine?
  */


  class TSServer implements Runnable {
    LX parent;
    Method serverEventMethod;

    volatile Thread thread;
    ServerSocket server;
    int port;

    /** Array of client objects, useful length is determined by clientCount. */
    public List<TSClient> clients;

    /**
     * @param parent typically use "this"
     * @param port port used to transfer data
     */
    public TSServer(LX parent, int port) {
      this(parent, port, null);
    }

    /**
     * @param host when multiple NICs are in use, the ip (or name) to bind from
     */
    public TSServer(LX parent, int port, String host) {
      this.parent = parent;
      this.port = port;

      try {
        if (host == null) {
          server = new ServerSocket(this.port);
        } else {
          server = new ServerSocket(this.port, 10, InetAddress.getByName(host));
        }

        // needs to be thread safe I think?
        clients = Collections.synchronizedList(new ArrayList<TSClient>(10));

        thread = new Thread(this);
        thread.start();

        // this is a way to get the parent to dispose automagically.
        // better to actually have the owner call close...
        // parent.registerMethod("dispose", this);

        // reflection to check whether host applet has a call for
        // public void serverEvent(Server s, Client c);
        // which is called when a new guy connects
        try {
          serverEventMethod =
            parent.getClass().getMethod("serverEvent", TSServer.class, TSClient.class);
        } catch (Exception e) {
          // At the moment, this is the standard path. This code was brought over
          // from Processing and heavily modified; we do not use the serverEventMethod
          // functionality.  CSW - 11/2022
          //System.out.println("server create: no server event on this object");
        }

      } catch (IOException e) {
        //e.printStackTrace();
        thread = null;
        throw new RuntimeException(e);
        //errorMessage("<init>", e);
      }
    }


    /**
     * Disconnect a particular client.
     *
     * @brief Disconnect a particular client.
     * @webref server:server
     * @param client the client to disconnect
     */
    public void disconnect(TSClient client) {
      client.stop();
    clients.remove(client);
    }

    protected void disconnectAll() {
        for (TSClient client : clients) {
          try {
            client.stop();
          } catch (Exception e) {
            // ignore
          }
        }
        clients.clear();
      }


    /**
     * Returns true if this server is still active and hasn't run
     * into any trouble.
     *
     * @webref server:server
     * @brief Return true if this server is still active.
     */
    public boolean active() {
      return thread != null;
    }


    static public String ip() {
      try {
        return InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
        e.printStackTrace();
        return null;
      }
    }


    // the last index used for available. can't just cycle through
    // the clients in order from 0 each time, because if client 0 won't
    // shut up, then the rest of the clients will never be heard from.
    // Breadcrumb. Fairness not as important as stability. The thread locking
    // wasn't right. Let's just use a standard java primitive.

    /**
     * Returns the next client in line with a new message.
     * and oh by the way reaps out inactive clients
     *
     * @brief Returns the next client in line with a new message.
     * @webref server
     * @usage application
     */
    public TSClient available() {
      ArrayList<TSClient> deletes = null;
      TSClient av = null;
        // the fairness thing is cute, but let's let it alone
        for (TSClient client : clients) {
          //Check for valid client
          if (!client.active()){
            if (deletes == null) deletes = new ArrayList<TSClient>();
            deletes.add(client);
          }
          if (client.available() > 0) {
            av = client;
            break;
          }
        }

        if (null != deletes) {
          for (TSClient client : deletes) {
            client.stop();
            clients.remove(client);
          }
        }

      return av;
    }


    /**
     * Disconnects all clients and stops the server.
    *
     * Use this to shut down the server if you finish using it while your applet
     * is still running. Otherwise, it will be automatically be shut down by the
     * host PApplet using dispose(), which is identical.

     * @brief Disconnects all clients and stops the server.
     * @webref server
     * @usage application
     */
    public void stop() {
      dispose();
    }


    /**
     * Disconnect all clients and stop the server: internal use only.
     */
    public void dispose() {
      thread = null;

      if (clients != null) {
        disconnectAll();
        clients = null;
      }

      try {
        if (server != null) {
          server.close();
          server = null;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


    @Override
    public void run() {
      while (Thread.currentThread() == thread) {
        try {
          Socket socket = server.accept();
          TSClient client = new TSClient(parent, socket);
          clients.add(client);
          if (serverEventMethod != null) {
            try {
              serverEventMethod.invoke(parent, this, client);
            } catch (Exception e) {
              System.err.println("Disabling serverEvent() for port " + port);
              Throwable cause = e;
              // unwrap the exception if it came from the user code
              if (e instanceof InvocationTargetException && e.getCause() != null) {
                cause = e.getCause();
              }
              cause.printStackTrace();
              serverEventMethod = null;
            }
          }
        } catch (SocketException e) {
          //thrown when server.close() is called and server is waiting on accept
          System.err.println("Server SocketException: " + e.getMessage());
          thread = null;
        } catch (IOException e) {
          //errorMessage("run", e);
          e.printStackTrace();
          thread = null;
        }
      }
    }


    /**
     * Writes a value to all the connected clients. It sends bytes out from the
     * Server object. writeAll is the common metaphore because you want to send
     * all the parameter changes to all the clients.
     *
     * @webref server
     * @brief Writes data to all connected clients
     * @param data data to write
     */
    public void writeAll(int data) {  // will also cover char
      ArrayList<TSClient> deletes = null;
    for (TSClient client : clients) {
        if (client.active()) {
          client.write(data);
        } else {
          if (deletes == null) deletes = new ArrayList<TSClient>(0);
          deletes.add(client);
        }
      }

      if (null != deletes) {
        for (TSClient client : deletes ) {
          client.stop();
          clients.remove(client);
        }
    }
    }


    public void writeAll(byte data[]) {
      ArrayList<TSClient> deletes = null;
    for (TSClient client : clients) {
        if (client.active()) {
          client.write(data);
        } else {
          if (deletes == null) deletes = new ArrayList<TSClient>(0);
          deletes.add(client);
        }
      }

      if (null != deletes) {
        for (TSClient client : deletes ) {
          client.stop();
          clients.remove(client);
        }
    }
    }


    public void writeAll(String data) {
      ArrayList<TSClient> deletes = null;
    for (TSClient client : clients) {
        if (client.active()) {
          client.write(data);
        } else {
          if (deletes == null) deletes = new ArrayList<TSClient>(0);
          deletes.add(client);
        }
      }

      if (null != deletes) {
        for (TSClient client : deletes ) {
          client.stop();
          clients.remove(client);
        }
    }
    }

  }



}
