package de.avgl.dmp.controller.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.avgl.dmp.persistence.model.TestObject;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 * 
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN + "; qs=0.9")
	public String getIt() {
		return "Got it!";
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TestObject getItJSON() {

		return new TestObject();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public TestObject getItXML() {

		return new TestObject();
	}
}
