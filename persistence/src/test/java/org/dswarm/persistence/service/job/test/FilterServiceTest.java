package org.dswarm.persistence.service.job.test;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.job.FilterService;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FilterServiceTest extends IDBasicJPAServiceTest<ProxyFilter, Filter, FilterService> {

	private static final Logger	LOG				= LoggerFactory.getLogger(FilterServiceTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public FilterServiceTest() {

		super("filter", FilterService.class);
	}

	@Test
	public void testSimpleFilter() {

		final String name = "my filter";

		final String expression = "SELECT ?identifier ?url\n" + "WHERE {\n" + "    ?record custmabxml:metadata ?metadata ;\n"
				+ "            custmabxml:header ?header .\n" + "    ?header custmabxml:identifier ?identifier .\n"
				+ "    ?metadata m:record ?mabrecord .\n" + "    ?mabrecord m:datafield ?dataField .\n" + "    ?dataField m:tag \"088\" ;\n"
				+ "               m:ind1 \"a\" ;\n" + "               m:subfield ?subField .\n" + "    ?subField rdf:value ?url .\n" + "}";

		final Filter filter = createObject().getObject();

		filter.setName(name);
		filter.setExpression(expression);

		final Filter updatedFilter = updateObjectTransactional(filter).getObject();

		Assert.assertNotNull("the filter's name of the updated filter shouldn't be null", updatedFilter.getName());
		Assert.assertEquals("the filter's names are not equal", filter.getName(), updatedFilter.getName());
		Assert.assertNotNull("the expression of the updated filter shouldn't be null", updatedFilter.getExpression());
		Assert.assertEquals("the filter's expressions are not equal", filter.getExpression(), updatedFilter.getExpression());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(filter);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		FilterServiceTest.LOG.debug("filter json: " + json);

		// clean up DB
		deleteObject(filter.getId());
	}
}
