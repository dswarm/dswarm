package de.avgl.dmp.controller.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.avgl.dmp.persistence.model.TestObject;
import de.avgl.dmp.persistence.model.Transformation;

@Path("transformations")
public class TransformationsResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TestObject run(final Transformation transformation) {

		final TestObject testObject = new TestObject();
		testObject.setMessage(transformation.toString());
		
		return testObject;
	}
}
