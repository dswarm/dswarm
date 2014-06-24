package de.avgl.dmp.persistence.service.schema.test.internalmodel;

import de.avgl.dmp.persistence.model.schema.NameSpacePrefixRegistry;
import de.avgl.dmp.persistence.model.schema.Schema;

public class BibrmContractItemSchemaBuilder extends SchemaBuilder {


	//private static final Logger	LOG				= LoggerFactory.getLogger(ERMSchemaBuilder.class);

	public Schema buildSchema() {

		final AttributePathBuilder builder = new AttributePathBuilder();

		final Schema tempSchema = new Schema();

		tempSchema.setRecordClass(builder.createClass(NameSpacePrefixRegistry.BIBRM + "ContractItem", "ContractItem"));

		/*
		 * // Example of how to use the normal API of the attribute path builder
		 * biboDocumentSchema.addAttributePath(builder.start().add(DC + "creator").add(FOAF + "first_name").getPath());
		 */

		// basic properties for ERM example
		//tempSchema.addAttributePath(builder.parsePrefixPath("bibrm:hasItem")); // this needs to go to the schema of Contract itself
		tempSchema.addAttributePath(builder.parsePrefixPath("rdf:type"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibrm:EISSN"));
		tempSchema.addAttributePath(builder.parsePrefixPath("dc:title"));
		tempSchema.addAttributePath(builder.parsePrefixPath("bibrm:price"));

		// This can be generated from an excel file Jan curates

		// store all parsed paths as an overview
		prefixPaths = builder.getPrefixPaths();

		final Schema persistentSchema = createSchema("bibrm:ContractItem-Schema (ERM-Scenario)", tempSchema.getAttributePaths(), tempSchema.getRecordClass());

		return persistentSchema;
	}

	public String getPrefixPaths() {
		return prefixPaths;
	}

}
