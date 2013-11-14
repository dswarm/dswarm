package de.avgl.dmp.persistence.model.job.test;

import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Filter;

public class FilterTest extends GuicedTest {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(FilterTest.class);

	private final ObjectMapper						objectMapper	= injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleFilterTest() {

		final String expression = "SELECT ?identifier ?url\n" + "WHERE {\n" + "    ?record custmabxml:metadata ?metadata ;\n"
				+ "            custmabxml:header ?header .\n" + "    ?header custmabxml:identifier ?identifier .\n"
				+ "    ?metadata m:record ?mabrecord .\n" + "    ?mabrecord m:datafield ?dataField .\n" + "    ?dataField m:tag \"088\" ;\n"
				+ "               m:ind1 \"a\" ;\n" + "               m:subfield ?subField .\n" + "    ?subField rdf:value ?url .\n" + "}";

		final Filter filter = new Filter();
		filter.setId(UUID.randomUUID().toString());

		filter.setExpression(expression);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(filter);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		LOG.debug("filter json: " + json);
	}
}
