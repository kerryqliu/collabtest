package org.example.collabtest;

import org.example.collabtest.resources.testResource;
import org.eclipse.jetty.websocket.jsr356.server.BasicServerEndpointConfig;
import org.example.collabtest.health.testHealthCheck;
import org.example.collabtest.resources.testWebSocketServer;
import org.example.collabtest.resources.automergeSocket;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;

import javax.websocket.server.ServerEndpointConfig;

public class testApplication extends Application<testConfiguration> {

    private WebsocketBundle websocketBundle;

    public static void main(final String[] args) throws Exception {
        new testApplication().run(args);
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void initialize(final Bootstrap<testConfiguration> bootstrap) {
        // TODO: application initialization
        websocketBundle = new WebsocketBundle(testWebSocketServer.class);
        bootstrap.addBundle(websocketBundle);
    }

    @Override
    public void run(final testConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
        final testResource resource = new testResource(
            configuration.getTemplate(),
            configuration.getDefaultName());
        
        environment.jersey().register(resource);
        
        final testHealthCheck healthCheck = 
                        new testHealthCheck(configuration.getTemplate());
        
        final BasicServerEndpointConfig bsec = new BasicServerEndpointConfig(null, automergeSocket.class,
                "/automerge");
        websocketBundle.addEndpoint(bsec);
        environment.healthChecks().register("template", healthCheck);
    }

}
