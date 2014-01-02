package org.stella;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class ServerManager {
	
	private static HttpServer currentServer;
	
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(String path, int port) {
    	if(currentServer != null) currentServer.stop();
    	
    	// Manually configure the URI since UriBuilder was behaving strangely
    	// 	the first time around (it was building "http://:<port>/<path>", 
    	//	which was clearly not working.
    	while(path.charAt(path.length()-1) == '/')
    		path = path.substring(0, path.length()-1);
    	URI baseURI = UriBuilder.fromPath("http://" + path + ":" + port).build();
    	
        // create a resource config that scans for JAX-RS resources and providers
        // in the given package
        ResourceConfig rc = new ResourceConfig().packages(Constants.REST_PACKAGE);
        // the CrossDomainFilter is required for accpeting requests from external source
        // (i.e. all requests)
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at baseURI
        currentServer = GrizzlyHttpServerFactory.createHttpServer(baseURI, rc);
        
        if(currentServer.isStarted())
        	Main.log("Server started at " + baseURI);
        else
        	Main.log("Problem starting server at " + baseURI, true);
        
        return currentServer;
    }

}
