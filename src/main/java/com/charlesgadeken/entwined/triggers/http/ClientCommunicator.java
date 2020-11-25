package com.charlesgadeken.entwined.triggers.http;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ClientCommunicator {
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
