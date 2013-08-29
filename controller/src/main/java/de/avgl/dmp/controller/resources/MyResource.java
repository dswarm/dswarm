package de.avgl.dmp.controller.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.net.HttpHeaders;

import de.avgl.dmp.persistence.model.TestObject;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to the client as "text/plain" media type.
	 * 
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN + "; qs=0.9")
	public Response getIt() {

		final String message = "Got it!";

		return Response.ok(message).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItJSON() {

		final TestObject testObject = new TestObject();

		return Response.ok(testObject).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getItXML() {

		final TestObject testObject = new TestObject();

		return Response.ok(testObject).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}
}
