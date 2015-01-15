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
package org.dswarm.persistence.model.schema.utils;

import java.net.URI;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.model.utils.BasicDMPJPAObjectUtils;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 */
public final class SchemaUtils extends BasicDMPJPAObjectUtils<Schema> {

	private static final Logger LOG                 = LoggerFactory.getLogger(SchemaUtils.class);
	public static final  String BASE_URI            = "http://data.slub-dresden.de/";
	private static final String RECORD_BASE_URI     = BASE_URI + "records/";
	public static final  String DATA_MODEL_BASE_URI = BASE_URI + "datamodels/";
	private static final String RECORD_RELATIVE_URI = "/records/";
	private static final String TERM_BASE_URI       = BASE_URI + "terms/%s";
	public static final  String HASH                = "#";
	public static final  String SLASH               = "/";
	public static final  String TYPE_POSTFIX        = "Type";
	private static final String SCHEMA_BASE_URI     = BASE_URI + "schemas/";

	public static final String MABXML_SCHEMA_UUID = "Schema-d87ba5c2-b02b-481d-a62d-2b46dd66d347";
	public static final String BIBRM_CONTRACT_ITEM_SCHEMA_UUID = "Schema-70228b28-10fc-43fe-9d3e-ad22b038ebdf";
	public static final String BIBO_DOCUMENT_SCHEMA_UUID = "Schema-ff62ec21-0a11-4c27-a704-d7ca53a21521";
	public final static String FOAF_SCHEMA_UUID = "Schema-309e901c-3da9-4d82-a694-bab632eaa340";
	public static final String PNX_SCHEMA_UUID = "Schema-dbc97499-278d-4551-a65e-8e8bb219ca6c";
	public static final String MARC21_SCHEMA_UUID = "Schema-781d73f0-d115-462e-9b4c-ec23e4251c8d";

	public static String determineRelativeURIPart(final String uri) {

		final String lastPartDelimiter;

		if (uri.lastIndexOf(HASH) > 0) {

			lastPartDelimiter = HASH;
		} else if (uri.lastIndexOf(SLASH) > 0) {

			lastPartDelimiter = SLASH;
		} else {

			lastPartDelimiter = null;
		}

		final String relativeURIPart;

		if (lastPartDelimiter != null) {

			relativeURIPart = uri.substring(uri.lastIndexOf(lastPartDelimiter) + 1, uri.length());
		} else {

			relativeURIPart = uri;
		}

		return relativeURIPart;
	}

	public static String determineSchemaNamespaceURI(final String schemaUuid) {

		return SCHEMA_BASE_URI + schemaUuid + SLASH;
	}

	public static String mintAttributeURI(final String attributeName, final String namespaceURI) {

		final String attributeNameURLEncoded = UrlEscapers.urlFormParameterEscaper().escape(attributeName);

		return namespaceURI + attributeNameURLEncoded;
	}

	public static String mintTermUri(final String possibleTermURI, final String baseURI) {

		return mintTermUri(possibleTermURI, possibleTermURI, baseURI);
	}

	public static String mintSchemaTermURI(final String possibleTermURI, final String schemaUuid) {

		final String schemaNamespaceURI = determineSchemaNamespaceURI(schemaUuid);

		return mintTermUri(possibleTermURI, possibleTermURI, schemaNamespaceURI);
	}

	public static boolean addRecordClass(final Schema schema, final String recordClassUri, final Provider<ClaszService> classServiceProvider)
			throws DMPPersistenceException {

		final Clasz recordClass;

		if (schema.getRecordClass() != null) {

			if (schema.getRecordClass().getUri().equals(recordClassUri)) {

				// nothing to do, record class is already set

				return false;
			}
		} else {

			// create new class
			final ProxyClasz proxyRecordClass = classServiceProvider.get().createOrGetObjectTransactional(recordClassUri);

			if (proxyRecordClass == null) {

				throw new DMPPersistenceException("couldn't create or retrieve record class");
			}

			recordClass = proxyRecordClass.getObject();

			if (proxyRecordClass.getType().equals(RetrievalType.CREATED)) {

				if (recordClass == null) {

					throw new DMPPersistenceException("couldn't create new record class");
				}

				final String recordClassName = SchemaUtils.determineRelativeURIPart(recordClassUri);

				recordClass.setName(recordClassName);
			}

			schema.setRecordClass(recordClass);
		}

		return true;
	}

	public static boolean addAttributePaths(
			final Schema schema,
			final Set<AttributePathHelper> attributePathHelpers,
			final Provider<AttributePathService> attributePathServiceProvider,
			final Provider<SchemaAttributePathInstanceService> attributePathInstanceServiceProvider,
			final Provider<AttributeService> attributeServiceProvider)
			throws DMPPersistenceException {

		if (attributePathHelpers == null) {

			SchemaUtils.LOG.debug("couldn't determine attribute paths for schema '" + schema.getUuid() + "'");

			return false;
		}

		if (attributePathHelpers.isEmpty()) {

			SchemaUtils.LOG.debug("there are no attribute paths for schema '" + schema.getUuid() + "'");
		}

		for (final AttributePathHelper attributePathHelper : attributePathHelpers) {

			System.out.println(attributePathHelper.toString());

			final LinkedList<Attribute> attributes = Lists.newLinkedList();

			final LinkedList<String> attributePathFromHelper = attributePathHelper.getAttributePath();

			if (attributePathFromHelper.isEmpty()) {

				SchemaUtils.LOG.debug("there are no attributes for this attribute path for schema '" + schema.getUuid() + "'");
			}

			final AttributeService attributeService = attributeServiceProvider.get();
			final AttributePathService attributePathService = attributePathServiceProvider.get();
			final SchemaAttributePathInstanceService schemaAttributePathInstanceService =
					attributePathInstanceServiceProvider.get();

			for (final String attributeString : attributePathFromHelper) {

				final ProxyAttribute proxyAttribute = attributeService.createOrGetObjectTransactional(attributeString);

				if (proxyAttribute == null) {

					throw new DMPPersistenceException("couldn't create or retrieve attribute");
				}

				final Attribute attribute = proxyAttribute.getObject();

				if (attribute == null) {

					throw new DMPPersistenceException("couldn't create or retrieve attribute");
				}

				attributes.add(attribute);

				final String attributeName = SchemaUtils.determineRelativeURIPart(attributeString);

				attribute.setName(attributeName);
			}

			addAttributePaths(schema, attributes, attributePathService, schemaAttributePathInstanceService);
		}

		return true;
	}

	public static void addAttributePaths(final Schema schema, final LinkedList<Attribute> attributes, final AttributePathService attributePathService,
			final SchemaAttributePathInstanceService schemaAttributePathInstanceService) throws DMPPersistenceException {

		final SchemaAttributePathInstance schemaAttributePathInstance = createSchemaAttributePathInstance(attributes, attributePathService,
				schemaAttributePathInstanceService);

		schema.addAttributePath(schemaAttributePathInstance);
	}

	public static SchemaAttributePathInstance createSchemaAttributePathInstance(final LinkedList<Attribute> attributes,
			final AttributePathService attributePathService, final SchemaAttributePathInstanceService schemaAttributePathInstanceService)
			throws DMPPersistenceException {

		final ProxyAttributePath proxyAttributePath = attributePathService.createOrGetObjectTransactional(attributes);

		if (proxyAttributePath == null) {

			throw new DMPPersistenceException("couldn't create or retrieve attribute path");
		}

		final AttributePath attributePath = proxyAttributePath.getObject();

		if (attributePath == null) {

			throw new DMPPersistenceException("couldn't create or retrieve attribute path");
		}

		final ProxySchemaAttributePathInstance proxySchemaAttributePathInstance =
				schemaAttributePathInstanceService.createObjectTransactional(attributePath);

		if (proxySchemaAttributePathInstance == null) {

			throw new DMPPersistenceException("couldn't create or retrieve schema attribute path instance");

		}

		final SchemaAttributePathInstance schemaAttributePathInstance = proxySchemaAttributePathInstance.getObject();

		if (schemaAttributePathInstance == null) {

			throw new DMPPersistenceException("couldn't create or retrieve schema attribute path instance");

		}
		return schemaAttributePathInstance;
	}

	public static boolean isValidUri(@Nullable final String identifier) {
		if (identifier != null) {
			try {
				final URI _uri = URI.create(identifier);

				return _uri != null && _uri.getScheme() != null;
			} catch (final IllegalArgumentException e) {

				return false;
			}
		}

		return false;
	}

	public static String mintUri(final String uri, final String localName) {

		final String finalLocalName = UrlEscapers.urlFormParameterEscaper().escape(localName);

		// allow has and slash uris
		if (uri != null && uri.endsWith(SLASH)) {

			return uri + finalLocalName;
		}

		if (localName.startsWith(HASH)) {

			return uri + localName;
		}

		if (uri != null && uri.endsWith(HASH)) {

			return uri + finalLocalName;
		}

		return uri + HASH + finalLocalName;
	}

	public static String mintTermUri(@Nullable final String uri, @Nullable final String localName, Optional<String> baseURI) {

		final boolean canUseLocalName = !Strings.isNullOrEmpty(localName);

		if (Strings.isNullOrEmpty(uri)) {

			if (baseURI.isPresent()) {

				if (canUseLocalName) {

					return SchemaUtils.mintUri(baseURI.get(), localName);
				} else {

					return SchemaUtils.mintUri(baseURI.get(), UUID.randomUUID().toString());
				}
			}

			return String.format(TERM_BASE_URI, UUID.randomUUID());
		}

		if (canUseLocalName) {

			return SchemaUtils.mintUri(uri, localName);
		} else {

			return String.format(TERM_BASE_URI, UUID.randomUUID());
		}
	}

	public static Schema updateSchema(final Schema schema, final Provider<SchemaService> schemaServiceProvider) throws DMPPersistenceException {

		final ProxySchema proxyUpdatedSchema = schemaServiceProvider.get().updateObjectTransactional(schema);

		if (proxyUpdatedSchema == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedSchema.getObject();
	}

	public static String mintRecordUri(@Nullable final String identifier, final String currentId, final Optional<DataModel> optionalDataModel) {

		if (currentId == null) {

			// mint completely new uri

			final StringBuilder sb = new StringBuilder();

			if (optionalDataModel.isPresent()) {

				// create uri from resource id and configuration id and random uuid

				sb.append(DATA_MODEL_BASE_URI).append(optionalDataModel.get().getUuid()).append(RECORD_RELATIVE_URI);
			} else {

				// create uri from random uuid

				sb.append(RECORD_BASE_URI);
			}

			return sb.append(UUID.randomUUID()).toString();
		}

		// create uri with help of given record id

		final StringBuilder sb = new StringBuilder();

		if (optionalDataModel.isPresent()) {

			// create uri from resource id and configuration id and identifier

			sb.append(DATA_MODEL_BASE_URI).append(optionalDataModel.get().getUuid()).append(RECORD_RELATIVE_URI).append(identifier);
		} else {

			// create uri from identifier

			sb.append(RECORD_BASE_URI).append(identifier);
		}

		return sb.toString();
	}

	private static String mintTermUri(final String termUri, final String localTermName, final String baseUri) {

		final boolean isValidURI = isValidUri(localTermName);

		if (isValidURI) {

			return localTermName;
		} else {

			final Optional<String> optionalBaseUri = Optional.fromNullable(baseUri);

			final boolean isValidTermURI = isValidUri(termUri);

			if (isValidTermURI) {

				return mintTermUri(termUri, localTermName, optionalBaseUri);
			} else {

				return mintTermUri(null, localTermName, optionalBaseUri);
			}
		}
	}

}
