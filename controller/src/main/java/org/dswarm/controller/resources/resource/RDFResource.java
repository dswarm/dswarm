package org.dswarm.controller.resources.resource;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import org.dswarm.controller.resources.resource.utils.ExportUtils;
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
			@ApiResponse(code = 406, message = "requested export format is not supported"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/getall")
	// SR TODO removing of @Produces should result in accepting any requested format (accept header?) Is this what we want as a
	// proxy endpoint - let the graph endpoint decide which formats are accepted
	//	@Produces({ MediaTypeUtil.N_QUADS, MediaTypeUtil.TRIG })
	public Response exportAllRDFForDownload(@QueryParam("format") final String format)
			throws DMPControllerException {

		RDFResource.LOG.debug("Forwarding to graph db: Request to export all rdf data to " + format);

		// send the request to graph DB
		final WebTarget target = target("/getall");
		final Response responseFromGraph = target.request().accept(format).get(Response.class);
		
		Response responseToRequester = ExportUtils.processGraphDBResponseInternal(responseFromGraph);

		return responseToRequester;

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
