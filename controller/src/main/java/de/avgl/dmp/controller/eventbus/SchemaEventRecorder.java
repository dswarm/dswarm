package de.avgl.dmp.controller.eventbus;

import java.util.List;
import java.util.Set;

import org.culturegraph.mf.types.Triple;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;

@Singleton
public class SchemaEventRecorder {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(SchemaEventRecorder.class);

	private final AttributePathService				attributePathService;
	private final AttributeService					attributeService;
	private final ClaszService						claszService;
	private final DataModelService					dataModelService;
	private final SchemaService						schemaService;

	@Inject
	public SchemaEventRecorder(final AttributePathService attributePathService, final AttributeService attributeService,
			final ClaszService claszService, final DataModelService dataModelService, final SchemaService schemaService, final EventBus eventBus) {

		this.attributePathService = attributePathService;
		this.attributeService = attributeService;
		this.claszService = claszService;
		this.dataModelService = dataModelService;
		this.schemaService = schemaService;

		eventBus.register(this);
	}

	private void createSchemaFromCsv(final SchemaEvent event) throws DMPPersistenceException, DMPConverterException {

		final DataModel dataModel;

		if (event.getDataModel() != null) {

			dataModel = event.getDataModel();
		} else {

			dataModel = dataModelService.createObject();
		}

		final List<Triple> triples = triplesFromCsv(dataModel.getDataResource(), dataModel.getConfiguration()).orNull();

		if (triples == null) {
			throw new DMPConverterException("could not transform CSV into triples");
		}

		final Schema schema;

		if (dataModel.getSchema() != null) {

			schema = dataModel.getSchema();
		} else {

			schema = schemaService.createObject();
		}

		final String dataResourceBaseSchemaURI = DataModelUtils.determineDataResourceSchemaBaseURI(dataModel);

		final Clasz clasz;

		if (schema.getRecordClass() != null) {

			clasz = schema.getRecordClass();
		} else {

			final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";

			final Clasz newClasz = claszService.createObject(recordClassURI);
			newClasz.setName("record type");

			clasz = newClasz;
		}

		final Set<String> stringAttributes = Sets.newLinkedHashSet();

		for (final Triple triple : triples) {
			stringAttributes.add(triple.getPredicate());
		}

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		for (final String stringAttribute : stringAttributes) {
			final String attributeId = dataResourceBaseSchemaURI + stringAttribute;
			final Attribute attribute = attributeService.createObject(attributeId);

			attribute.setName(stringAttribute);

			final AttributePath attributePath = attributePathService.createObject();
			attributePath.addAttribute(attribute);

			attributePaths.add(attributePath);
		}

		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(clasz);
		schema.setName(dataModel.getDataResource().getName() + " schema");

		dataModel.setSchema(schema);

		if (dataModel.getName() == null) {

			dataModel.setName(dataModel.getDataResource().getName() + " + " + dataModel.getConfiguration().getName() + " data model");
		}

		if (dataModel.getDescription() == null) {

			dataModel.setDescription(" data model of resource '" + dataModel.getDataResource().getName() + "' and configuration ' "
					+ dataModel.getConfiguration().getName() + "'");
		}

		dataModelService.updateObjectTransactional(dataModel);
	}

	private Optional<List<Triple>> triplesFromCsv(final Resource resource, final Configuration configuration) {
		final JsonNode jsonPath = resource.getAttribute("path");

		if (jsonPath == null) {
			LOG.warn("resource does not have a path attribute, did you miss to upload a file?");
			return Optional.absent();
		}

		final String filePath = jsonPath.asText();

		final List<Triple> result;

		try {
			final CSVSourceResourceTriplesFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceTriplesFlow.class);

			result = flow.applyFile(filePath);

		} catch (final DMPConverterException e) {
			LOG.error("could not transform CSV", e);
			return Optional.absent();
		}

		return Optional.of(result);
	}

	@Subscribe
	public void convertSchema(final SchemaEvent event) {

		if (event.getSchemaType() != SchemaEvent.SchemaType.CSV) {

			LOG.info("currently, only CSV is supported. Please come back later");
			return;
		}

		try {
			createSchemaFromCsv(event);
		} catch (final DMPPersistenceException | DMPConverterException e) {
			LOG.error("could not persist schema", e);
		}
	}
}
