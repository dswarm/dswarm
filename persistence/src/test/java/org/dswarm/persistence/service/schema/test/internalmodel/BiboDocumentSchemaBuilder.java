/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

public class BiboDocumentSchemaBuilder extends SchemaBuilder {

	@Override
	public Schema buildSchema() {

		final AttributePathBuilder builder = new AttributePathBuilder();

		final Schema tempSchema = new Schema();

		tempSchema.setRecordClass(builder.createClass(NameSpacePrefixRegistry.BIBO + "Document", "Document"));

		/*
		 * // Example of how to use the normal API of the attribute path builder
		 * biboDocumentSchema.addAttributePath(builder.start().add(DC + "creator").add(FOAF + "first_name").getPath());
		 */

		// basic properties used in DINI-AG Titeldaten recommendations
		tempSchema.addAttributePath(builder.parsePrefixPath("dc:title"));
		tempSchema.addAttributePath(builder.parsePrefixPath("rda:otherTitleInformation"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:alternative"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibo:shortTitle"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dc:creator"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dc:contributor"));
		tempSchema.addAttributePath(builder.parsePrefixPath("rda:publicationStatement"));
		tempSchema.addAttributePath(builder.parsePrefixPath("rda:placeOfPublication"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dc:publisher"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:issued"));
		tempSchema.addAttributePath(builder.parsePrefixPath("owl:sameAs"));
		tempSchema.addAttributePath(builder.parsePrefixPath("umbel:isLike"));
		tempSchema.addAttributePath(builder.parsePrefixPath("umbel:isLike"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibo:issn"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibo:eissn"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibo:lccn"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibo:oclcnum"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibo:isbn"));
		tempSchema.addAttributePath(builder.parsePrefixPath("rdf:type"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:medium"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:hasPart"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:isPartOf"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:hasVersion"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:isFormatOf"));
		tempSchema.addAttributePath(builder.parsePrefixPath("rda:precededBy"));
		tempSchema.addAttributePath(builder.parsePrefixPath("rda:succeededBy"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:language"));
		tempSchema.addAttributePath(builder.parsePrefixPath("isbd:1053"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibo:edition"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:bibliographicCitation"));

		// extra (added to have some details on creator/contributor resources):
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/rdf:type"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/foaf:familyName"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/foaf:givenName"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor/rdf:type"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor/foaf:familyName"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor/foaf:givenName"));

		// This can be generated from an excel file Jan curates

		// store all parsed paths as an overview
		prefixPaths = builder.getPrefixPaths();

		final Schema persistentSchema = createSchema("bibo:Document-Schema (KIM-Titeldaten)", tempSchema.getUniqueAttributePaths(),
				tempSchema.getRecordClass());

		return persistentSchema;
	}

}
