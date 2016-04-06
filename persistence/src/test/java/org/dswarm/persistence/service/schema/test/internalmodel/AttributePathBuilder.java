/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.schema.test.internalmodel;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.utils.NameSpacePrefixRegistry;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaAttributePathInstanceServiceTestUtils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class AttributePathBuilder extends GuicedTest {

	//private static final Logger									LOG			= LoggerFactory.getLogger(AttributePathBuilder.class);

	private final Optional<Map<String, String>> otionalAttributePathsSAPIUUIDs;

	private AttributePath pathUnderConstruction;

	private LinkedList<Attribute> attributeList;

	// private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private final NameSpacePrefixRegistry registry;

	private String prefixPaths = "";

	private SchemaAttributePathInstanceServiceTestUtils schemaAPinstanceServiceTestUtils;
	private AttributePathServiceTestUtils apServiceTestUtils;
	private AttributeServiceTestUtils attributeServiceTestUtils;

	public AttributePathBuilder() {

		super();

		otionalAttributePathsSAPIUUIDs = Optional.empty();

		registry = new NameSpacePrefixRegistry();

		init();
	}

	public AttributePathBuilder(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDsArg) {

		super();

		otionalAttributePathsSAPIUUIDs = optionalAttributePathsSAPIUUIDsArg;

		registry = new NameSpacePrefixRegistry();

		init();
	}

	private void init() {

		schemaAPinstanceServiceTestUtils = new SchemaAttributePathInstanceServiceTestUtils();
		apServiceTestUtils = new AttributePathServiceTestUtils();
		attributeServiceTestUtils = new AttributeServiceTestUtils();
	}

	@Override
	protected void initObjects() {
		super.initObjects();

		init();
	}

	public AttributePathBuilder start() {
		attributeList = Lists.newLinkedList();
		return this;
	}

	public AttributePathBuilder add(final String fullURI) throws Exception {
		attributeList.add(attributeServiceTestUtils.createObject(fullURI, getLocalName(fullURI)));
		return this;
	}

	public AttributePath getPath() throws Exception {

		pathUnderConstruction = apServiceTestUtils.createAttributePath(attributeList);
		return pathUnderConstruction;
	}

	public AttributePath parseAsAttributePath(final String pathInPrefixNotation) throws Exception {

		// temp store prefix paths as a summary
		prefixPaths += pathInPrefixNotation + System.lineSeparator();

		start();

		final String[] attributesInPrefixNotation = pathInPrefixNotation.split("/");

		for (int i = 0; i < attributesInPrefixNotation.length; i++) {

			final String attributeInPrefixNotation = attributesInPrefixNotation[i];

			final String[] attributeParts = attributeInPrefixNotation.split(":");

			final String prefix = attributeParts[0];
			final String localName = attributeParts[1];
			final String namespace = registry.getNamespace(prefix);

			add(namespace + localName);
		}

		return getPath();

	}

	public SchemaAttributePathInstance parseAsAttributePathInstance(final String pathInPrefixNotation) throws Exception {

		final AttributePath attributePath = parseAsAttributePath(pathInPrefixNotation);

		if(otionalAttributePathsSAPIUUIDs.isPresent()) {

			final String attributePathString = attributePath.toAttributePath();

			final Map<String, String> attributePathsSAPIUUIDs = otionalAttributePathsSAPIUUIDs.get();

			final Optional<String> optionalSapiUUID = Optional.ofNullable(attributePathsSAPIUUIDs.getOrDefault(attributePathString, null));

			if (optionalSapiUUID.isPresent()) {

				return schemaAPinstanceServiceTestUtils.createOrGetSchemaAttributePathInstance(optionalSapiUUID.get(), attributePath);
			}
		}

		return schemaAPinstanceServiceTestUtils.createSchemaAttributePathInstance(attributePath);
	}

	public SchemaAttributePathInstance parseAsAttributePathInstance(final String pathInPrefixNotation, final Schema subSchema) throws Exception {

		final AttributePath attributePath = parseAsAttributePath(pathInPrefixNotation);

		if (otionalAttributePathsSAPIUUIDs.isPresent()) {

			final String attributePathString = attributePath.toAttributePath();

			final Map<String, String> attributePathsSAPIUUIDs = otionalAttributePathsSAPIUUIDs.get();

			final Optional<String> optionalSapiUUID = Optional.ofNullable(attributePathsSAPIUUIDs.getOrDefault(attributePathString, null));

			if (optionalSapiUUID.isPresent()) {

				return schemaAPinstanceServiceTestUtils.createOrGetSchemaAttributePathInstance(optionalSapiUUID.get(), null, attributePath, subSchema);
			}
		}

		return schemaAPinstanceServiceTestUtils.createSchemaAttributePathInstance(null, attributePath, subSchema);
	}

	public String getPrefixPaths() {
		return prefixPaths;
	}

	private String getLocalName(final String fullURI) {

		String localName = "";

		if (fullURI.contains("#")) {
			localName = fullURI.substring(StringUtils.lastIndexOf(fullURI, "#") + 1);
		} else {
			localName = fullURI.substring(StringUtils.lastIndexOf(fullURI, "/") + 1);
		}

		return localName;
	}
}
