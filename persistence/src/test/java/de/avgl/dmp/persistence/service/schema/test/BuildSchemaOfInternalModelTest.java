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
import de.avgl.dmp.persistence.model.schema.NameSpacePrefixRegistry;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class BuildSchemaOfInternalModelTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(BuildSchemaOfInternalModelTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private Map<Long, Attribute>					attributes		= Maps.newLinkedHashMap();
	

	

					
	private static final String NL = System.lineSeparator();
	
	


	@Test
	public void testSimpleSchema() {
		
		AttributePathBuilder builder = new AttributePathBuilder();
		
		
		Schema biboDocumentSchema = new Schema();
		
		
		biboDocumentSchema.setRecordClass(builder.createClass(NameSpacePrefixRegistry.BIBO + "Document", "Document"));
		
		/*
		// Example of how to use the normal API of the attribute path builder
		biboDocumentSchema.addAttributePath(builder.start().add(DC + "creator").add(FOAF + "first_name").getPath());
		*/
		
		// basic properties used in DINI-AG Titeldaten recommendations
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dc:title"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rda:otherTitleInformation"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:alternative"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:shortTitle"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dc:creator"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dc:contributor"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rda:publicationStatement"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rda:placeOfPublication"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dc:publisher"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:issued"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("owl:sameAs"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("umbel:isLike"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("umbel:isLike"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:issn"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:eissn"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:lccn"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:oclcnum"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:isbn"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rdf:type"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:medium"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:hasPart"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:isPartOf"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:hasVersion"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:isFormatOf"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rda:precededBy"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("rda:succeededBy"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:language"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("isbd:1053"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("bibo:edition"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:bibliographicCitation"));
		
		// extra (added to have some details on creator/contributor resources):
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/rdf:type"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/foaf:familyName"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:creator/foaf:givenName"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor/rdf:type"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor/foaf:familyName"));
		biboDocumentSchema.addAttributePath(builder.parsePrefixPath("dcterms:contributor/foaf:givenName"));
	


		// To be continued. This can be generated from an excel file Jan curates

		
		printSchemaJSON(biboDocumentSchema);
		printSchemaText(biboDocumentSchema);
		printSchemaTextAsPrefixPaths(builder.getPrefixPaths());

	}
	
	private void printSchemaTextAsPrefixPaths(String prefixPaths) {
		
		System.out.println("****************************************************");
		System.out.println("Schema as prefix paths");
		System.out.println("****************************************************");
		System.out.println(prefixPaths);
		System.out.println("****************************************************");
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
		
		System.out.println(path.toAttributePath().replace("", " :: "));
		
	}

}
