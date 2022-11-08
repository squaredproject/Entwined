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
  IPadServerController engineController;

  AppServer(LX lx, IPadServerController engineController) {
    this.lx = lx;
    this.engineController = engineController;
  }

  void start() {
    TSServer server = new TSServer(lx, 5204);

    ClientCommunicator clientCommunicator = new ClientCommunicator(server);
    ClientModelUpdater clientModelUpdater = new ClientModelUpdater(engineController, clientCommunicator);
    ClientTimerUpdater clientTimerUpdater = new ClientTimerUpdater(engineController, clientCommunicator);
    ParseClientTask parseClientTask = new ParseClientTask(engineController, server, clientModelUpdater, clientTimerUpdater);
    lx.engine.addLoopTask(parseClientTask);
  }

  class ParseClientTask implements LXLoopTask {
    Gson gson = new Gson();

    IPadServerController engineController;
    TSServer server;
    ClientModelUpdater clientModelUpdater;
    ClientTimerUpdater clientTimerUpdater;

    boolean hasActiveClients = false;

    ParseClientTask(IPadServerController engineController, TSServer server, ClientModelUpdater clientModelUpdater, ClientTimerUpdater clientTimerUpdater) {
      this.engineController = engineController;
      this.server = server;
      this.clientModelUpdater = clientModelUpdater;
      this.clientTimerUpdater = clientTimerUpdater;
    }

    // we want to watch for the last client that disconnects
    // this method is called when anything disconnects so we can try to do the right thing
    //
    //
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

    		if (false == engineController.isAutoplaying) {
          	engineController.setAutoplay(true);
          }
          engineController.setMasterBrightness(1.0);

          hasActiveClients = false;
    	}
    }

    public void loop(double deltaMs) {
      try {
        checkClientsAllDisconnected();
        TSClient client = server.available();
        if (client == null) return;
        hasActiveClients = true;

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
        else if (method.equals("resetTimerRun")) {
          engineController.autoPauseTask.pauseResetRunning();
        } else if (method.equals("resetTimerPause")) {
          engineController.autoPauseTask.pauseResetPaused();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  class ClientModelUpdater {
    IPadServerController engineController;
    ClientCommunicator communicator;

    ClientModelUpdater(IPadServerController engineController, ClientCommunicator communicator) {
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

      Map<String, Object> pauseParams = new HashMap<String, Object>();
      pauseParams.put("runSeconds", Config.pauseRunMinutes * 60.0 );
      pauseParams.put("pauseSeconds", Config.pausePauseMinutes * 60.0 );
      pauseParams.put("state",  engineController.autoPauseTask.pauseStateRunning() ? "run" : "pause");
      pauseParams.put("timeRemaining", engineController.autoPauseTask.pauseTimeRemaining() );
      returnParams.put("pauseTimer", pauseParams);

      communicator.send("model", returnParams);
    }
  }

  class ClientTimerUpdater {
    IPadServerController engineController;
    ClientCommunicator communicator;

    ClientTimerUpdater(IPadServerController engineController, ClientCommunicator communicator) {
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

      returnParams.put("runSeconds", Config.pauseRunMinutes * 60.0 );
      returnParams.put("pauseSeconds", Config.pausePauseMinutes * 60.0 );
      returnParams.put("state",  engineController.autoPauseTask.pauseStateRunning() ? "run" : "pause");
      returnParams.put("timeRemaining", engineController.autoPauseTask.pauseTimeRemaining() );

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
            parent.getClass().getMethod("serverEvent", Server.class, TSClient.class);
        } catch (Exception e) {
        	// this happens. No server events apparently
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



  /**
     * ( begin auto-generated from Client.xml )
     *
     * A client connects to a server and sends data back and forth. If anything
     * goes wrong with the connection, for example the host is not there or is
     * listening on a different port, an exception is thrown.
     *
     * ( end auto-generated )
   * @webref net
   * @brief The client class is used to create client Objects which connect to a server to exchange data.
   * @instanceName client any variable of type Client
   * @usage Application
   * @see_external LIB_net/clientEvent
   */
  class TSClient implements Runnable {

    protected static final int MAX_BUFFER_SIZE = 1 << 27; // 128 MB

    LX parent;
    Method clientEventMethod;
    Method disconnectEventMethod;

    volatile Thread thread;
    Socket socket;
    int port;
    String host;

    public InputStream input;
    public OutputStream output;

    final Object bufferLock = new Object[0];

    byte buffer[] = new byte[32768];
    int bufferIndex;
    int bufferLast;

    boolean disposeRegistered = false;


    /**
     * @param parent typically use "this"
     * @param host address of the server
     * @param port port to read/write from on the server
     */
    public TSClient(LX parent, String host, int port) {
      this.parent = parent;
      this.host = host;
      this.port = port;

      try {
        socket = new Socket(this.host, this.port);
        input = socket.getInputStream();
        output = socket.getOutputStream();

        thread = new Thread(this);
        thread.start();

  // can't hook into these, should LX have the ability to stop me?
  //      parent.registerMethod("dispose", this);
  //      disposeRegistered = true;

        // reflection to check whether host sketch has a call for
        // public void clientEvent(processing.net.Client)
        // which would be called each time an event comes in
        try {
          clientEventMethod =
            parent.getClass().getMethod("clientEvent", TSClient.class);
        } catch (Exception e) {
          System.out.println(" Client: parent has no client event method, ok ");
        }
        // do the same for disconnectEvent(Client c);
        try {
          disconnectEventMethod =
            parent.getClass().getMethod("disconnectEvent", TSClient.class);
        } catch (Exception e) {
          System.out.println(" Client: parent has no disconnect method, ok ");
        }

      } catch (IOException e) {
        e.printStackTrace();
        dispose();
      }
    }


    /**
     * @param socket any object of type Socket
     * @throws IOException
     */
    public TSClient(LX parent, Socket socket) throws IOException {
      this.parent = parent;
      this.socket = socket;

      input = socket.getInputStream();
      output = socket.getOutputStream();

      thread = new Thread(this);
      thread.start();

      // reflection to check whether host sketch has a call for
      // public void clientEvent(processing.net.Client)
      // which would be called each time an event comes in
      try {
        clientEventMethod =
            parent.getClass().getMethod("clientEvent", TSClient.class);
      } catch (Exception e) {
        // no such method, or an error.. which is fine, just ignore
      }
      // do the same for disconnectEvent(Client c);
      try {
        disconnectEventMethod =
          parent.getClass().getMethod("disconnectEvent", TSClient.class);
      } catch (Exception e) {
        // no such method, or an error.. which is fine, just ignore
      }
    }


    /**

     * Disconnects from the server. Use to shut the connection when you're
     * finished with the Client.
     *
     * @webref client:client
     * @brief Disconnects from the server
     * @usage application
     */
    public void stop() {
      if (disconnectEventMethod != null && thread != null){
        try {
          disconnectEventMethod.invoke(parent, this);
        } catch (Exception e) {
          Throwable cause = e;
          // unwrap the exception if it came from the user code
          if (e instanceof InvocationTargetException && e.getCause() != null) {
            cause = e.getCause();
          }
          cause.printStackTrace();
          disconnectEventMethod = null;
        }
      }
  //    if (disposeRegistered) {
  //      parent.unregisterMethod("dispose", this);
  //      disposeRegistered = false;
  //    }
      dispose();
    }


    /**
     * Disconnect from the server: internal use only.
     * <P>
     * This should only be called by the internal functions in PApplet,
     * use stop() instead from within your own applets.
     */
    public void dispose() {
      thread = null;
      try {
        if (input != null) {
          input.close();
          input = null;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        if (output != null) {
          output.close();
          output = null;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        if (socket != null) {
          socket.close();
          socket = null;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }


    @Override
    public void run() {
      byte[] readBuffer;
      { // make the read buffer same size as socket receive buffer so that
        // we don't waste cycles calling listeners when there is more data waiting
        int readBufferSize = 1 << 16; // 64 KB (default socket receive buffer size)
        try {
          readBufferSize = socket.getReceiveBufferSize();
        } catch (SocketException ignore) { }
        readBuffer = new byte[readBufferSize];
      }
      while (Thread.currentThread() == thread) {
        try {
          while (input != null) {
            int readCount;

            // try to read a byte using a blocking read.
            // An exception will occur when the sketch is exits.
            try {
              readCount = input.read(readBuffer, 0, readBuffer.length);
            } catch (SocketException e) {
               System.err.println("Client SocketException: " + e.getMessage());
               // the socket had a problem reading so don't try to read from it again.
               stop();
               return;
            }

            // read returns -1 if end-of-stream occurs (for example if the host disappears)
            if (readCount == -1) {
              System.err.println("Client got end-of-stream. SERVER");
              stop();
              return;
            }

            synchronized (bufferLock) {
              int freeBack = buffer.length - bufferLast;
              if (readCount > freeBack) {
                // not enough space at the back
                int bufferLength = bufferLast - bufferIndex;
                byte[] targetBuffer = buffer;
                if (bufferLength + readCount > buffer.length) {
                  // can't fit even after compacting, resize the buffer
                  // find the next power of two which can fit everything in
                  int newSize = Integer.highestOneBit(bufferLength + readCount - 1) << 1;
                  if (newSize > MAX_BUFFER_SIZE) {
                    // buffer is full because client is not reading (fast enough)
                    System.err.println("Client: can't receive more data, buffer is full. " +
                                           "Make sure you read the data from the client.");
                    stop();
                    return;
                  }
                  targetBuffer = new byte[newSize];
                }
                // compact the buffer (either in-place or into the new bigger buffer)
                System.arraycopy(buffer, bufferIndex, targetBuffer, 0, bufferLength);
                bufferLast -= bufferIndex;
                bufferIndex = 0;
                buffer = targetBuffer;
              }
              // copy all newly read bytes into the buffer
              System.arraycopy(readBuffer, 0, buffer, bufferLast, readCount);
              bufferLast += readCount;
            }

            // now post an event
            if (clientEventMethod != null) {
              try {
                clientEventMethod.invoke(parent, this);
              } catch (Exception e) {
                System.err.println("error, disabling clientEvent() for " + host);
                Throwable cause = e;
                // unwrap the exception if it came from the user code
                if (e instanceof InvocationTargetException && e.getCause() != null) {
                  cause = e.getCause();
                }
                cause.printStackTrace();
                clientEventMethod = null;
              }
            }
          }
        } catch (IOException e) {
          //errorMessage("run", e);
          e.printStackTrace();
        }
      }
    }


    /**
     * Returns true if this client is still active and hasn't run
     * into any trouble.
     * @webref client:client
     * @brief Returns true if this client is still active
     * @usage application
     */
    public boolean active() {
      return (thread != null);
    }


    /**
     * Returns the IP address of the computer to which the Client is attached.
     *
     * @webref client:client
     * @usage application
     * @brief Returns the IP address of the machine as a String
     */
    public String ip() {
      if (socket != null){
        return socket.getInetAddress().getHostAddress();
      }
      return null;
    }


    /**
     * Returns the number of bytes available. When any client has bytes
     * available from the server, it returns the number of bytes.
     *
     * @webref client:client
     * @usage application
     * @brief Returns the number of bytes in the buffer waiting to be read
     */
    public int available() {
      synchronized (bufferLock) {
        return (bufferLast - bufferIndex);
      }
    }


    /**
     * Empty the buffer, removes all the data stored there.
     *
     * ( end auto-generated )
     * @webref client:client
     * @usage application
     * @brief Clears the buffer
     */
    public void clear() {
      synchronized (bufferLock) {
        bufferLast = 0;
        bufferIndex = 0;
      }
    }


    /**
     * Returns a number between 0 and 255 for the next byte that's waiting in
     * the buffer. Returns -1 if there is no byte, although this should be
     * avoided by first cheacking <b>available()</b> to see if any data is available.
     *
     * ( end auto-generated )
     * @webref client:client
     * @usage application
     * @brief Returns a value from the buffer
     */
    public int read() {
      synchronized (bufferLock) {
        if (bufferIndex == bufferLast) return -1;

        int outgoing = buffer[bufferIndex++] & 0xff;
        if (bufferIndex == bufferLast) {  // rewind
          bufferIndex = 0;
          bufferLast = 0;
        }
        return outgoing;
      }
    }


    /**
     * Returns the next byte in the buffer as a char. Returns -1 or 0xffff if
     * nothing is there.
     *
     * ( end auto-generated )
     * @webref client:client
     * @usage application
     * @brief Returns the next byte in the buffer as a char
     */
    public char readChar() {
      synchronized (bufferLock) {
        if (bufferIndex == bufferLast) return (char) (-1);
        return (char) read();
      }
    }


    /**
     * Reads a group of bytes from the buffer. The version with no parameters
     * returns a byte array of all data in the buffer. This is not efficient,
     * but is easy to use. The version with the <b>byteBuffer</b> parameter is
     * more memory and time efficient. It grabs the data in the buffer and puts
     * it into the byte array passed in and returns an int value for the number
     * of bytes read. If more bytes are available than can fit into the
     * <b>byteBuffer</b>, only those that fit are read.
     *
     * ( end auto-generated )
     * <h3>Advanced</h3>
     * Return a byte array of anything that's in the serial buffer.
     * Not particularly memory/speed efficient, because it creates
     * a byte array on each read, but it's easier to use than
     * readBytes(byte b[]) (see below).
     *
     * @webref client:client
     * @usage application
     * @brief Reads everything in the buffer
     */
    public byte[] readBytes() {
      synchronized (bufferLock) {
        if (bufferIndex == bufferLast) return null;

        int length = bufferLast - bufferIndex;
        byte outgoing[] = new byte[length];
        System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

        bufferIndex = 0;  // rewind
        bufferLast = 0;
        return outgoing;
      }
    }


    /**
     * <h3>Advanced</h3>
     * Return a byte array of anything that's in the serial buffer
     * up to the specified maximum number of bytes.
     * Not particularly memory/speed efficient, because it creates
     * a byte array on each read, but it's easier to use than
     * readBytes(byte b[]) (see below).
     *
     * @param max the maximum number of bytes to read
     */
    public byte[] readBytes(int max) {
      synchronized (bufferLock) {
        if (bufferIndex == bufferLast) return null;

        int length = bufferLast - bufferIndex;
        if (length > max) length = max;
        byte outgoing[] = new byte[length];
        System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

        bufferIndex += length;
        if (bufferIndex == bufferLast) {
          bufferIndex = 0;  // rewind
          bufferLast = 0;
        }

        return outgoing;
      }
    }


    /**
     * <h3>Advanced</h3>
     * Grab whatever is in the serial buffer, and stuff it into a
     * byte buffer passed in by the user. This is more memory/time
     * efficient than readBytes() returning a byte[] array.
     *
     * Returns an int for how many bytes were read. If more bytes
     * are available than can fit into the byte array, only those
     * that will fit are read.
     *
     * @param bytebuffer passed in byte array to be altered
     */
    public int readBytes(byte bytebuffer[]) {
      synchronized (bufferLock) {
        if (bufferIndex == bufferLast) return 0;

        int length = bufferLast - bufferIndex;
        if (length > bytebuffer.length) length = bytebuffer.length;
        System.arraycopy(buffer, bufferIndex, bytebuffer, 0, length);

        bufferIndex += length;
        if (bufferIndex == bufferLast) {
          bufferIndex = 0;  // rewind
          bufferLast = 0;
        }
        return length;
      }
    }


    /**
     * ( begin auto-generated from Client_readBytesUntil.xml )
     *
     * Reads from the port into a buffer of bytes up to and including a
     * particular character. If the character isn't in the buffer, 'null' is
     * returned. The version with no <b>byteBuffer</b> parameter returns a byte
     * array of all data up to and including the <b>interesting</b> byte. This
     * is not efficient, but is easy to use. The version with the
     * <b>byteBuffer</b> parameter is more memory and time efficient. It grabs
     * the data in the buffer and puts it into the byte array passed in and
     * returns an int value for the number of bytes read. If the byte buffer is
     * not large enough, -1 is returned and an error is printed to the message
     * area. If nothing is in the buffer, 0 is returned.
     *
     * ( end auto-generated )
     * @webref client:client
     * @usage application
     * @brief Reads from the buffer of bytes up to and including a particular character
     * @param interesting character designated to mark the end of the data
     */
    public byte[] readBytesUntil(int interesting) {
      byte what = (byte)interesting;

      synchronized (bufferLock) {
        if (bufferIndex == bufferLast) return null;

        int found = -1;
        for (int k = bufferIndex; k < bufferLast; k++) {
          if (buffer[k] == what) {
            found = k;
            break;
          }
        }
        if (found == -1) return null;

        int length = found - bufferIndex + 1;
        byte outgoing[] = new byte[length];
        System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

        bufferIndex += length;
        if (bufferIndex == bufferLast) {
          bufferIndex = 0; // rewind
          bufferLast = 0;
        }
        return outgoing;
      }
    }


    /**
     * <h3>Advanced</h3>
     * Reads from the serial port into a buffer of bytes until a
     * particular character. If the character isn't in the serial
     * buffer, then 'null' is returned.
     *
     * If outgoing[] is not big enough, then -1 is returned,
     *   and an error message is printed on the console.
     * If nothing is in the buffer, zero is returned.
     * If 'interesting' byte is not in the buffer, then 0 is returned.
     *
     * @param byteBuffer passed in byte array to be altered
     */
    public int readBytesUntil(int interesting, byte byteBuffer[]) {
      byte what = (byte)interesting;

      synchronized (bufferLock) {
        if (bufferIndex == bufferLast) return 0;

        int found = -1;
        for (int k = bufferIndex; k < bufferLast; k++) {
          if (buffer[k] == what) {
            found = k;
            break;
          }
        }
        if (found == -1) return 0;

        int length = found - bufferIndex + 1;
        if (length > byteBuffer.length) {
          System.err.println("readBytesUntil() byte buffer is" +
                             " too small for the " + length +
                             " bytes up to and including char " + interesting);
          return -1;
        }
        //byte outgoing[] = new byte[length];
        System.arraycopy(buffer, bufferIndex, byteBuffer, 0, length);

        bufferIndex += length;
        if (bufferIndex == bufferLast) {
          bufferIndex = 0;  // rewind
          bufferLast = 0;
        }
        return length;
      }
    }


    /**
     * ( begin auto-generated from Client_readString.xml )
     *
     * Returns the all the data from the buffer as a String. This method
     * assumes the incoming characters are ASCII. If you want to transfer
     * Unicode data, first convert the String to a byte stream in the
     * representation of your choice (i.e. UTF8 or two-byte Unicode data), and
     * send it as a byte array.
     *
     * ( end auto-generated )
     * @webref client:client
     * @usage application
     * @brief Returns the buffer as a String
     */
    public String readString() {
      byte b[] = readBytes();
      if (b == null) return null;
      return new String(b);
    }


    /**
     * ( begin auto-generated from Client_readStringUntil.xml )
     *
     * Combination of <b>readBytesUntil()</b> and <b>readString()</b>. Returns
     * <b>null</b> if it doesn't find what you're looking for.
     *
     * ( end auto-generated )
     * <h3>Advanced</h3>
     * <p/>
     * If you want to move Unicode data, you can first convert the
     * String to a byte stream in the representation of your choice
     * (i.e. UTF8 or two-byte Unicode data), and send it as a byte array.
     *
     * @webref client:client
     * @usage application
     * @brief Returns the buffer as a String up to and including a particular character
     * @param interesting character designated to mark the end of the data
     */
    public String readStringUntil(int interesting) {
      byte b[] = readBytesUntil(interesting);
      if (b == null) return null;
      return new String(b);
    }


    /**
     * ( begin auto-generated from Client_write.xml )
     *
     * Writes data to a server specified when constructing the client.
     *
     * ( end auto-generated )
     * @webref client:client
     * @usage application
     * @brief  	Writes bytes, chars, ints, bytes[], Strings
     * @param data data to write
     */
    public void write(int data) {  // will also cover char
      try {
        output.write(data & 0xff);  // for good measure do the &
        output.flush();   // hmm, not sure if a good idea

      } catch (Exception e) { // null pointer or serial port dead
        //errorMessage("write", e);
        //e.printStackTrace();
        //dispose();
        //disconnect(e);
        e.printStackTrace();
        stop();
      }
    }


    public void write(byte data[]) {
      try {
        output.write(data);
        output.flush();   // hmm, not sure if a good idea

      } catch (Exception e) { // null pointer or serial port dead
        //errorMessage("write", e);
        //e.printStackTrace();
        //disconnect(e);
        e.printStackTrace();
        stop();
      }
    }


    /**
     * <h3>Advanced</h3>
     * Write a String to the output. Note that this doesn't account
     * for Unicode (two bytes per char), nor will it send UTF8
     * characters.. It assumes that you mean to send a byte buffer
     * (most often the case for networking and serial i/o) and
     * will only use the bottom 8 bits of each char in the string.
     * (Meaning that internally it uses String.getBytes)
     *
     * If you want to move Unicode data, you can first convert the
     * String to a byte stream in the representation of your choice
     * (i.e. UTF8 or two-byte Unicode data), and send it as a byte array.
     */
    public void write(String data) {
      write(data.getBytes());
    }
  }
}
