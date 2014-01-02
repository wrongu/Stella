package org.stella.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.stella.Main;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("r2")
public class ResourceTwo {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("{stuff:.*}")
    public Response getIt(@PathParam("stuff") String daStuff) {
    	Main.log("GET @ r2");
    	return Response.status(200).entity("welp. you asked for "+daStuff).build();
    }
}
