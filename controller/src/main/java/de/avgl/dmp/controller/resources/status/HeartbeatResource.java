package de.avgl.dmp.controller.resources.status;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.net.HttpHeaders;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;


@RequestScoped
@Api(value = "/_ping", description = "Ping the server for a heartbeat.")
@Path("_ping")
public class HeartbeatResource {

	private static final String RESPONSE = "pong";

	@ApiOperation("send a ping, receive a pong")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getPong() {
		return Response
				.ok(RESPONSE)
				.header(HttpHeaders.CACHE_CONTROL, "must-revalidate,no-cache,no-store")
				.build();
	}
}
