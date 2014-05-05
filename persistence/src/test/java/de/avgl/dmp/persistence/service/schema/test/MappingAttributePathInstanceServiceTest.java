package de.avgl.dmp.persistence.service.schema.test;

import java.util.LinkedList;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import de.avgl.dmp.persistence.service.job.test.utils.FilterServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.MappingAttributePathInstanceService;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class MappingAttributePathInstanceServiceTest extends
		IDBasicJPAServiceTest<ProxyMappingAttributePathInstance, MappingAttributePathInstance, MappingAttributePathInstanceService> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(SchemaServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Attribute>				attributes		= Maps.newLinkedHashMap();

	private final AttributeServiceTestUtils			attributeServiceTestUtils;
	private final AttributePathServiceTestUtils		attributePathServiceTestUtils;
	private final FilterServiceTestUtils			filterServiceTestUtils;

	public MappingAttributePathInstanceServiceTest() {

		super("mapping attribute path instance", MappingAttributePathInstanceService.class);

		attributeServiceTestUtils = new AttributeServiceTestUtils();
		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
		filterServiceTestUtils = new FilterServiceTestUtils();
	}

	@Test
	public void testSimpleMappingAttributePathInstance() throws Exception {

		// first attribute path

		final Attribute dctermsTitle = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		attributes.put(dctermsTitle.getId(), dctermsTitle);

		final Attribute dctermsHasPart = attributeServiceTestUtils.createAttribute("http://purl.org/dc/terms/hasPart", "hasPart");
		attributes.put(dctermsHasPart.getId(), dctermsHasPart);

		final LinkedList<Attribute> attributePath1Arg = Lists.newLinkedList();

		attributePath1Arg.add(dctermsTitle);
		attributePath1Arg.add(dctermsHasPart);
		attributePath1Arg.add(dctermsTitle);

		System.out.println("attribute title = '" + dctermsTitle.toString());
		System.out.println("attribute hasPart = '" + dctermsHasPart.toString());

		final AttributePath attributePath1 = attributePathServiceTestUtils.createAttributePath(attributePath1Arg);

		// filter

		final String filterName = "my filter";

		final String filterExpression = "SELECT ?identifier ?url\n" + "WHERE {\n" + "    ?record custmabxml:metadata ?metadata ;\n"
				+ "            custmabxml:header ?header .\n" + "    ?header custmabxml:identifier ?identifier .\n"
				+ "    ?metadata m:record ?mabrecord .\n" + "    ?mabrecord m:datafield ?dataField .\n" + "    ?dataField m:tag \"088\" ;\n"
				+ "               m:ind1 \"a\" ;\n" + "               m:subfield ?subField .\n" + "    ?subField rdf:value ?url .\n" + "}";

		final Filter filter = filterServiceTestUtils.createFilter(filterName, filterExpression);

		final Integer ordinal = Integer.valueOf(1);

		// mapping attribute path instance

		final MappingAttributePathInstance mappingAttributePathInstance = createObject().getObject();

		mappingAttributePathInstance.setName("my mapping attribute path instance");
		mappingAttributePathInstance.setAttributePath(attributePath1);
		mappingAttributePathInstance.setOrdinal(ordinal);
		mappingAttributePathInstance.setFilter(filter);

		// update mapping attribute path instance

		final MappingAttributePathInstance updatedMappingAttributePathInstance = updateObjectTransactional(mappingAttributePathInstance).getObject();

		Assert.assertNotNull(
				"the mapping attribute path instance's attribute paths of the updated mapping attribute path instance shouldn't be null",
				updatedMappingAttributePathInstance.getAttributePath());
		Assert.assertEquals("the mapping attribute path instance's attribute paths are not equal", mappingAttributePathInstance.getAttributePath(),
				updatedMappingAttributePathInstance.getAttributePath());
		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
				+ "' of the updated mapping attribute path instance shouldn't be null", updatedMappingAttributePathInstance.getAttributePath()
				.getAttributes());
		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.getAttributes(), updatedMappingAttributePathInstance.getAttributePath().getAttributes());
		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
				.getAttributePath().get(0), updatedMappingAttributePathInstance.getAttributePath().getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId()
				+ "' of the updated mapping attribute path instance shouldn't be null", updatedMappingAttributePathInstance.getAttributePath()
				.toAttributePath());
		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
				attributePath1.toAttributePath(), updatedMappingAttributePathInstance.getAttributePath().toAttributePath());
		Assert.assertNotNull("the mapping attribute path instance's ordinals of the updated mapping attribute path instance shouldn't be null",
				updatedMappingAttributePathInstance.getAttributePath());
		Assert.assertEquals("the mapping attribute path instance's ordinals are not equal", mappingAttributePathInstance.getOrdinal(),
				updatedMappingAttributePathInstance.getOrdinal());
		Assert.assertNotNull("the mapping attribute path instance's filters of the updated mapping attribute path instance shouldn't be null",
				updatedMappingAttributePathInstance.getFilter());
		Assert.assertEquals("the mapping attribute path instance's filters are not equal", mappingAttributePathInstance.getFilter(),
				updatedMappingAttributePathInstance.getFilter());
		Assert.assertEquals("the mapping attribute path instance's filter's expressions are not equal", mappingAttributePathInstance.getFilter()
				.getExpression(), updatedMappingAttributePathInstance.getFilter().getExpression());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(mappingAttributePathInstance);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		MappingAttributePathInstanceServiceTest.LOG.debug("mapping attribute path instance json: " + json);

		// clean up DB
		deleteObject(mappingAttributePathInstance.getId());

		filterServiceTestUtils.deleteObject(filter);

		attributePathServiceTestUtils.deleteObject(attributePath1);

		for (final Attribute attribute : attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}
	}
}
