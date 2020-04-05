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


@ServerEndpoint("/automerge")
public class automergeSocket {
    public static HashMap<String, Session> websocketSessionMap = new HashMap<>();

    public static String lockHolder = "";

    // Stores state of automerge
    public static String automergeInitial = "";

    @OnOpen
    public void myOnOpen(final Session session) throws IOException {
        session.getAsyncRemote().sendText("{\"connected\": true}");
        if (automergeInitial == "") {
            // We send false back to client to let them know we need to initialize. We'll have a function
            // In the client to do this and send the changes over
            session.getAsyncRemote().sendText("{\"initial\": false}");
        }
        else {
            // If it's already set, we send over true then send over initial data to initialize.
            // I'll change the names for the fields. 
            session.getAsyncRemote().sendText("{\"initial\": true}");
            session.getAsyncRemote().sendText("{\"initialization\": \"" + automergeInitial + "\"}");
        }
        websocketSessionMap.put(session.getId(), session);
        System.out.println("NEW CONNECTION");
    }

    @OnMessage
    public void myOnMsg(final Session session, String message) {

        message = message.substring(1, message.length() - 1);
        // Two different things if current automergeObject is sent 
        if (message.contains("changeInitial")) {
            // Will change this so we can update initial whenever a change gets made
            // Change index based on initial thing
            automergeInitial = message.substring(13);
        }
        else {
            if (message.equals("lockRequest")) {
                this.changeLock(session);
            }
            else {
                String temp = "{\"response\": \"" + message + "\"}";
                System.out.println(temp);
                for(String key: websocketSessionMap.keySet()) {
                    // only send to other sessions, not the session that send the message?
                    Session sess = websocketSessionMap.get(key);
                    if (sess != session) {
                        websocketSessionMap.get(key).getAsyncRemote().sendText(temp);
                        System.out.println(key + " : "  + "hi"); 
                    }       
                }
            }
        }
        System.out.println("Message received");
     //   session.getAsyncRemote().sendText(temp);
    }

    public void changeLock(Session newSession) {
        // Client is incapable fo changing if lock is currently claimed
        System.out.println(newSession.getId());
        System.out.println(lockHolder);
        System.out.println("Size: " + websocketSessionMap.size());
        if (lockHolder == "") {
            String temp = "{\"lockStatus\": true}";
            newSession.getAsyncRemote().sendText(temp);
            lockHolder = newSession.getId();
            System.out.println("SETTING LOCK");
        } else if (newSession.getId().equals(lockHolder)) {
            String temp = "{\"lockStatus\": false}";
            newSession.getAsyncRemote().sendText(temp);
            this.releaseLock();
            System.out.println("RESETTING LOCK");
        } else {
            String temp = "{\"lockStatus\": false}";
            newSession.getAsyncRemote().sendText(temp);
            System.out.println("LOCK ALREADY SET");
        }
    }

    public void releaseLock() {
        lockHolder = "";
    }

    @OnClose
    public void myOnClose(final Session session, CloseReason cr) {
        websocketSessionMap.remove(session.getId());
        System.out.println("Session disconnected");
        if (this.lockHolder.equals(session.getId())) {
            this.releaseLock();
        }
        if (websocketSessionMap.isEmpty()) {
            automergeInitial = "";
        }
    }
}