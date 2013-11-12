package de.avgl.dmp.controller.resources;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.utils.InternalSchemaDataUtil;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.converter.mf.stream.reader.JsonNodeReader;
import de.avgl.dmp.converter.morph.MorphScriptBuilder;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.types.Tuple;

@RequestScoped
@Path("transformations")
public class TransformationsResource {

	private final Provider<JsonToPojoMapper>	pojoMapperProvider;
	private final InternalSchemaDataUtil		schemaDataUtil;

	@Inject
	public TransformationsResource(final Provider<JsonToPojoMapper> pojoMapperProvider, final InternalSchemaDataUtil schemaDataUtil) {
		this.pojoMapperProvider = pojoMapperProvider;
		this.schemaDataUtil = schemaDataUtil;
	}

	private Response buildResponse(final String responseContent) {
		return Response.ok(responseContent).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public Response runToXML(final String jsonObjectString) throws IOException, DMPConverterException {

		final Transformation transformation;
		try {
			transformation = pojoMapperProvider.get().toTransformation(jsonObjectString);
		} catch (DMPPersistenceException e) {
			throw new DMPConverterException(e.getMessage());
		}

		final String xml = new MorphScriptBuilder().apply(transformation).toString();

		return buildResponse(xml);
	}

	/**
	 * this endpoint consumes a transformation as JSON representation
	 * 
	 * @param jsonObjectString a JSON representation of one transformation
	 * @return
	 * @throws IOException
	 * @throws DMPConverterException
	 * @throws DMPControllerException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runWithMetamorph(final String jsonObjectString, @QueryParam("resourceId") final Long resourceId,
			@QueryParam("configurationId") final Long configurationId) throws IOException, DMPConverterException, DMPControllerException {

		final Transformation transformation;

		try {
			transformation = pojoMapperProvider.get().toTransformation(jsonObjectString);
		} catch (DMPPersistenceException e) {
			throw new DMPConverterException(e.getMessage());
		}

		if (resourceId == null) {

			throw new DMPControllerException("No resource id defined for this transformation task. Please set a resource id.");
		}

		if (configurationId == null) {

			throw new DMPControllerException("No configuration id defined for this transformation task. Please set a configuration id.");
		}

		final TransformationFlow flow = TransformationFlow.fromTransformation(transformation);

		// final long resourceId = transformation.getSource().getResourceId();
		// final long configurationId = transformation.getSource().getConfigurationId();
		//
		// final Optional<Configuration> configurationOptional = schemaDataUtil.fetchConfiguration(resourceId, configurationId);

		// final List<String> parts = new ArrayList<String>(2);
		// parts.add("record");
		//
		// if (configurationOptional.isPresent()) {
		// final String name = configurationOptional.get().getName();
		// if (name != null && !name.isEmpty()) {
		// parts.add(name);
		// }
		// }
		//
		// final String recordPrefix = Joiner.on('.').join(parts);

		final Optional<Iterator<Tuple<String, JsonNode>>> inputData = schemaDataUtil.getData(resourceId, configurationId);

		if (!inputData.isPresent()) {
			
			throw new DMPConverterException("couldn't find input data for transformation");
		}

		final Iterator<Tuple<String, JsonNode>> tupleIterator = inputData.get();

		final String result = flow.apply(tupleIterator, new JsonNodeReader());

		return buildResponse(result);
	}

	/**
	 * this endpoint consumes a transformation as JSON representation
	 * 
	 * @param jsonObjectString a JSON representation of one transformation
	 * @return
	 * @throws IOException
	 * @throws DMPConverterException
	 */
	@Path("/demo")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response runWithMetamorphDemo(final String jsonObjectString) throws IOException, DMPConverterException {

		final Transformation transformation;
		try {
			transformation = pojoMapperProvider.get().toTransformation(jsonObjectString);
		} catch (DMPPersistenceException e) {
			throw new DMPConverterException(e.getMessage());
		}

		final TransformationFlow flow = TransformationFlow.fromTransformation(transformation);

		final String result = flow.applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);

		return buildResponse(result);
	}
}
