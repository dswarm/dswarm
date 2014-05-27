package de.avgl.dmp.persistence.model.job.test;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(FilterTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleFilterTest() {

		final String expression = "SELECT ?identifier ?url\n" + "WHERE {\n" + "    ?record custmabxml:metadata ?metadata ;\n"
				+ "            custmabxml:header ?header .\n" + "    ?header custmabxml:identifier ?identifier .\n"
				+ "    ?metadata m:record ?mabrecord .\n" + "    ?mabrecord m:datafield ?dataField .\n" + "    ?dataField m:tag \"088\" ;\n"
				+ "               m:ind1 \"a\" ;\n" + "               m:subfield ?subField .\n" + "    ?subField rdf:value ?url .\n" + "}";

		final Filter filter = new Filter();
		// filter.setId(UUID.randomUUID().toString());

		filter.setExpression(expression);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(filter);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		FilterTest.LOG.debug("filter json: " + json);
	}
}
