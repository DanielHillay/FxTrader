package com.deriska.psydtrader.service.websocket;

import java.net.URI;
import java.io.IOException;
import java.lang.InterruptedException;
import javax.websocket.*;

@ClientEndpoint
public class WSClient  {

    private Session session;

    @OnOpen
    public void onOpen(Session session) throws java.io.IOException
    {
        this.session = session;
        session.getBasicRemote().sendText("{\"ticks\": \"R_100\"}");
    }

    @OnMessage
    public void onMessage(String message)
    {
        System.out.println("ticks update: " + message);
    }

    public static void makeConnectionToDeriv()
            throws IOException, DeploymentException, InterruptedException
    {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI apiUri = URI.create("wss://ws.binaryws.com/websockets/v3?app_id=1089");
        Session session = container.connectToServer(WSClient.class, apiUri);
        while (session.isOpen()) {
            // receive ticks
        }
    }

    public void closeConnection(Session session) throws IOException {
        session.close();
    }
}