import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.ZoneId;

// the jar we have doesn't have an emitter class, but it does have On and Manager and io.socket.parser.Parser


import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.client.IO;
import java.net.URI;


import org.json.JSONException;
import org.json.JSONObject;


//
// Introducing... Canopy!
// the point of Canopy is to allow interactivity from web pages.
// LX will connect, over 'socket.io', to Canopy. The canopy server
// will then send commands, which it gathers from web pages.
// The canopy server is delagated with security ( like checking location )
// Since Socket.io is realtime (we'll see!), and bicirectional, the Canopy
// server will be able to send commands like "turn shrub 5 red for 10 seconds"
// and similar.
//
// Threading: since LX likes to run on its own thread, we'll need to use a queue between them,
// which will be created by the caller and use passed in (had to do something similar for Crown...)
//
//
// PLEASE NOTE ABOUT SOCKET.IO. There are a number of repos on github that claim to be the
// One True Canonical, but the one that _is_ is at https://github.com/socketio/socket.io-client-java
//
// There is no example code or anything to speak of there.
// https://socketio.github.io/socket.io-client-java/installation.html
//
// Basic steps: connect. After you connect, you have a socket object. You can register for events like
// io.on("connection", (socket) => ")... which says you're a server an on a connection event call the lambda
//
// Once you have a connection, there are only two primitives, asend with socket.emit("hello", "world");
// or you receve events with socket.on("eventType", new Emitter.Listener() ...
// that's it. According to the "contract", if an event is emitted while disconnected,
// they will be buffered. Problably, from Canopy to Lx, we should probably have a timestamp
// that we can discard if too old

class CanopyController {

  boolean enabled = false;
  Runnable canopyRunnable;
  Thread  canopyThread;
  Socket socket;

  Engine engine; // gives us access to the InteractiveFilter
  InteractiveFilterEffect interactiveFilterEffect;

  CanopyController(Engine engine) {

  	if (Config.canopyServer == "") {
  		enabled = false;
  		return;
  	}

  	this.engine = engine;
  	this.interactiveFilterEffect = engine.interactiveFilterEffect;

  	canopyRunnable = new Runnable() {

	  	@Override
	  	public void run() {
	  		System.out.println(" CanopyController thread ");

	  		URI uri = URI.create(Config.canopyServer);

		    IO.Options opts = IO.Options.builder()
		    	.setReconnection(true)
		    	.build();

		    socket = IO.socket(uri, opts);

		    socket.connect();

		    socket.on("interactionStarted", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		System.out.println(" interactionStarted from Canopy ");
		    		for (Object o : args) {
		   				System.out.println(o);
		   			}
		    	}
		    });

		    // these receive the ID of the shrub, type string, with the integer in it
		    socket.on("interactionStopped", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		System.out.println(" interactionStopped from Canopy ");
		    		if (args[0] instanceof String) {
		    			stopShrubInteraction((String)args[0]);
		    		}
		    		else if (args[0] instanceof JSONObject) {
			    		stopShrubInteraction((JSONObject) args[0]);
			    	}
//		    		for (Object o : args) {
//		   				System.out.println(o+" Type: "+o.getClass());
//		   			}
		    	}
		    });

		    socket.on("updateShrubSetting", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		System.out.println(" updateShrubSetting from Canopy: argslen "+args.length);
		    		//updateShrubSetting((JSONObject) args[0]);
		    		for (Object o : args) {
		   				System.out.println(o+" Type: "+o.getClass());
		   			}
		    	}
		    });

		    socket.on("runOneShotTriggerable", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		System.out.println(" runOneShotTriggerable from Canopy ");
		    		for (Object o : args) {
		   				System.out.println(o);
		   			}
		    	}
		    });

		    socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		System.out.println(" socket connect event id "+socket.id() );
		    	}
		    });

		    socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
		    	@Override
		   		public void call(Object... args) {
		   			System.out.println(" socket disconnect event ");
		   			// This kind of disconnect is the underlying socket that shouldn't lose
		   			// messages, so we're not going to disable until we see a connect
		   			// error (below)
					//interactiveFilterEffect.disableAll();
		   			//for (Object o : args) {
		   			//	System.out.println(o);
		   			//}
		   		}
		    });

		    socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
		    	@Override
		   		public void call(Object... args) {
		   			System.out.println(" socket connect error of unknown type, will disable current effects and immediately try reconnect ");
		   			interactiveFilterEffect.disableAll();
		   			socket.connect();
		   			for (Object o : args) {
		   				System.out.println(o);
		   			}
		   		}
		    });


		    while (true) {
		    	try {
		    	    Thread.sleep(60000);
		        } catch (Exception e) {
		        	System.out.println(" CanopyThreadSleepException: "+e);
		        }
		    	System.out.println("CanopyController connect state: "+ socket.connected() );
		    }

		} /* run */
	}; /* runnable */

	canopyThread = new Thread(canopyRunnable);
	canopyThread.start();

  }

  void updateShrubSetting(JSONObject o) {
  	//System.out.println("UpdateShrubSetting: object "+o);

  	try {
	  	int shrubId = o.getInt("shrubId");

	  	if (o.has("hue")) {
	  		int hue = o.getInt("hue");
	  		//System.out.println(" going to set hue to "+hue+" for shrub "+shrubId);
	  		interactiveFilterEffect.setShrubHue(shrubId,(float)hue);
	  	}
	} catch (Exception e) {
		System.out.println(" updateShrubSettingException "+e);
	}

  }

  void stopShrubInteraction(JSONObject o) {
  	//System.out.println("stopShrubInteraction: object "+o);

  	try {
	  	int shrubId = o.getInt("shrubId");

	  	interactiveFilterEffect.disableShrub(shrubId);

	} catch (Exception e) {
		System.out.println(" stopShrubInteraction(JSON): Exception "+e);
	}

  }

  void stopShrubInteraction(String s) {
  	//System.out.println("stopShrubInteraction: object "+o);

  	try {
	  	int shrubId = Integer.parseInt(s);

	  	interactiveFilterEffect.disableShrub(shrubId);

	} catch (Exception e) {
		System.out.println(" stopShrubInteraction(String): Exception "+e);
	}

  }


  // call this at startup to get the run/pause/whatever
  // and if there's a change to the "state"
  // and if the sculpture goes non-interactive

  public void 
  modelUpdate(boolean interactive, int runSeconds, int pauseSeconds, String state, ZonedDateTime nextTransition) {

  	// convert date to something pleasant
  	// THIS IS PROBABLY WRONG because it won't have timezone (Z).
  	// we probably need to have a ZonedDateTime and ask for it in Zulu time.
  	String nextTransition_str = nextTransition.format(DateTimeFormatter.ISO_DATE_TIME);

  	// make json object
  	JSONObject modelUpdateObj = new JSONObject();
  	try {
	  	modelUpdateObj.put("interactivityEnabled", interactive);
	  	JSONObject breakTimer = new JSONObject();
	  	breakTimer.put("runSeconds",runSeconds);
	  	breakTimer.put("pauseSeconds",pauseSeconds);
	  	breakTimer.put("state",state);
	  	breakTimer.put("nextStateChangeDate",nextTransition_str);
	  	modelUpdateObj.put("breakTimer",breakTimer);

	} catch (Exception e) {
		System.out.println(" could not create json object for model Update "+e);
		return;
	}
		  	String modelUpdate_str = modelUpdateObj.toString();
  	System.out.println(" Model Update String: " + modelUpdate_str);

  	// send to other end
  	while (socket == null) {
  		try {
  			Thread.sleep(100 /* milliseconds */);
  		} catch (Exception e) {
  			;
  		}
  	}
  	socket.emit("modelUpdated", modelUpdate_str);

  }



} // CanopyConnector
