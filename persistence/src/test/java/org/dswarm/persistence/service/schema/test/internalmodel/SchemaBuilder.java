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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public abstract class SchemaBuilder extends GuicedTest {

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
	private static final Logger LOG = LoggerFactory.getLogger(SchemaBuilder.class);
	protected String prefixPaths = "";

	protected ClaszServiceTestUtils claszServiceTestUtils;
	protected SchemaServiceTestUtils schemaServiceTestUtils;

	protected final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs;

	public SchemaBuilder() {

		super();

		optionalAttributePathsSAPIUUIDs = Optional.empty();

		init();
	}

	public SchemaBuilder(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDsArg) {

		super();

		optionalAttributePathsSAPIUUIDs = optionalAttributePathsSAPIUUIDsArg;

		init();
	}

	private void init() {

		claszServiceTestUtils = new ClaszServiceTestUtils();
		schemaServiceTestUtils = new SchemaServiceTestUtils();
	}

	@Override
	protected void initObjects() {
		super.initObjects();

		init();
	}

	public abstract Schema buildSchema() throws Exception;

	public String getPrefixPaths() {
		return prefixPaths;
	}

}
