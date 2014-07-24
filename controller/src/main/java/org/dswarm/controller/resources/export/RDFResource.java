package org.dswarm.controller.resources.export;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.status.DMPStatus;

/**
 * Created by tgaengler on 28/04/14.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/rdf", description = "Provides some RDF export services.")
@Path("rdf")
public class RDFResource {

	private static final Logger		LOG					= LoggerFactory.getLogger(RDFResource.class);

	private static final String		resourceIdentifier	= "rdf";

	private static final MediaType	N_QUADS_TYPE		= new MediaType("application", "n-quads");

	/**
	 * The metrics registry.
	 */
	private final DMPStatus			dmpStatus;

	private final String			graphEndpoint;

	@Inject
	public RDFResource(final DMPStatus dmpStatusArg, @Named("dswarm.db.graph.endpoint") final String graphEndpointArg) {

		dmpStatus = dmpStatusArg;
		graphEndpoint = graphEndpointArg;
	}

	/**
	 * for triggering a download
	 *
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "exports all data from the graph DB in the given RDF serialisation format", notes = "Returns exported data in the given RDF serialisation format.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "export was successfully processed"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/getall")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportAllRDFForDownload(@QueryParam("format") @DefaultValue("application/n-quads") final String format)
			throws DMPControllerException {

		final String[] formatStrings = format.split("/", 2);
		final MediaType formatType;
		if (formatStrings.length == 2) {
			formatType = new MediaType(formatStrings[0], formatStrings[1]);
		} else {
			formatType = RDFResource.N_QUADS_TYPE;
		}

		RDFResource.LOG.debug("Exporting rdf data into " + formatType);

		final WebTarget target = target("/getall");

		// GET the request
		final Response response = target.queryParam("format", format).request().accept(MediaType.APPLICATION_OCTET_STREAM).get(Response.class);

		if (response.getStatus() != 200) {

			throw new DMPControllerException("Couldn't export data from database. Received status code '" + response.getStatus()
					+ "' from database endpoint.");
		}

		final InputStream result = response.readEntity(InputStream.class);

		return Response.ok(result, MediaType.APPLICATION_OCTET_STREAM_TYPE)
				.header("Content-Disposition", "attachment; filename*=UTF-8''rdf_export.nq").build();
	}

	@GET
	@Path("/getall")
	@Produces("application/n-quads")
	public Response exportAllRDF() throws DMPControllerException {

		final WebTarget target = target("/getall");

		// GET the request
		final Response response = target.request().accept("application/n-quads").get(Response.class);

		if (response.getStatus() != 200) {

			throw new DMPControllerException("Couldn't export data from database. Received status code '" + response.getStatus()
					+ "' from database endpoint.");
		}

		final String result = response.readEntity(String.class);

		return Response.ok().entity(result).build();
	}

	private Client client() {

		final ClientBuilder builder = ClientBuilder.newBuilder();

		return builder.register(MultiPartFeature.class).build();
	}

	private WebTarget target() {

		return client().target(graphEndpoint).path(RDFResource.resourceIdentifier);
	}

	private WebTarget target(final String... path) {

		WebTarget target = target();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}
}
