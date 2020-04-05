package org.example.collabtest.resources;

import org.example.collabtest.testConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@ServerEndpoint("/test")
public class testWebSocketServer {
    public static HashMap<String, Session> websocketSessionMap = new HashMap<>();
    public static BiMap<String, String> userWebSocketMap = HashBiMap.create();


    @OnOpen
    public void myOnOpen(final Session session) throws IOException {
        session.getAsyncRemote().sendText("{\"connected\": \"true\"}");
        websocketSessionMap.put(session.getId(), session);
    }

    @OnMessage
    public void myOnMsg(final Session session, String message) {
      //  System.out.println("Message received");
        message = message.replace("\"", "");
        String temp = "{\"response\": \"hello " + message.toUpperCase() + "\"}";
        System.out.println(temp);
        for(String key: websocketSessionMap.keySet()) {
            websocketSessionMap.get(key).getAsyncRemote().sendText(temp);
            System.out.println(key + " : "  + "hi");        
        }
        System.out.println("Message received");
        System.out.println(temp);
     //   session.getAsyncRemote().sendText(temp);
    }
    

    @OnClose
    public void myOnClose(final Session session, CloseReason cr) {
        websocketSessionMap.remove(session.getId());
    }
}