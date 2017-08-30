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
package org.dswarm.persistence.model.schema.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.net.UrlEscapers;
import com.google.inject.Provider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
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

	private static final Logger LOG = LoggerFactory.getLogger(SchemaUtils.class);
	public static final String BASE_URI = "http://data.slub-dresden.de/";
	private static final String RECORD_BASE_URI = BASE_URI + "records/";
	public static final String DATA_MODEL_BASE_URI = BASE_URI + "datamodels/";
	private static final String RECORD_RELATIVE_URI = "/records/";
	private static final String TERM_BASE_URI = BASE_URI + "terms/%s";
	public static final String HASH = "#";
	public static final String SLASH = "/";
	public static final String AT = "@";
	public static final String TYPE_POSTFIX = "Type";
	private static final String SCHEMA_BASE_URI = BASE_URI + "schemas/";

	public static final String MABXML_SCHEMA_UUID = "Schema-d87ba5c2-b02b-481d-a62d-2b46dd66d347";
	public static final String BIBRM_CONTRACT_ITEM_SCHEMA_UUID = "Schema-70228b28-10fc-43fe-9d3e-ad22b038ebdf";
	public static final String BIBO_DOCUMENT_SCHEMA_UUID = "Schema-ff62ec21-0a11-4c27-a704-d7ca53a21521";
	public static final String FOAF_SCHEMA_UUID = "Schema-309e901c-3da9-4d82-a694-bab632eaa340";
	public static final String PNX_SCHEMA_UUID = "Schema-dbc97499-278d-4551-a65e-8e8bb219ca6c";
	public static final String MARCXML_SCHEMA_UUID = "Schema-781d73f0-d115-462e-9b4c-ec23e4251c8d";
	public static final String PICAPLUSXML_SCHEMA_UUID = "Schema-b02b22bf-4657-4853-9263-d2acf7fdae4d";
	public static final String PICAPLUSXML_GLOBAL_SCHEMA_UUID = "Schema-435ea7b2-f377-4545-9f0c-91c0ce95371a";
	public static final String FINC_SOLR_SCHEMA_UUID = "Schema-5664ba0e-ccb3-4b71-8823-13281490de30";
	public static final String OAI_PMH_DC_ELEMENTS_SCHEMA_UUID = "Schema-cb8f4b96-9ab2-4972-88f8-143656199518";
	public static final String OAI_PMH_DC_TERMS_SCHEMA_UUID = "Schema-8fefbced-c2f2-478c-a22a-debb122e05de";
	public static final String OAI_PMH_MARCXML_SCHEMA_UUID = "Schema-5ca8e59f-0f40-4f17-8237-e5d0a6e83f18";
	public static final String SRU_11_PICAPLUSXML_GLOBAL_SCHEMA_UUID = "Schema-93cd8d2a-a583-48f1-83e3-2867e9f85ec8";
	public static final String OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID = "Schema-e6d4ff86-07d9-494f-9299-9d67d3a0d9e8";
	public static final String UBL_INTERMEDIATE_FORMAT_SCHEMA_UUID = "Schema-d06726be-a8e2-412a-b5e7-76cba340108b";
	public static final String SPRINGER_JOURNALS_SCHEMA_UUID = "Schema-f7fd6d5d-9b85-4103-85d0-f062c8de52f8";

	private static final Collection<String> inbuiltSchemaUuids = new ArrayList<>();

	static {

		// add all inbuilt schema uuids here
		inbuiltSchemaUuids.add(MABXML_SCHEMA_UUID);
		inbuiltSchemaUuids.add(BIBRM_CONTRACT_ITEM_SCHEMA_UUID);
		inbuiltSchemaUuids.add(BIBO_DOCUMENT_SCHEMA_UUID);
		inbuiltSchemaUuids.add(FOAF_SCHEMA_UUID);
		inbuiltSchemaUuids.add(PNX_SCHEMA_UUID);
		inbuiltSchemaUuids.add(MARCXML_SCHEMA_UUID);
		inbuiltSchemaUuids.add(PICAPLUSXML_SCHEMA_UUID);
		inbuiltSchemaUuids.add(PICAPLUSXML_GLOBAL_SCHEMA_UUID);
		inbuiltSchemaUuids.add(FINC_SOLR_SCHEMA_UUID);
		inbuiltSchemaUuids.add(OAI_PMH_DC_ELEMENTS_SCHEMA_UUID);
		inbuiltSchemaUuids.add(OAI_PMH_DC_TERMS_SCHEMA_UUID);
		inbuiltSchemaUuids.add(OAI_PMH_MARCXML_SCHEMA_UUID);
		inbuiltSchemaUuids.add(SRU_11_PICAPLUSXML_GLOBAL_SCHEMA_UUID);
		inbuiltSchemaUuids.add(OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID);
		inbuiltSchemaUuids.add(UBL_INTERMEDIATE_FORMAT_SCHEMA_UUID);
		inbuiltSchemaUuids.add(SPRINGER_JOURNALS_SCHEMA_UUID);
	}

	public static Collection<String> getInbuiltSchemaUuids() {

		return inbuiltSchemaUuids;
	}

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

	public static String mintSchemaTermURI(final String possibleTermURI, final String schemaUuid, Optional<String> optionalBaseURI) {

		final String schemaNamespaceURI;

		if(optionalBaseURI.isPresent() && !optionalBaseURI.get().trim().isEmpty()) {

			schemaNamespaceURI = optionalBaseURI.get();
		} else {

			schemaNamespaceURI = determineSchemaNamespaceURI(schemaUuid);
		}

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

	public static boolean addAttributePaths(final Schema schema,
	                                        final Set<AttributePathHelper> attributePathHelpers,
	                                        final Provider<AttributePathService> attributePathServiceProvider,
	                                        final Provider<SchemaAttributePathInstanceService> attributePathInstanceServiceProvider,
	                                        final Provider<AttributeService> attributeServiceProvider) throws DMPPersistenceException {


		return addAttributePaths(schema, attributePathHelpers, attributePathServiceProvider, attributePathInstanceServiceProvider, attributeServiceProvider, Optional.empty());
	}

	public static boolean addAttributePaths(final Schema schema,
	                                        final Set<AttributePathHelper> attributePathHelpers,
	                                        final Provider<AttributePathService> attributePathServiceProvider,
	                                        final Provider<SchemaAttributePathInstanceService> attributePathInstanceServiceProvider,
	                                        final Provider<AttributeService> attributeServiceProvider,
	                                        final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {


		return addAttributePaths(schema, attributePathHelpers, attributePathServiceProvider, attributePathInstanceServiceProvider, attributeServiceProvider, optionalAttributePathsSAPIUUIDs, Optional.empty());
	}

	public static boolean addAttributePaths(final Schema schema,
	                                        final Set<AttributePathHelper> attributePathHelpers,
	                                        final Provider<AttributePathService> attributePathServiceProvider,
	                                        final Provider<SchemaAttributePathInstanceService> attributePathInstanceServiceProvider,
	                                        final Provider<AttributeService> attributeServiceProvider,
	                                        final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                        final Optional<Set<String>> optionalExcludeAttributePathStubs) throws DMPPersistenceException {

		if (attributePathHelpers == null) {

			SchemaUtils.LOG.debug("couldn't determine attribute paths for schema '{}'", schema.getUuid());

			return false;
		}

		if (attributePathHelpers.isEmpty()) {

			SchemaUtils.LOG.debug("there are no attribute paths for schema '{}'", schema.getUuid());

			return true;
		}

		final AttributeService attributeService = attributeServiceProvider.get();
		final AttributePathService attributePathService = attributePathServiceProvider.get();
		final SchemaAttributePathInstanceService schemaAttributePathInstanceService =
				attributePathInstanceServiceProvider.get();

		for (final AttributePathHelper attributePathHelper : attributePathHelpers) {

			final String attributePathString = attributePathHelper.toString();

			if (attributePathString == null || attributePathString.trim().isEmpty()) {

				SchemaUtils.LOG.debug("attribute path is non-existent or empty for schema '{}'", schema.getUuid());

				continue;
			}

			if (optionalExcludeAttributePathStubs.isPresent()) {

				boolean excludeAttributePath = false;

				final Set<String> excludeAttributePathStubs = optionalExcludeAttributePathStubs.get();

				for (final String excludeAttributePathStub : excludeAttributePathStubs) {

					if (attributePathString.startsWith(excludeAttributePathStub)) {

						excludeAttributePath = true;

						break;
					}
				}

				if (excludeAttributePath) {

					// don't add and persist this attribute path to the schema, i.e., exclude it

					continue;
				}
			}

			final SchemaAttributePathInstance attributePathByURIPath = schema.getAttributePathByURIPath(attributePathString);

			if (attributePathByURIPath != null) {

				// attribute path is already in schema

				continue;
			}

			if (optionalAttributePathsSAPIUUIDs.isPresent()) {

				final Map<String, String> attributePathsSAPIUUIDs = optionalAttributePathsSAPIUUIDs.get();

				final Optional<String> optionalSAPIUUID = Optional.ofNullable(attributePathsSAPIUUIDs.getOrDefault(attributePathString, null));

				if (optionalSAPIUUID.isPresent()) {

					// try to retrieve existing SAPI
					final Optional<SchemaAttributePathInstance> optionalSAPI = Optional.ofNullable(schemaAttributePathInstanceService.getObject(optionalSAPIUUID.get()));

					if (optionalSAPI.isPresent()) {

						// add existing SAPI to schema
						schema.addAttributePath(optionalSAPI.get());

						continue;
					}
				}
			}

			final List<String> attributePathFromHelper = attributePathHelper.getAttributePath();

			if (attributePathFromHelper.isEmpty()) {

				SchemaUtils.LOG.debug("there are no attributes for this attribute path for schema '{}'", schema.getUuid());

				continue;
			}

			final List<Attribute> attributes = new ArrayList<>();

			for (final String attributeString : attributePathFromHelper) {

				final String attributeURI = SchemaUtils.mintSchemaTermURI(attributeString, schema.getUuid(), Optional.ofNullable(schema.getBaseURI()));

				final ProxyAttribute proxyAttribute = attributeService.createOrGetObjectTransactional(attributeURI);

				if (proxyAttribute == null) {

					throw new DMPPersistenceException("couldn't create or retrieve attribute");
				}

				final Attribute attribute = proxyAttribute.getObject();

				if (attribute == null) {

					throw new DMPPersistenceException("couldn't create or retrieve attribute");
				}

				attributes.add(attribute);

				final String attributeName = SchemaUtils.determineRelativeURIPart(attributeURI);

				attribute.setName(attributeName);
			}

			final Boolean required = attributePathHelper.isRequired();
			final Boolean multivalue = attributePathHelper.isMultivalue();

			addAttributePaths(schema, attributes, required, multivalue, attributePathService, schemaAttributePathInstanceService);
		}

		return true;
	}

	public static AttributePath addAttributePaths(final Schema schema,
	                                              final List<Attribute> attributes,
	                                              final Boolean required,
	                                              final Boolean multivalue,
	                                              final AttributePathService attributePathService,
	                                              final SchemaAttributePathInstanceService schemaAttributePathInstanceService,
	                                              final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		final SchemaAttributePathInstance schemaAttributePathInstance = createOrGetSchemaAttributePathInstance(attributes, required, multivalue, attributePathService, schemaAttributePathInstanceService, optionalAttributePathsSAPIUUIDs);

		schema.addAttributePath(schemaAttributePathInstance);

		return schemaAttributePathInstance.getAttributePath();
	}

	public static AttributePath addAttributePaths(final Schema schema,
	                                              final List<Attribute> attributes,
	                                              final Boolean required,
	                                              final Boolean multivalue,
	                                              final AttributePathService attributePathService,
	                                              final SchemaAttributePathInstanceService schemaAttributePathInstanceService) throws DMPPersistenceException {

		return addAttributePaths(schema, attributes, required, multivalue, attributePathService, schemaAttributePathInstanceService, Optional.empty());
	}

	public static SchemaAttributePathInstance createOrGetSchemaAttributePathInstance(final List<Attribute> attributes,
	                                                                                 final Boolean required,
	                                                                                 final Boolean multivalue,
	                                                                                 final AttributePathService attributePathService,
	                                                                                 final SchemaAttributePathInstanceService schemaAttributePathInstanceService,
	                                                                                 final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		if (optionalAttributePathsSAPIUUIDs.isPresent()) {

			final String attributePathString = AttributePathUtils.generateAttributePath(attributes);

			final Map<String, String> attributePathsSAPIUUIDs = optionalAttributePathsSAPIUUIDs.get();

			final Optional<String> optionalSAPIUUID = Optional.ofNullable(attributePathsSAPIUUIDs.getOrDefault(attributePathString, null));

			if (optionalSAPIUUID.isPresent()) {

				// try to retrieve existing SAPI
				final Optional<SchemaAttributePathInstance> optionalSAPI = Optional.ofNullable(schemaAttributePathInstanceService.getObject(optionalSAPIUUID.get()));

				if (optionalSAPI.isPresent()) {

					return optionalSAPI.get();
				}
			}
		}

		final ProxyAttributePath proxyAttributePath = attributePathService.createOrGetObjectTransactional(attributes);

		if (proxyAttributePath == null) {

			throw new DMPPersistenceException("couldn't create or retrieve attribute path");
		}

		final AttributePath attributePath = proxyAttributePath.getObject();

		if (attributePath == null) {

			throw new DMPPersistenceException("couldn't create or retrieve attribute path");
		}

		final ProxySchemaAttributePathInstance proxySchemaAttributePathInstance =
				schemaAttributePathInstanceService.createObjectTransactional(attributePath, required, multivalue);

		if (proxySchemaAttributePathInstance == null) {

			throw new DMPPersistenceException("couldn't create or retrieve schema attribute path instance");

		}

		final SchemaAttributePathInstance schemaAttributePathInstance = proxySchemaAttributePathInstance.getObject();

		if (schemaAttributePathInstance == null) {

			throw new DMPPersistenceException("couldn't create or retrieve schema attribute path instance");

		}
		return schemaAttributePathInstance;
	}


	public static SchemaAttributePathInstance createSchemaAttributePathInstance(final List<Attribute> attributes,
	                                                                            final AttributePathService attributePathService,
	                                                                            final SchemaAttributePathInstanceService schemaAttributePathInstanceService) throws DMPPersistenceException {

		final Boolean required = null;
		final Boolean multivalue = null;

		return createOrGetSchemaAttributePathInstance(attributes, required, multivalue, attributePathService, schemaAttributePathInstanceService, Optional.empty());
	}

	public static boolean isValidUri(@Nullable final String identifier) {
		if (identifier != null) {
			try {
				final URI _uri = URI.create(identifier);

				return _uri.getScheme() != null;
			} catch (final IllegalArgumentException e) {

				return false;
			}
		}

		return false;
	}

	public static String mintUri(final String uri, final String localName) {

		// 1. uri ends with slash
		// 2. uri ends with hash
		// 3. local name starts with hash (#)
		// 4. local name starts with at (@)

		final boolean localNameStartsWithHash = localName.startsWith(HASH);
		final boolean localNameStartsWithAt = localName.startsWith(AT);

		// allow hash and slash uris
		if (uri != null && uri.endsWith(SLASH)) {

			final String finalLocalNameForSlash;

			if(localNameStartsWithHash) {

				finalLocalNameForSlash = StringUtils.stripStart(localName, HASH);
			} else if(localNameStartsWithAt) {

				finalLocalNameForSlash = StringUtils.stripStart(localName, AT);
			} else {

				finalLocalNameForSlash = localName;
			}

			final String finalLocalNameForSlashEscaped = UrlEscapers.urlFormParameterEscaper().escape(finalLocalNameForSlash);

			return uri + finalLocalNameForSlashEscaped;
		}


		if (localNameStartsWithHash) {

			return uri + localName;
		}

		final String finalLocalName = UrlEscapers.urlFormParameterEscaper().escape(localName);

		if (uri != null && uri.endsWith(HASH)) {

			return uri + finalLocalName;
		}

		return uri + HASH + finalLocalName;
	}

	public static String mintTermUri(@Nullable final String uri, @Nullable final String localName, final Optional<String> baseURI) {

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

	public static Map<String, AttributePath> generateAttributePathMap(final Schema schema) {

		final Map<String, AttributePath> aps = Maps.newHashMap();

		for (final SchemaAttributePathInstance schemaAttributePathInstance : schema.getAttributePaths()) {

			final AttributePath attributePath = schemaAttributePathInstance.getAttributePath();
			aps.put(attributePath.toAttributePath(), attributePath);
		}

		return aps;
	}

	/**
	 * returns an attribute map with local attribute names as key and attribute objects as values of an entry. Incl. record class.
	 * <p>
	 * note: if a local name occurs multiple times, this won't be handles right now
	 *
	 * @param schema
	 * @return
	 */
	public static Map<String, AdvancedDMPJPAObject> generateTermMap(final Schema schema) {

		if (schema == null) {

			return null;
		}

		final Map<String, AdvancedDMPJPAObject> termMap = new HashMap<>();

		generateAttributeMap(schema, termMap);

		final Clasz recordClass = schema.getRecordClass();

		if (recordClass != null) {

			termMap.put(recordClass.getName(), recordClass);
		}

		return termMap;
	}

	/**
	 * returns an attribute map with local attribute names as key and attribute objects as values of an entry.
	 * <p>
	 * note: if a local name occurs multiple times, this won't be handles right now
	 *
	 * @param schema
	 * @return
	 */
	public static Map<String, Attribute> generateAttributeMap(final Schema schema) {

		if (schema == null) {

			return null;
		}

		final Map<String, Attribute> termMap = new HashMap<>();

		generateAttributeMap(schema, termMap);

		return termMap;
	}

	/**
	 * returns an attribute map with attribute uuids as key and attribute objects as values of an entry.
	 *
	 * @param schema
	 * @return
	 */
	public static Map<String, Attribute> generateAttributeMap2(final Schema schema) {

		if (schema == null) {

			return null;
		}

		final Map<String, Attribute> termMap = new HashMap<>();

		generateAttributeMap2(schema, termMap);

		return termMap;
	}

	/**
	 * returns an attribute map with local attribute names as key and attribute objects as values of an entry.
	 * <p>
	 * note: if a local name occurs multiple times, this won't be handles right now
	 *
	 * @param schema
	 * @return
	 */
	private static <T extends AdvancedDMPJPAObject> void generateAttributeMap(final Schema schema, final Map<String, T> termMap) {

		final Collection<SchemaAttributePathInstance> attributePaths = schema.getAttributePaths();

		if (attributePaths == null) {

			return;
		}

		for (final SchemaAttributePathInstance sapi : attributePaths) {

			final AttributePath attributePath = sapi.getAttributePath();

			for (final Attribute attribute : attributePath.getAttributes()) {

				termMap.put(attribute.getName(), (T) attribute);
			}
		}
	}

	/**
	 * returns an attribute map with attribute uuids as key and attribute objects as values of an entry.
	 *
	 * @param schema
	 * @return
	 */
	private static <T extends AdvancedDMPJPAObject> void generateAttributeMap2(final Schema schema, final Map<String, T> termMap) {

		final Collection<SchemaAttributePathInstance> attributePaths = schema.getAttributePaths();

		if (attributePaths == null) {

			return;
		}

		for (final SchemaAttributePathInstance sapi : attributePaths) {

			final AttributePath attributePath = sapi.getAttributePath();

			for (final Attribute attribute : attributePath.getAttributes()) {

				termMap.put(attribute.getUuid(), (T) attribute);
			}
		}
	}

	private static String mintTermUri(final String termUri, final String localTermName, final String baseUri) {

		final String finalLocalTermName;

		if(localTermName.startsWith(AT)) {

			finalLocalTermName = StringUtils.stripStart(localTermName, AT);
		} else {

			finalLocalTermName = localTermName;
		}

		final boolean isValidURI = isValidUri(finalLocalTermName);

		if (isValidURI) {

			return finalLocalTermName;
		} else {

			final Optional<String> optionalBaseUri = Optional.ofNullable(baseUri);

			final boolean isValidTermURI = isValidUri(termUri);

			if (isValidTermURI) {

				return mintTermUri(termUri, localTermName, optionalBaseUri);
			} else {

				return mintTermUri(null, localTermName, optionalBaseUri);
			}
		}
	}

}
