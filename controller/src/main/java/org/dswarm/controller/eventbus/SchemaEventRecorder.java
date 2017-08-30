/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.eventbus;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
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
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

@Singleton
public class SchemaEventRecorder {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaEventRecorder.class);

	private final Provider<CSVResourceFlowFactory>            flowFactory2;
	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProvider;
	private final Provider<AttributePathService>               attributePathServiceProvider;
	private final Provider<AttributeService>                   attributeServiceProvider;
	private final Provider<ClaszService>                       claszServiceProvider;
	private final Provider<DataModelService>                   dataModelServiceProvider;
	private final Provider<SchemaService>                      schemaServiceProvider;

	@Inject
	public SchemaEventRecorder(
			final Provider<CSVResourceFlowFactory> flowFactory2,
			final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceService,
			final Provider<AttributePathService> attributePathService,
			final Provider<AttributeService> attributeService,
			final Provider<ClaszService> claszService,
			final Provider<DataModelService> dataModelService,
			final Provider<SchemaService> schemaService) {

		this.flowFactory2 = flowFactory2;
		this.schemaAttributePathInstanceServiceProvider = schemaAttributePathInstanceService;
		this.attributePathServiceProvider = attributePathService;
		this.attributeServiceProvider = attributeService;
		this.claszServiceProvider = claszService;
		this.dataModelServiceProvider = dataModelService;
		this.schemaServiceProvider = schemaService;

		// eventBus.register(this);
	}

	private void createSchemaFromCsv(final SchemaEvent event) throws DMPPersistenceException, DMPConverterException {

		final DataModel dataModel;

		if (event.getDataModel() != null) {

			dataModel = event.getDataModel();
		} else {

			final ProxyDataModel proxyDataModel = dataModelServiceProvider.get().createObjectTransactional();

			if (proxyDataModel != null) {

				dataModel = proxyDataModel.getObject();
			} else {

				// TODO: log something?

				dataModel = null;
			}
		}

		createSchemaFromCsv(dataModel);
	}
	private void createSchemaFromCsv(final DataModel dataModel) throws DMPPersistenceException, DMPConverterException {

		final Collection<Triple> triples = dataModel == null ? null : triplesFromCsv(dataModel.getDataResource(), dataModel.getConfiguration()).orNull();

		if (triples == null) {

			throw new DMPConverterException("could not transform CSV into triples");
		}

		final Schema schema;

		if (dataModel.getSchema() != null) {

			schema = dataModel.getSchema();
		} else {

			final ProxySchema proxySchema = schemaServiceProvider.get().createObjectTransactional();

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

		// set schema at data model so that it can be utilised for data model schema base URI determination
		dataModel.setSchema(schema);

		final String dataResourceBaseSchemaURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);

		final Clasz clasz;

		if (schema.getRecordClass() != null) {

			clasz = schema.getRecordClass();
		} else {

			final String recordClassURI = dataResourceBaseSchemaURI + "RecordType";

			final ProxyClasz proxyNewClasz = claszServiceProvider.get().createOrGetObjectTransactional(recordClassURI);

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

		stringAttributes.addAll(triples.stream().map(Triple::getPredicate).collect(Collectors.toList()));

		final AttributeService attributeService = attributeServiceProvider.get();
		final AttributePathService attributePathService = attributePathServiceProvider.get();
		final SchemaAttributePathInstanceService schemaAttributePathInstanceService = schemaAttributePathInstanceServiceProvider.get();

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

			final Boolean required = null;
			final Boolean multivalue = null;

			SchemaUtils.addAttributePaths(schema, attributes, required, multivalue, attributePathService, schemaAttributePathInstanceService);
		}

		schema.setRecordClass(clasz);
		schema.setName(dataModel.getDataResource().getName() + " schema");

		if (dataModel.getName() == null) {

			dataModel.setName(dataModel.getDataResource().getName() + " + " + dataModel.getConfiguration().getName() + " data model");
		}

		if (dataModel.getDescription() == null) {

			dataModel.setDescription(" data model of resource '" + dataModel.getDataResource().getName() + "' and configuration ' "
					+ dataModel.getConfiguration().getName() + "'");
		}

		dataModelServiceProvider.get().updateObjectTransactional(dataModel);
	}

	private Optional<Collection<Triple>> triplesFromCsv(final Resource resource, final Configuration configuration) {
		final JsonNode jsonPath = resource.getAttribute("path");

		if (jsonPath == null) {
			SchemaEventRecorder.LOG.warn("resource does not have a path attribute, did you miss to upload a file?");
			return Optional.absent();
		}

		final String filePath = jsonPath.asText();

		final Collection<Triple> result;

		try {
			final CSVSourceResourceTriplesFlow flow = flowFactory2.get().fromConfiguration(configuration);

			// TODO: change, if necessary
			result = flow.applyFile(filePath).toBlocking().first();

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
