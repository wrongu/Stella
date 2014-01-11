package org.stella.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.stella.Main;

import static org.stella.Constants.*;

@Path(API_ROOT)
public class Documents {
	
	// Because we are embedding file paths with '/' in them in the url,
	// we accept any text as a path using the regex .*
	private static final String PATH_PATTERN = "{path:.*}";
	
	/**
	 * CORS is Cross-Origin Resource Sharing. From the clients' perspective,
	 * Stella is an external resource, so this must be set. The protocol is to
	 * first query the server with OPTIONS to see if further requests are allowed.
	 */
	@OPTIONS
	@Path(PATH_PATTERN)
	public Response checkCORS(
			@PathParam("path") String path,
			@HeaderParam("Access-Control-Request-Headers") String requestH){
		Main.log("OPTIONS {"+path+"}");
		return Response.status(Response.Status.OK)
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS") // all available verbs for this path
				.header("Access-Control-Allow-Origin", "*") // accept requests from anyone anywhere
				.header("Access-Control-Allow-Headers", requestH) // indiscriminately allow any custom headers through
				.build();
	}
	
	@PUT
	@Path(PATH_PATTERN)
	public Response createDocument(@PathParam("path") String path){
		Main.log("create {"+path+"}");
		return Response.status(Response.Status.OK).build();
	}
	
	@DELETE
	@Path(PATH_PATTERN)
	public Response deleteDocument(@PathParam("path") String path){
		Main.log("delete {"+path+"}");
		return Response.status(Response.Status.OK).build();
	}
	
	@POST
	@Path(PATH_PATTERN)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response interactDocument(
			String messageBody,
			@PathParam("path") String path,
			@HeaderParam(API_HEADER_ACTION) String action,
			@DefaultValue("") @HeaderParam(API_HEADER_GROUP) String group){
		// handle each action separately
		if(action.equals(API_ACTION_EDIT)){
			Main.log("edit {"+path+"} with group {"+group+"}: " + messageBody);
		} else if(action.equals(API_ACTION_UNDO)){
			Main.log("undo {"+path+"} with group {"+group+"}");
		} else if(action.equals(API_ACTION_REDO)){
			Main.log("redo {"+path+"} with group {"+group+"}");
		} else if(action.equals("")){
			Main.log("Can't do anything with a POST and empty action", true);
		} else{
			Main.log("Unknown editing action: " + action, true);
		}
		return Response.status(Response.Status.OK).build();
	}
	
	@GET
	@Path(PATH_PATTERN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response refreshDocument(
			@PathParam("path") String path,
			@DefaultValue("0") @HeaderParam(API_HEADER_REVISION) int rev){
		Main.log("refresh {"+path+"} rev "+rev);
		return Response.status(Response.Status.OK).build();
	}
}
