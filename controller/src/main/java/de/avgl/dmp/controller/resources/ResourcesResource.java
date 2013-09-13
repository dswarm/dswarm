package de.avgl.dmp.controller.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.services.PersistenceServices;
import de.avgl.dmp.controller.utils.DMPControllerUtils;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.services.ResourceService;

@Path("resources")
public class ResourcesResource {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ResourcesResource.class);

	private Response buildResponse(final String responseContent) {

		return Response.ok(responseContent).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadResource(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws DMPControllerException {

		final Resource resource = createResource(uploadedInputStream, fileDetail);

		String resourceJSON = null;

		try {

			resourceJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string");
		}

		return buildResponse(resourceJSON);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources() throws DMPControllerException {
		
		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();
		
		final List<Resource> resources = resourceService.getObjects();
		
		if(resources == null) {
			
			LOG.debug("couldn't find resource");
			
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		if(resources.isEmpty()) {
			
			LOG.debug("there are no resources");
			
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		String resourcesJSON = null;

		try {

			resourcesJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(resources);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resources list object to JSON string.\n" + e.getMessage());
		}

		return buildResponse(resourcesJSON);
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResource(@PathParam("id") Long id) throws DMPControllerException {
		
		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();
		
		final Resource resource = resourceService.getObject(id);
		
		if(resource == null) {
			
			LOG.debug("couldn't find resource");
			
			return Response.status(Status.NOT_FOUND).header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
		}
		
		String resourceJSON = null;

		try {

			resourceJSON = DMPUtil.getJSONObjectMapper().writeValueAsString(resource);
		} catch (final JsonProcessingException e) {

			throw new DMPControllerException("couldn't transform resource object to JSON string.\n" + e.getMessage());
		}

		return buildResponse(resourceJSON);
	}

	@OPTIONS
	public Response getOptions() {

		return Response.ok().header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS, HEAD")
				.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Accept, Origin, X-Requested-With, Content-Type").build();
	}

	private Resource createResource(final InputStream uploadInputedStream, final FormDataContentDisposition fileDetail) throws DMPControllerException {

		final String fileName = fileDetail.getFileName();

		final File file = DMPControllerUtils.writeToFile(uploadInputedStream, fileName, "resources");

		final ResourceService resourceService = PersistenceServices.getInstance().getResourceService();

		Resource resource = null;

		try {

			resource = resourceService.createObject();
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while resource creation");

			throw new DMPControllerException("something went wrong while resource creation\n" + e.getMessage());
		}

		if (resource == null) {

			throw new DMPControllerException("fresh resource shouldn't be null");
		}

		resource.setName(fileName);
		resource.setType(ResourceType.FILE);

		String fileType = null;

		try {

			fileType = Files.probeContentType(file.toPath());
		} catch (IOException e1) {

			LOG.debug("couldn't determine file type from file '" + file.getAbsolutePath() + "'");
		}

		final ObjectNode attributes = new ObjectNode(DMPUtil.getJSONFactory());
		attributes.put("path", file.getAbsolutePath());

		if (fileType != null) {

			attributes.put("filetype", fileType);
		}

		attributes.put("filesize", fileDetail.getSize());

		resource.setAttributes(attributes);

		try {

			resourceService.updateObjectTransactional(resource);
		} catch (final DMPPersistenceException e) {

			LOG.debug("something went wrong while resource updating");

			throw new DMPControllerException("something went wrong while resource updating\n" + e.getMessage());
		}

		return resource;
	}
}
