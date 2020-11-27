package com.charlesgadeken.entwined.triggers.http;

import com.charlesgadeken.entwined.EngineController;
import heronarts.lx.LX;

public class AppServer {
    LX lx;
    EngineController engineController;

    public AppServer(LX lx, EngineController engineController) {
        this.lx = lx;
        this.engineController = engineController;
    }

    public void start() {
        Server server = new Server(5204);

        ClientCommunicator clientCommunicator = new ClientCommunicator(server);
        ClientModelUpdater clientModelUpdater =
                new ClientModelUpdater(engineController, clientCommunicator);
        ParseClientTask parseClientTask =
                new ParseClientTask(engineController, server, clientModelUpdater);
        lx.engine.addLoopTask(parseClientTask);
    }
}
