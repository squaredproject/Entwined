package entwined.plugin;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*
 * class TSServer
 *
 * Generic server class; cribbed from Processing and heavily modified
 * Reads from TCP connections until we get a \n character, calls
 * a callback on the 'parent'
 */

class TSServer implements Runnable {
  Object parent;
  Method serverEventMethod;
  volatile Thread thread;
  ServerSocket server;
  int port;
  public List<TSClient> clients;

  public TSServer(Object parent, int port) {
    this(parent, port, null);
  }

  public TSServer(Object parent, int port, String host) {
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

      // reflection to check whether parent has a call -
      // public void serverEvent(Server s, Client c);
      // which is called when a new guy connects
      try {
        serverEventMethod =
          parent.getClass().getMethod("serverEvent", TSServer.class, TSClient.class);
      } catch (Exception e) {
        System.out.println("INFO: TSServer create: no server event on this object");
      }

    } catch (IOException e) {
      thread = null;
      throw new RuntimeException(e);
    }
  }


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


  public boolean active() {
    return thread != null;
  }

  /*
  static public String ip() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return null;
    }
  }
  */


  // the last index used for available. can't just cycle through
  // the clients in order from 0 each time, because if client 0 won't
  // shut up, then the rest of the clients will never be heard from.
  // Breadcrumb. Fairness not as important as stability. The thread locking
  // wasn't right. Let's just use a standard java primitive.

  /**
   * Returns the next client in line with a new message.
   * and oh by the way reaps out inactive clients
   *
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


  public void stop() {
    dispose();
  }


  protected void dispose() {
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


