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

	private static final Logger LOG = LoggerFactory.getLogger(SchemaUtils.class);

	public static String determineRelativeURIPart(final String uri) {

		final String lastPartDelimiter;

		if (uri.lastIndexOf("#") > 0) {

			lastPartDelimiter = "#";
		} else if (uri.lastIndexOf("/") > 0) {

			lastPartDelimiter = "/";
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

	public static String determineSchemaNamespaceURI(final Long schemaId) {

		return "http://data.slub-dresden.de/schemas/" + schemaId + "/";
	}

	public static String mintAttributeURI(final String attributeName, final String namespaceURI) {

		final String attributeNameURLEncoded = UrlEscapers.urlFormParameterEscaper().escape(attributeName);

		return namespaceURI + attributeNameURLEncoded;
	}

	public static String mintTermUri(final String possibleTermURI, final String baseURI) {

		return mintTermUri(possibleTermURI, possibleTermURI, baseURI);
	}

	public static String mintSchemaTermURI(final String possibleTermURI, final Long schemaId) {

		final String schemaNamespaceURI = determineSchemaNamespaceURI(schemaId);

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

			SchemaUtils.LOG.debug("couldn't determine attribute paths for schema '" + schema.getId() + "'");

			return false;
		}

		if (attributePathHelpers.isEmpty()) {

			SchemaUtils.LOG.debug("there are no attribute paths for schema '" + schema.getId() + "'");
		}

		for (final AttributePathHelper attributePathHelper : attributePathHelpers) {

			final LinkedList<Attribute> attributes = Lists.newLinkedList();

			final LinkedList<String> attributePathFromHelper = attributePathHelper.getAttributePath();

			if (attributePathFromHelper.isEmpty()) {

				SchemaUtils.LOG.debug("there are no attributes for this attribute path for schema '" + schema.getId() + "'");
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
		if (uri != null && uri.endsWith("/")) {

			return uri + finalLocalName;
		}

		if (localName.startsWith("#")) {

			return uri + localName;
		}

		if(uri != null && uri.endsWith("#")) {

			return uri + finalLocalName;
		}

		return uri + "#" + finalLocalName;
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

			return String.format("http://data.slub-dresden.de/terms/%s", UUID.randomUUID());
		}

		if (canUseLocalName) {

			return SchemaUtils.mintUri(uri, localName);
		} else {

			return String.format("http://data.slub-dresden.de/terms/%s", UUID.randomUUID());
		}
	}

	public static Schema updateSchema(final Schema schema, final Provider<SchemaService> schemaServiceProvider) throws DMPPersistenceException {

		final ProxySchema proxyUpdatedSchema = schemaServiceProvider.get().updateObjectTransactional(schema);

		if (proxyUpdatedSchema == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedSchema.getObject();
	}

	private static String mintTermUri(final String termUri, final String localTermName, final String baseUri) {

		final boolean isValidURI = isValidUri(localTermName);

		if (isValidURI) {

			return localTermName;
		} else {

			final Optional<String> optionalBaseUri = Optional.fromNullable(baseUri);

			final boolean isValidTermURI = isValidUri(termUri);

			if(isValidTermURI) {

				return mintTermUri(termUri, localTermName, optionalBaseUri);
			} else {

				return mintTermUri(null, localTermName, optionalBaseUri);
			}
		}
	}

}
