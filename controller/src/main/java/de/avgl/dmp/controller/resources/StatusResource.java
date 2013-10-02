package de.avgl.dmp.controller.resources;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@Path("_status")
public class StatusResource {

	@Inject
	private DMPStatus		dmpStatus;

	@Inject
	private MetricRegistry	registry;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getApiStatus() throws DMPControllerException {
		Response.ResponseBuilder result = Response.status(Response.Status.OK);

		final Map uptimeMap = Maps.newHashMap();
		uptimeMap.put("value", dmpStatus.getUptime());
		uptimeMap.put("unit", "milliseconds");

		final Map jsonMap = Maps.newHashMap();
		jsonMap.put("uptime", uptimeMap);
		jsonMap.put("metrics", registry);

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();

		String json;
		try {
			json = objectMapper.writeValueAsString(jsonMap);

		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		return result.entity(json).build();
	}
}
