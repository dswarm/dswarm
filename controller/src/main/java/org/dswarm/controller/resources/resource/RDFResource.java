/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.resource.utils.ExportUtils;

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

	// this is likely to be http://localhost:7474/graph
	private final String		graphEndpoint;

	@Inject
	public RDFResource(@Named("dswarm.db.graph.endpoint") final String graphEndpointArg) {
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
	@Timed
	@GET
	@Path("/getall")
	// SR TODO removing of @Produces should result in accepting any requested format (accept header?) Is this what we want as a
	// proxy endpoint - let the graph endpoint decide which formats are accepted
	//	@Produces({ MediaTypeUtil.N_QUADS, MediaTypeUtil.TRIG })
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportAllRDFForDownload(@QueryParam("format") final String format)
			throws DMPControllerException {

		RDFResource.LOG.debug("Forwarding to graph db: Request to export all rdf data to " + format);

		// send the request to graph DB
		final WebTarget target = target("/getall");
		final Response responseFromGraph = target.request().accept(format).get(Response.class);

		return ExportUtils.processGraphDBResponseInternal(responseFromGraph);

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
