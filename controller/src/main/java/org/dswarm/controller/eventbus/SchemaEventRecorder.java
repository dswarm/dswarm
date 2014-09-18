package org.dswarm.controller.eventbus;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.culturegraph.mf.types.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceTriplesFlow;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaService;

@Singleton
public class SchemaEventRecorder {

	private static final Logger			LOG	= LoggerFactory.getLogger(SchemaEventRecorder.class);

	private final AttributePathService	attributePathService;
	private final AttributeService		attributeService;
	private final ClaszService			claszService;
	private final DataModelService		dataModelService;
	private final SchemaService			schemaService;

	@Inject
	public SchemaEventRecorder(final AttributePathService attributePathService, final AttributeService attributeService,
			final ClaszService claszService, final DataModelService dataModelService, final SchemaService schemaService/*
																														 * , final
																														 * EventBus
																														 * eventBus
																														 */) {

		this.attributePathService = attributePathService;
		this.attributeService = attributeService;
		this.claszService = claszService;
		this.dataModelService = dataModelService;
		this.schemaService = schemaService;

		// eventBus.register(this);
	}

	private void createSchemaFromCsv(final SchemaEvent event) throws DMPPersistenceException, DMPConverterException {

		final DataModel dataModel;

		if (event.getDataModel() != null) {

			dataModel = event.getDataModel();
		} else {

			final ProxyDataModel proxyDataModel = dataModelService.createObjectTransactional();

			if (proxyDataModel != null) {

				dataModel = proxyDataModel.getObject();
			} else {

				// TODO: log something?

				dataModel = null;
			}
		}

		final List<Triple> triples = dataModel == null ? null : triplesFromCsv(dataModel.getDataResource(), dataModel.getConfiguration()).orNull();

		if (triples == null) {

			throw new DMPConverterException("could not transform CSV into triples");
		}

		final Schema schema;

		if (dataModel.getSchema() != null) {

			schema = dataModel.getSchema();
		} else {

			final ProxySchema proxySchema = schemaService.createObjectTransactional();

			if (proxySchema != null) {

				schema = proxySchema.getObject();
			} else {

				// TODO: log something?

				schema = null;
			}
		}

		if (schema == null) {

			throw new DMPConverterException("could not transform CSV into triples due to missing schema");
		}

		final String dataResourceBaseSchemaURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);

		final Clasz clasz;

		if (schema.getRecordClass() != null) {

			clasz = schema.getRecordClass();
		} else {

			final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";

			final ProxyClasz proxyNewClasz = claszService.createOrGetObjectTransactional(recordClassURI);

			if (proxyNewClasz == null) {

				throw new DMPPersistenceException("couldn't create or retrieve class");
			}

			final Clasz newClasz = proxyNewClasz.getObject();

			if (proxyNewClasz.getType() == RetrievalType.CREATED) {

				if (newClasz == null) {

					throw new DMPPersistenceException("couldn't create new class");
				}

				newClasz.setName("record type");
			}

			clasz = newClasz;
		}

		final Set<String> stringAttributes = Sets.newLinkedHashSet();

		for (final Triple triple : triples) {
			stringAttributes.add(triple.getPredicate());
		}

		final Set<AttributePath> attributePaths = Sets.newLinkedHashSet();

		for (final String stringAttribute : stringAttributes) {
			final String attributeUri = SchemaUtils.mintTermUri(stringAttribute, dataResourceBaseSchemaURI);
			final ProxyAttribute proxyAttribute = attributeService.createOrGetObjectTransactional(attributeUri);

			if (proxyAttribute == null) {

				throw new DMPPersistenceException("couldn't create or retrieve attribute");
			}

			final Attribute attribute = proxyAttribute.getObject();

			if (proxyAttribute.getType() == RetrievalType.CREATED) {

				if (attribute == null) {

					throw new DMPPersistenceException("couldn't create new attribute");
				}

				attribute.setName(stringAttribute);
			}

			final LinkedList<Attribute> attributes = Lists.newLinkedList();
			attributes.add(attribute);

			final ProxyAttributePath proxyAttributePath = attributePathService.createOrGetObjectTransactional(attributes);

			if (proxyAttributePath == null) {

				throw new DMPPersistenceException("couldn't create or retrieve attribute path");
			}

			final AttributePath attributePath = proxyAttributePath.getObject();

			if (attributePath == null) {

				throw new DMPPersistenceException("couldn't create or retrieve attribute path");
			}

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
			SchemaEventRecorder.LOG.warn("resource does not have a path attribute, did you miss to upload a file?");
			return Optional.absent();
		}

		final String filePath = jsonPath.asText();

		final List<Triple> result;

		try {
			final CSVSourceResourceTriplesFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceTriplesFlow.class);

			result = flow.applyFile(filePath);

		} catch (final DMPConverterException e) {
			SchemaEventRecorder.LOG.error("could not transform CSV", e);
			return Optional.absent();
		}

		return Optional.of(result);
	}

	// @Subscribe
	public void convertSchema(final SchemaEvent event) throws DMPControllerException {

		if (event.getSchemaType() != SchemaEvent.SchemaType.CSV) {

			SchemaEventRecorder.LOG.info("currently, only CSV is supported. Please come back later");
			return;
		}

		try {
			createSchemaFromCsv(event);
		} catch (final DMPPersistenceException | DMPConverterException e) {

			final String message = "could not persist schema";

			SchemaEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		} catch (final Exception e) {

			final String message = "really couldn't convert the schema";

			SchemaEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(message + " " + e.getMessage(), e);
		}
	}
}
