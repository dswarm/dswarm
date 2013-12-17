package de.avgl.dmp.controller.eventbus;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;


import org.culturegraph.mf.types.Triple;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
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

	private final AttributePathService attributePathService;
	private final AttributeService attributeService;
	private final ClaszService claszService;
	private final DataModelService dataModelService;
	private final SchemaService schemaService;

	@Inject
	public SchemaEventRecorder(final AttributePathService attributePathService,
	                           final AttributeService attributeService,
	                           final ClaszService claszService,
	                           final DataModelService dataModelService,
	                           final SchemaService schemaService,
	                           final EventBus eventBus) {

		this.attributePathService = attributePathService;
		this.attributeService = attributeService;
		this.claszService = claszService;
		this.dataModelService = dataModelService;
		this.schemaService = schemaService;

		eventBus.register(this);
	}

	private void createSchemaFromCsv(final SchemaEvent event) throws DMPPersistenceException, DMPConverterException {

		final Resource resource = event.getResource();
		final Configuration configuration = event.getConfiguration();

		final List<Triple> triples = triplesFromCsv(resource, configuration).orNull();

		if (triples == null) {
			throw new DMPConverterException("could not transform CSV into triples");
		}

		final DataModel model = dataModelService.createObject();
		final Schema schema = schemaService.createObject();
		final AttributePath attributePath = attributePathService.createObject();
		final Clasz clasz = claszService.createObject("csv");

		final Set<String> stringAttributes = Sets.newLinkedHashSet();

		for (final Triple triple : triples) {
			stringAttributes.add(triple.getPredicate());
		}

		for (final String stringAttribute : stringAttributes) {
			final String attributeId = "csv:" + stringAttribute;
			final Attribute attribute = attributeService.createObject(attributeId);

			attribute.setName(stringAttribute);
			attributeService.updateObjectTransactional(attribute);

			attributePath.addAttribute(attribute);
		}

		attributePathService.updateObjectTransactional(attributePath);

		schema.addAttributePath(attributePath);
		schema.setRecordClass(clasz);
		schema.setName(configuration.getName());

		schemaService.updateObjectTransactional(schema);

		model.setConfiguration(configuration);
		model.setDataResource(resource);
		model.setSchema(schema);
		model.setName(resource.getName());
		model.setDescription(resource.getDescription());

		dataModelService.updateObjectTransactional(model);
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
