package org.dswarm.controller.resources.export;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.jena.riot.RDFLanguages;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.persistence.util.GDMUtil;

/**
 * Created by tgaengler on 28/04/14.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/rdf", description = "Provides some RDF export services.")
@Path("rdf")
public class RDFResource {

	private static final Logger	LOG					= LoggerFactory.getLogger(RDFResource.class);

	// SR TO move somewhere else and reuse 
	public static final String	resourceIdentifier	= "rdf";

	/**
	 * The metrics registry.
	 */
	private final DMPStatus		dmpStatus;

	// this is likely to be http://localhost:7474/graph
	private final String		graphEndpoint;

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
	@Produces({ MediaTypeUtil.N_QUADS, MediaTypeUtil.TRIG })
	public Response exportAllRDFForDownload(@QueryParam("format") @DefaultValue(MediaTypeUtil.N_QUADS) final String format)
			throws DMPControllerException {

		final MediaType formatType = MediaTypeUtil.getMediaType(format, MediaTypeUtil.N_QUADS_TYPE);

		// get file extension
		final String fileExtension = RDFLanguages.contentTypeToLang(formatType.toString()).getFileExtensions().get(0);
		RDFResource.LOG.debug("Exporting rdf data into " + formatType);

		// forward the request to graph DB
		final WebTarget target = target("/getall");
		final Response response = target.request().accept(format).get(Response.class);

		if (response.getStatus() != 200) {

			// TODO give more details? e.g. if the requested format is not supported?
			throw new DMPControllerException("Couldn't export data from database. Received status code '" + response.getStatus()
					+ "' from database endpoint.");
		}

		final InputStream result = response.readEntity(InputStream.class);

		return Response.ok(result, formatType).header("Content-Disposition", "attachment; filename*=UTF-8''rdf_export." + fileExtension).build();
	}

	

	// deactivated because of unexplainable, unpredictable behavior (on a windows machine) 2014-07-30. This is likely a jersey
	// bug.
	// * precondition: start FE, databases
	// * to reproduce the bug, do the following steps repeatedly (up to 10 times..) till the bug comes up
	// * 1. start BE
	// * 2. in FE: push Export button
	// * 3. check with developer tools:
	// ** "Request URL:http://127.0.0.1:8087/dmp/rdf/getall?format=application%2Fn-quads"
	// ** response header should contain "Content-Disposition:attachment; filename*=UTF-8''rdf_export.nq",
	// ** "Content-Type:application/octet-stream"
	// * in case the bug is "active", the response header contains "Content-Type:application/n-quads" and no "Content-Disposition"
	// field
	// SR hint for bugfix: maybe this comes up since @Produces("application/n-quads") and @Produces({ N_QUADS, TRIG }) overlap in
	// producing n-quads, it is not allowed to have 2 methods for one endpoint producing the same MediaType
	//
	// @GET
	// @Path("/getall")
	// @Produces("application/n-quads")
	// public Response exportAllRDF() throws DMPControllerException {
	//
	// final WebTarget target = target("/getall");
	//
	// // GET the request
	// final Response response = target.request().accept("application/n-quads").get(Response.class);
	//
	// if (response.getStatus() != 200) {
	//
	// throw new DMPControllerException("Couldn't export data from database. Received status code '" + response.getStatus()
	// + "' from database endpoint.");
	// }
	//
	// final String result = response.readEntity(String.class);
	//
	// return Response.ok().entity(result).build();
	// }

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
