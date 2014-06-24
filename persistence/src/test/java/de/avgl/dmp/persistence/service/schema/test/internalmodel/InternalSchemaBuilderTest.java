package de.avgl.dmp.persistence.service.schema.test.internalmodel;

import java.util.Iterator;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Schema;

public class InternalSchemaBuilderTest extends GuicedTest {

	//private static final Logger			LOG				= LoggerFactory.getLogger(InternalSchemaBuilderTest.class);

	private final ObjectMapper			objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	//private static final String			NL				= System.lineSeparator();

	@Ignore
	@Test
	public void buildInternalSchema() {
		buildSchema(new BiboDocumentSchemaBuilder());
	}

	@Ignore
	@Test
	public void buildERMSchema() {
		buildSchema(new BibrmContractItemSchemaBuilder());
	}

	private void buildSchema(SchemaBuilder schemaBuilder) {
		
		final Schema schema = schemaBuilder.buildSchema();

		printSchemaJSON(schema);
		printSchemaText(schema);
		printSchemaTextAsPrefixPaths(schemaBuilder);
	}
	
	private void printSchemaTextAsPrefixPaths(final SchemaBuilder schemaBuilder) {

		System.out.println("****************************************************");
		System.out.println("Schema as prefix paths");
		System.out.println("****************************************************");
		System.out.println(schemaBuilder.getPrefixPaths());
		System.out.println("****************************************************");
	}

	public void printSchemaJSON(final Schema schema) {

		String json = null;

		try {

			json = objectMapper.writeValueAsString(schema);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		System.out.println("****************************************************");
		System.out.println("Schema as json: " + json);
		System.out.println("****************************************************");

	}

	public void printSchemaText(final Schema schema) {

		System.out.println("****************************************************");
		System.out.println("Schema for " + schema.getRecordClass().getUri());
		System.out.println("****************************************************");

		final Set<AttributePath> pathSet = schema.getAttributePaths();

		for (final Iterator<AttributePath> iterator = pathSet.iterator(); iterator.hasNext();) {

			final AttributePath attributePath = iterator.next();
			InternalSchemaBuilderTest.printAttributePath(attributePath);

		}

		System.out.println("****************************************************");

	}

	public static void printAttributePath(final AttributePath path) {

		System.out.println(path.toAttributePath().replace("", " :: "));

	}

}
