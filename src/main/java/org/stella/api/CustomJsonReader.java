package org.stella.api;

import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;

/**
 * A custom MessageBodyReader that gives us access to the plain JSON, instead
 * 	of having to map the JSON to a Java Object
 * 
 * Thanks to http://stackoverflow.com/questions/1662490/consuming-json-object-in-jersey-service
 * 
 * Note that moving this class to another package will cause it to no longer be recognized.
 * 	See ServerManager for details. TODO this isn't working properly
 * 
 * @author wrongu
 *
 * @param <T> may be String or org.codehaus.jackson.JsonNode; it is implicit in the argument of
 *  whatever function Consumes application/json
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class CustomJsonReader<T> implements MessageBodyReader<T> {
	
	private static JsonFactory jFactory = new JsonFactory();

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// because we've already declared that we only accept application/json,
		// we assume that anything that has made it this far is readable
		return true;
	}

	@Override
	public T readFrom(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		try{
			if(type == String.class){
				System.out.println("hit custom json-->string reader");
				return type.cast(IOUtils.toString(entityStream, "UTF-8"));
			} else if(type == JsonNode.class){
				System.out.println("hit custom json object parsing code");
				return type.cast(jFactory.createJsonParser(entityStream).readValueAsTree());
			}
		} catch(Exception e){
			throw new WebApplicationException(e);
		}
		return null;
	}
}