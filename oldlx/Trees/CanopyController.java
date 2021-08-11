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
// that we can discard if too old or out of order, but socket.io is supposed to help us

class CanopyController {

  boolean enabled = false; // not that we're really using this, but I feel better
  Runnable canopyRunnable;
  Thread  canopyThread;
  Socket socket;

  final Engine engine; // gives us access to the InteractiveEffects

  CanopyController(Engine engine) {

  	this.engine = engine;

  	if (Config.canopyServer == "") {
  		enabled = false;
  		return;
  	}
  	enabled = true;

	final CanopyController self = this;

  	canopyRunnable = new Runnable() {

		  // Log Helper
	  	final ZoneId localZone = ZoneId.of("America/Los_Angeles");
		 void log(String s) {
		  	 System.out.println(
		  		ZonedDateTime.now( localZone ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) + " " + s );
		}

	  	@Override
	  	public void run() {
	  		log(" CanopyController thread start ");

	  		URI uri = URI.create(Config.canopyServer);

		    IO.Options opts = IO.Options.builder()
		    	.setReconnection(true)
		    	.build();

		    socket = IO.socket(uri, opts);

		    socket.connect();

		    socket.on("interactionStarted", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		try {
			    		log(" interactionStarted from Canopy " + args[0].toString());
	//		    		for (Object o : args) {
	//		   				log(o.toString());
	//		   			}
		    		} catch (Exception e) {
		    			log(" socket: interaction Started threw error "+e);
		    		}
		    	}
		    });

		    // these receive the ID of the shrub, type string, with the integer in it
		    socket.on("interactionStopped", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		log(" interactionStopped from Canopy ");
		    		try {
			    		if (args[0] instanceof String) {
			    			log(" interactionStopped from Canopy shrub "+(String)args[0]);
			    			stopShrubInteraction((String)args[0]);
			    		}
			    		else if (args[0] instanceof JSONObject) {
			    			log(" interactionStopped from Canopy shrub "+(JSONObject)args[0]);
				    		stopShrubInteraction((JSONObject) args[0]);
				    	}
					} catch (Exception e) {
						log(" socket: Interaction Stopped threw error "+e);
					}
		    	}
		    });

		    socket.on("updateShrubSetting", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		try {
			    		//log(" updateShrubSetting from Canopy: argslen "+args.length);
			    		updateShrubSetting((JSONObject) args[0]);
			    	} catch (Exception e) {
			    		log(" socket: updateShrubSetting threw error "+e);
			    	}
		    	}
		    });

		    socket.on("runOneShotTriggerable", new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		log(" runOneShotTriggerable from Canopy ");
		    		try {
		    			runOneShotTriggerable((JSONObject)args[0]);
		    		} catch (Exception e) {
		    			log(" socket: run one shot triggerable threw "+e);
		    		}
		    	}
		    });

		    socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
		    	@Override
		    	public void call(Object... args) {
		    		log(" socket connect event id "+socket.id() );

					try {
						ZonedDateTime firstPause = ZonedDateTime.now();
						firstPause.plusSeconds( (int) (Config.pauseRunMinutes * 60.0) );
						self.modelUpdate(true /*interactive*/, (int) (Config.pauseRunMinutes * 60.0f) /*runSeconds*/,
							(int) (Config.pausePauseMinutes * 60.0f) /*pauseSeconds*/,"run" /*state*/,firstPause);
					} catch (Exception e) {
						log(" socket: attempting to modelUpdate threw " + e);
					}
		    	}
		    });

		    socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
		    	@Override
		   		public void call(Object... args) {
		   			log(" socket disconnect event ");
		   			// This kind of disconnect is the underlying socket that shouldn't lose
		   			// messages, so we're not going to disable until we see a connect
		   			// error (below)
		   			try {
			   			for (Object o : args) {
			   				log("SocketEventDisconnectArgs: "+o.toString() );
			   			}
			   		} catch (Exception e) {
			   			log("EventDisconnect: Exception: "+e);
			   		}
		   		}
		    });

		    socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
		    	@Override
		   		public void call(Object... args) {
		   			try {
			   			log(" socket connect error of unknown type, will disable current effects and immediately try reconnect ");
			   			for (Object o : args) {
			   				log("ConnectErrorArgs: "+o.toString() );
			   			}
			   			onSocketConnectError();
			   			socket.connect();
			   		} catch (Exception e) {
			   			log("EventConnectError: Exception: "+e);
			   		}
		   		}
		    });


		    while (true) {
		    	try {
		    	    Thread.sleep(60000);
		        } catch (Exception e) {
		        	log(" CanopyThreadSleepException: "+e);
		        }
		    	log("CanopyController connect state: "+ socket.connected() );
		    }

		} /* run */
	}; /* runnable */

	canopyThread = new Thread(canopyRunnable);
	canopyThread.start();

  }

  void onSocketConnectError() {
  		engine.interactiveHSVEffect.resetAll();
  }

  void runOneShotTriggerable(JSONObject o) {

  	engine.log("RunOneShot: object "+o);

  	try {
	  	int shrubId = o.getInt("shrubId");

	  	if (! o.has("triggerableName")) {
	  		engine.log("triggerable has no name");
	  		return;
	  	}

  		String triggerName = o.getString("triggerableName");

  		switch (triggerName) {
  			case "lightning":  		
  			case "bass-slam":
  			case "rain":
  			case "color-burst":	
  			case "fire":
  				engine.interactiveHSVEffect.resetShrub(shrubId);
  				engine.interactiveFireEffect.onTriggeredShrub(shrubId);
  				break;
  			default:
  				engine.log("unknown trigger name "+triggerName);
  				break;
  		}
	} catch (Exception e) {
		engine.log(" runOneShotTriggerable Exception "+e);
	}

  }


  void updateShrubSetting(JSONObject o) {

  	engine.log("UpdateShrubSetting: object "+o);

  	try {
	  	int shrubId = o.getInt("shrubId");

	  	if (o.has("hueSet")) {
	  		int hue = o.getInt("hueSet");
	  		//engine.log(" going to set hue to "+hue+" for shrub "+shrubId);
	  		engine.interactiveHSVEffect.setShrubHueSet(shrubId,(float)hue);
	  	}

	  	if (o.has("hueShift")) {
	  		int hue = o.getInt("hueShift");
	  		//engine.log(" going to set hue to "+hue+" for shrub "+shrubId);
	  		engine.interactiveHSVEffect.setShrubHueShift(shrubId,(float)hue);
	  	}

	  	if (o.has("brightness")) {
	  		int b = o.getInt("brightness");
	  		//engine.log(" going to set hue to "+hue+" for shrub "+shrubId);
	  		engine.interactiveHSVEffect.setShrubBrightness(shrubId,(float)b);
	  	}
	  	if (o.has("saturation")) {
	  		int s = o.getInt("saturation");
	  		//engine.log(" going to set hue to "+hue+" for shrub "+shrubId);
	  		engine.interactiveHSVEffect.setShrubSaturation(shrubId,(float)s);
	  	}
	} catch (Exception e) {
		engine.log(" updateShrubSettingException "+e);
	}

  }

  void stopShrubInteraction(JSONObject o) {
  	//log("stopShrubInteraction: object "+o);

  	try {
	  	int shrubId = o.getInt("shrubId");

	  	engine.interactiveHSVEffect.resetShrub(shrubId);

	} catch (Exception e) {
		engine.log(" stopShrubInteraction(JSON): Exception "+e);
	}

  }

  void stopShrubInteraction(String s) {
  	//log("stopShrubInteraction: object "+o);

  	try {
	  	int shrubId = Integer.parseInt(s);

	  	engine.interactiveHSVEffect.resetShrub(shrubId);

	} catch (Exception e) {
		engine.log(" stopShrubInteraction(String): Exception "+e);
	}

  }


  // call this at startup to get the run/pause/whatever
  // and if there's a change to the "state"
  // and if the sculpture goes non-interactive

  public void 
  modelUpdate(boolean interactive, int runSeconds, int pauseSeconds, String state, ZonedDateTime nextTransition) {

  	if (!enabled) return;

  	// convert date to something pleasant
  	// THIS IS PROBABLY WRONG because it won't have timezone (Z).
  	// we probably need to have a ZonedDateTime and ask for it in Zulu time.
  	String nextTransition_str = nextTransition.format(DateTimeFormatter.ISO_DATE_TIME);

  	// make json object
  	JSONObject modelUpdateObj = new JSONObject();
  	try {
		modelUpdateObj.put("installationId", Config.installationId);
	  	modelUpdateObj.put("interactivityEnabled", interactive);
	  	JSONObject breakTimer = new JSONObject();
	  	breakTimer.put("runSeconds",runSeconds);
	  	breakTimer.put("pauseSeconds",pauseSeconds);
	  	breakTimer.put("state",state);
	  	breakTimer.put("nextStateChangeDate",nextTransition_str);
	  	modelUpdateObj.put("breakTimer",breakTimer);
	} catch (Exception e) {
		engine.log(" could not create json object for model Update "+e);
		return;
	}

  	// send to other end
  	while (socket == null) {
  		try {
  			Thread.sleep(100 /* milliseconds */);
  		} catch (Exception e) {
  			;
  		}
  	}
  	socket.emit("modelUpdated", modelUpdateObj);

  }



} // CanopyConnector
