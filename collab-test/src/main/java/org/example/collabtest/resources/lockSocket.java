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

@ServerEndpoint("/lock")
class LockSocket {
    public static HashMap<String, Session> websocketSessionMap = new HashMap<>();

    public Session lockHolder = null;

    @OnOpen
    public void myOnOpen(final Session session) throws IOException {
        session.getAsyncRemote().sendText("{\"connected\": \"true\"}");
        websocketSessionMap.put(session.getId(), session);
    }

    @OnMessage
    public void myOnMsg(final Session session, String message) {
        String temp = "{\"response\": \"" + "True" + "\"}";
     //   session.getAsyncRemote().sendText(temp);
        // If requesting session is the lock holder, do nothing. Don't need to send response
        // If requesting session isn't lock holder, inform that client. Send some response.
        // If lockHolder is null, set new lockHolder. Send some response
    }


    public void setLockHolder(Session newSession) {
        this.lockHolder = newSession;
    }

    public void releaseLock() {
        this.lockHolder = null;
    }

    @OnClose
    public void myOnClose(final Session session, CloseReason cr) {
        websocketSessionMap.remove(session.getId());
        System.out.println("Session disconnected");
        if (this.lockHolder == session) {
            this.releaseLock();
        }
    }
}