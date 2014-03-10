package de.avgl.dmp.persistence.service.schema.test.internalmodel;

import java.util.Set;

import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.NameSpacePrefixRegistry;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

public class InternalSchemaBuilder extends GuicedTest {
	
	
	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(AttributePathBuilder.class);

	private String prefixPaths = "";
	
	
	
	public Schema buildInternalSchema() {
	
		AttributePathBuilder builder = new AttributePathBuilder();
		
		Schema tempSchema = new Schema();
				
		tempSchema.setRecordClass(builder.createClass(NameSpacePrefixRegistry.BIBO + "Document", "Document"));
		
		/*
		// Example of how to use the normal API of the attribute path builder
		biboDocumentSchema.addAttributePath(builder.start().add(DC + "creator").add(FOAF + "first_name").getPath());
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
		
		Schema persistentSchema = createSchema("Internal Schema", tempSchema.getAttributePaths(), tempSchema.getRecordClass());
		
		return persistentSchema;
	}
	
	private Schema createSchema(final String name, final Set<AttributePath> attributePaths, final Clasz recordClass) {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		Assert.assertNotNull("schema service shouldn't be null", schemaService);

		// create schema

		Schema schema = null;

		try {
			schema = schemaService.createObjectTransactional().getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while schema creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("schema shouldn't be null", schema);
		Assert.assertNotNull("schema id shouldn't be null", schema.getId());

		schema.setName(name);
		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(recordClass);

		// update schema

		Schema updatedSchema = null;

		try {

			updatedSchema = schemaService.updateObjectTransactional(schema).getObject();
		} catch (final DMPPersistenceException e) {

			Assert.assertTrue("something went wrong while updating the schema of id = '" + schema.getId() + "'", false);
		}

		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getId());

		final AttributePath attributePath1 = attributePaths.iterator().next();

		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedSchema.getAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getAttributePaths(), updatedSchema.getAttributePaths());
		Assert.assertEquals("the attribute path '" + attributePath1.getId() + "' of the schema are not equal",
				schema.getAttributePath(attributePath1.getId()), updatedSchema.getAttributePath(attributePath1.getId()));
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated schema shouldn't be null", updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedSchema.getAttributePath(attributePath1.getId()).getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedSchema.getAttributePath(attributePath1.getId()).getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId() + "' of the update schema shouldn't be null",
				updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedSchema.getAttributePath(attributePath1.getId()).toAttributePath());
		Assert.assertNotNull("the record class of the updated schema shouldn't be null", updatedSchema.getRecordClass());
		Assert.assertEquals("the recod classes are not equal", schema.getRecordClass(), updatedSchema.getRecordClass());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedSchema);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("schema json: " + json);

		return updatedSchema;
	}

	public String getPrefixPaths() {
		return prefixPaths;
	}

}
