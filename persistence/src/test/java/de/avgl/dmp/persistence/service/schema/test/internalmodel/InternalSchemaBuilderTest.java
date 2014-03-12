package de.avgl.dmp.persistence.service.schema.test.internalmodel;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
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

public class InternalSchemaBuilderTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(InternalSchemaBuilderTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private Map<Long, Attribute>					attributes		= Maps.newLinkedHashMap();
					
	private static final String NL = System.lineSeparator();


	@Ignore
	@Test
	public void buildInternalSchema() {
		
		InternalSchemaBuilder schemaBuilder = new InternalSchemaBuilder();
		Schema internalSchema = schemaBuilder.buildInternalSchema();

		printSchemaJSON(internalSchema);
		printSchemaText(internalSchema);
		printSchemaTextAsPrefixPaths(schemaBuilder);
	}
	
	private void printSchemaTextAsPrefixPaths(InternalSchemaBuilder schemaBuilder) {
		
		System.out.println("****************************************************");
		System.out.println("Schema as prefix paths");
		System.out.println("****************************************************");
		System.out.println(schemaBuilder.getPrefixPaths());
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
		System.out.println("Schema as json: " + json);
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
