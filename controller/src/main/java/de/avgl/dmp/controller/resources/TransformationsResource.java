package de.avgl.dmp.controller.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;

@RequestScoped
@Api(value = "/transformations", description = "Operations about transformations.")
@Path("transformations")
public class TransformationsResource extends BasicFunctionsResource<TransformationService, Transformation> {

	@Inject
	public TransformationsResource(final Provider<TransformationService> transformationServiceProviderArg, final ObjectMapper objectMapper,
			final DMPStatus dmpStatus) {

		super(Transformation.class, transformationServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the transformation that matches the given id", notes = "Returns the Transformation object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "transformation identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new transformation", notes = "Returns a new Transformation object.", response = Transformation.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "transformation (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all transformations ", notes = "Returns a list of Transformation objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Transformation prepareObjectForUpdate(final Transformation objectFromJSON, final Transformation object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setComponents(objectFromJSON.getComponents());

		return object;
	}

	// private final Provider<JsonToPojoMapper> pojoMapperProvider;
	// private final InternalSchemaDataUtil schemaDataUtil;
	//
	// @Inject
	// public TransformationsResource(final Provider<JsonToPojoMapper> pojoMapperProvider, final InternalSchemaDataUtil
	// schemaDataUtil) {
	// this.pojoMapperProvider = pojoMapperProvider;
	// this.schemaDataUtil = schemaDataUtil;
	// }
	//
	// private Response buildResponse(final String responseContent) {
	// return Response.ok(responseContent).build();
	// }
	//
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_XML)
	// public Response runToXML(final String jsonObjectString) throws IOException, DMPConverterException {
	//
	// // TODO: fixme
	//
	// // final Transformation transformations;
	// // try {
	// // transformations = pojoMapperProvider.get().toTransformation(jsonObjectString);
	// // } catch (DMPPersistenceException e) {
	// // throw new DMPConverterException(e.getMessage());
	// // }
	//
	// final String xml = null;
	//
	// //new MorphScriptBuilder().apply(transformations).toString();
	//
	// return buildResponse(xml);
	// }
	//
	// /**
	// * this endpoint consumes a transformations as JSON representation
	// *
	// * @param jsonObjectString a JSON representation of one transformations
	// * @return
	// * @throws IOException
	// * @throws DMPConverterException
	// * @throws DMPControllerException
	// */
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response runWithMetamorph(final String jsonObjectString, @QueryParam("resourceId") final Long resourceId,
	// @QueryParam("configurationId") final Long configurationId) throws IOException, DMPConverterException,
	// DMPControllerException {
	//
	// // TODO: fixme
	//
	// // final Transformation transformations;
	// //
	// // try {
	// // transformations = pojoMapperProvider.get().toTransformation(jsonObjectString);
	// // } catch (DMPPersistenceException e) {
	// // throw new DMPConverterException(e.getMessage());
	// // }
	//
	// if (resourceId == null) {
	//
	// throw new DMPControllerException("No resource id defined for this transformations task. Please set a resource id.");
	// }
	//
	// if (configurationId == null) {
	//
	// throw new
	// DMPControllerException("No configuration id defined for this transformations task. Please set a configuration id.");
	// }
	//
	// // TODO: fixme
	//
	// // final TransformationFlow flow = TransformationFlow.fromTransformation(transformations);
	//
	// // final long resourceId = transformations.getSource().getResourceId();
	// // final long configurationId = transformations.getSource().getConfigurationId();
	// //
	// // final Optional<Configuration> configurationOptional = schemaDataUtil.fetchConfiguration(resourceId, configurationId);
	//
	// // final List<String> parts = new ArrayList<String>(2);
	// // parts.add("record");
	// //
	// // if (configurationOptional.isPresent()) {
	// // final String name = configurationOptional.get().getName();
	// // if (name != null && !name.isEmpty()) {
	// // parts.add(name);
	// // }
	// // }
	// //
	// // final String recordPrefix = Joiner.on('.').join(parts);
	//
	// final Optional<Iterator<Tuple<String, JsonNode>>> inputData = schemaDataUtil.getData(resourceId, configurationId);
	//
	// if (!inputData.isPresent()) {
	//
	// throw new DMPConverterException("couldn't find input data for transformations");
	// }
	//
	// final Iterator<Tuple<String, JsonNode>> tupleIterator = inputData.get();
	//
	// // TODO: fime
	//
	// final String result = null;
	// //flow.apply(tupleIterator, new JsonNodeReader());
	//
	// return buildResponse(result);
	// }
	//
	// /**
	// * this endpoint consumes a transformations as JSON representation
	// *
	// * @param jsonObjectString a JSON representation of one transformations
	// * @return
	// * @throws IOException
	// * @throws DMPConverterException
	// */
	// @Path("/demo")
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response runWithMetamorphDemo(final String jsonObjectString) throws IOException, DMPConverterException {
	//
	// // TODO: fixme
	//
	// // final Transformation transformations;
	// // try {
	// // transformations = pojoMapperProvider.get().toTransformation(jsonObjectString);
	// // } catch (DMPPersistenceException e) {
	// // throw new DMPConverterException(e.getMessage());
	// // }
	//
	// final TransformationFlow flow = null;
	// //TransformationFlow.fromTransformation(transformations);
	//
	// // final String result = flow.applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);
	// final String result = "{\"status\":\"nok\",\"reason\":\"not implemented / in transition\"}";
	//
	// return buildResponse(result);
	// }
}
