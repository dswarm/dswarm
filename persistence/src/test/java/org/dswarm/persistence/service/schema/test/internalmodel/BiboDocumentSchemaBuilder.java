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

import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.NameSpacePrefixRegistry;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;

import java.util.Map;
import java.util.Optional;

public class BiboDocumentSchemaBuilder extends SchemaBuilder {

	private final Optional<Map<String, String>> optionalSubSchemaAttributePathsSAPIUUIDs;

	public BiboDocumentSchemaBuilder() {

		super();

		optionalSubSchemaAttributePathsSAPIUUIDs = Optional.empty();
	}

	public BiboDocumentSchemaBuilder(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDsArg,
	                                 final Optional<Map<String, String>> optionalSubSchemaAttributePathsSAPIUUIDsArg) {

		super(optionalAttributePathsSAPIUUIDsArg);

		optionalSubSchemaAttributePathsSAPIUUIDs = optionalSubSchemaAttributePathsSAPIUUIDsArg;
	}

	@Override
	public Schema buildSchema() throws Exception {

		final AttributePathBuilder builder = new AttributePathBuilder(optionalAttributePathsSAPIUUIDs);

		// we should take a static identifier here
		final Schema tempSchema = new Schema(SchemaUtils.BIBO_DOCUMENT_SCHEMA_UUID);

		final Clasz clasz = claszServiceTestUtils.createObject(NameSpacePrefixRegistry.BIBO + "Document", "Document");

		/*
		 * // Example of how to use the normal API of the attribute path builder
		 * biboDocumentSchema.addAttributePath(builder.start().add(DC + "creator").add(FOAF + "first_name").getPath());
		 */

		// basic properties used in DINI-AG Titeldaten recommendations
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dc:title"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("rda:otherTitleInformation"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:alternative"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("bibo:shortTitle"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dc:creator"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dc:contributor"));
		{
			// add "deep" paths as schema attribute path instances with attached sub-schemata ...
			final Schema foafPersonSchema = new FoafPersonSchemaBuilder(optionalSubSchemaAttributePathsSAPIUUIDs).buildSchema();
			tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:creator", foafPersonSchema));
			tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:contributor", foafPersonSchema));
		}
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("rda:publicationStatement"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("rda:placeOfPublication"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dc:publisher"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:issued"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("owl:sameAs"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("umbel:isLike"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("bibo:issn"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("bibo:eissn"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("bibo:lccn"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("bibo:oclcnum"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("bibo:isbn"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("rdf:type"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:medium"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:hasPart"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:isPartOf"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:hasVersion"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:isFormatOf"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("rda:precededBy"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("rda:succeededBy"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:language"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("isbd:1053"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("bibo:edition"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("dcterms:bibliographicCitation"));

		// store all parsed paths as an overview
		prefixPaths = builder.getPrefixPaths();

		return schemaServiceTestUtils.createAndPersistSchema(tempSchema.getUuid(), "bibo:Document-Schema (KIM-Titeldaten)",
				tempSchema.getUniqueAttributePaths(), clasz);
	}

}
