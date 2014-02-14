package de.avgl.dmp.persistence.service.schema.test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.mail.imap.protocol.Namespaces.Namespace;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.model.schema.test.AttributePathBuilder;
import de.avgl.dmp.persistence.model.schema.test.NameSpacePrefixRegistry;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class BuildSchemaOfInternalModelTest extends IDBasicJPAServiceTest<ProxySchema, Schema, SchemaService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(BuildSchemaOfInternalModelTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private Map<Long, Attribute>					attributes		= Maps.newLinkedHashMap();
	

	

					
	private static final String NL = System.lineSeparator();
	
	

	public BuildSchemaOfInternalModelTest() {

		super("schema", SchemaService.class);
	}

	@Test
	public void testSimpleSchema() {
		
		AttributePathBuilder builder = new AttributePathBuilder();
		
		
		Schema biboDocumentSchema = new Schema();
		
		
		biboDocumentSchema.setRecordClass(builder.createClass(NameSpacePrefixRegistry.BIBO + "Document", "Document"));
		
		/*
		// Example of how to use the normal API of the attribute path builder
		biboDocumentSchema.addAttributePath(builder.start().add(DC + "creator").add(FOAF + "first_name").getPath());
		*/
				
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dc:issued"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dc:title"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/foaf:first_name"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/foaf:last_name"));
		
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rdf:type"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:medium"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:hasPart"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:isPartOf"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:hasVersion"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:isFormatOf"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rda:precededBy"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rda:succeededBy"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:edition"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:bibliographicCitation"));

		// To be continued. This can be generated from an excel file Jan curates

		
		printSchemaJSON(biboDocumentSchema);
		printSchemaText(biboDocumentSchema);

	}
	
	public void printSchemaJSON(Schema schema){
		
		String json = null;
		
		try {

			json = objectMapper.writeValueAsString(schema);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		System.out.println("****************************************************");
		System.out.println("schema json: " + json);
		System.out.println("****************************************************");
		
	}
	
	public void printSchemaText(Schema schema){
		
		System.out.println("****************************************************");
		System.out.println("Schema for " + schema.getRecordClass().getUri() );
		System.out.println("****************************************************");
		
		Set<AttributePath> pathSet = schema.getAttributePaths();
		
		for (Iterator<AttributePath> iterator = pathSet.iterator(); iterator.hasNext();) {
			
			AttributePath attributePath = (AttributePath) iterator.next();
			printAttributePath(attributePath);
			
		}
		
		System.out.println("****************************************************");
		
	}
	
	public static void printAttributePath(AttributePath path){
		
		System.out.println(path.toAttributePath());
		
	}

	@Override
	public void idGenerationTest() {
	}
}
