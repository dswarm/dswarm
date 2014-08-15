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
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/getall")
	// SR TODO removing of @Produces should result in accepting any requested format (accept header?) Is this what we want as a
	// proxy endpoint - let the graph endpoint decide which formats are accepted
	//	@Produces({ MediaTypeUtil.N_QUADS, MediaTypeUtil.TRIG })
	public Response exportAllRDFForDownload(@QueryParam("format") @DefaultValue(MediaTypeUtil.N_QUADS) final String format)
			throws DMPControllerException {

		final MediaType formatType = MediaTypeUtil.getMediaType(format, MediaTypeUtil.N_QUADS_TYPE);
		RDFResource.LOG.debug("Exporting rdf data into " + formatType);

		// send the request to graph DB
		final WebTarget target = target("/getall");
		final Response responseFromGraph = target.request().accept(format).get(Response.class);
		
		Response responseToRequester = ExportUtils.processGraphDBResonseInternal(responseFromGraph);

		return responseToRequester;

	}

	
	// SR FIXME: temporarily reactivated this endpoint to be compatible with current FE version
	/**
	 * @param id a data model identifier
	 * @return
	 */
	@ApiOperation(value = "exports a selected data model from the graph DB in the given RDF serialisation format", notes = "Returns exported data in the given RDF serialisation format.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "export was successfully processed"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/export/{id}/")
	@Produces({ MediaTypeUtil.N_QUADS, MediaTypeUtil.RDF_XML, MediaTypeUtil.TRIG, MediaTypeUtil.TURTLE, MediaTypeUtil.N3 })
	public Response exportSingleRDFForDownload(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final Long id,
			@QueryParam("format") @DefaultValue(MediaTypeUtil.N_QUADS) String format) throws DMPControllerException {

		// construct provenanceURI from id
		final String provenanceURI = GDMUtil.getDataModelGraphURI(id);

		final MediaType formatType = MediaTypeUtil.getMediaType(format, MediaTypeUtil.N_QUADS_TYPE);

		// get file extension
		final String fileExtension = RDFLanguages.contentTypeToLang(formatType.toString()).getFileExtensions().get(0);
		LOG.debug("Exporting rdf data to " + formatType.toString());

		// forward the request to graph DB
		final WebTarget target = target("/export");
		final Response response = target.queryParam("format", format).queryParam("provenanceuri", provenanceURI).request().accept(format)
				.get(Response.class);

		if (response.getStatus() != 200) {

			// TODO give more details? e.g. if the requested format is not supported?
			throw new DMPControllerException("Couldn't export data from database. Received status code '" + response.getStatus()
					+ "' from database endpoint.");
		}

		final InputStream result = response.readEntity(InputStream.class);

		return Response.ok(result, formatType).header(ExportUtils.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''rdf_export." + fileExtension)
				.build();
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
