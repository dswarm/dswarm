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
package org.dswarm.persistence.service.schema.test.internalmodel;

import org.dswarm.persistence.model.schema.NameSpacePrefixRegistry;
import org.dswarm.persistence.model.schema.Schema;

public class BibrmContractItemSchemaBuilder extends SchemaBuilder {

	// private static final Logger LOG = LoggerFactory.getLogger(ERMSchemaBuilder.class);

	@Override
	public Schema buildSchema() {

		final AttributePathBuilder builder = new AttributePathBuilder();

		final Schema tempSchema = new Schema();

		tempSchema.setRecordClass(builder.createClass(NameSpacePrefixRegistry.BIBRM + "ContractItem", "ContractItem"));

		/*
		 * // Example of how to use the normal API of the attribute path builder
		 * biboDocumentSchema.addAttributePath(builder.start().add(DC + "creator").add(FOAF + "first_name").getPath());
		 */

		// basic properties for ERM example
		// tempSchema.addAttributePath(builder.parsePrefixPath("bibrm:hasItem")); // this needs to go to the schema of Contract
		// itself
		tempSchema.addAttributePath(builder.parsePrefixPath("rdf:type"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibrm:EISSN"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dc:title"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibrm:price"));

		// This can be generated from an excel file Jan curates

		// store all parsed paths as an overview
		prefixPaths = builder.getPrefixPaths();

		final Schema persistentSchema = createSchema("bibrm:ContractItem-Schema (ERM-Scenario)", tempSchema.getUniqueAttributePaths(),
				tempSchema.getRecordClass());

		return persistentSchema;
	}

	@Override
	public String getPrefixPaths() {
		return prefixPaths;
	}

}
