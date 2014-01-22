package de.avgl.dmp.controller.resources.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

public class ClaszesResourceTest extends BasicResourceTest<ClaszesResourceTestUtils, ClaszService, Clasz, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ClaszesResourceTest.class);

	public ClaszesResourceTest() {

		super(Clasz.class, ClaszService.class, "classes", "clasz.json", new ClaszesResourceTestUtils());
		
	}
	
	@Test
	public void testUniquenessOfClasses() {
		
		LOG.debug("start classes uniqueness test");

		Clasz clasz1 = null;

		try {
			
			clasz1 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("coudln't create class 1 for uniqueness test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("class 1 shouldn't be null in uniqueness test", clasz1);

		Clasz clasz2 = null;

		try {
			
			clasz2 = pojoClassResourceTestUtils.createObject(objectJSONString, expectedObject);
		} catch (Exception e) {
			
			LOG.error("couldn't create class 2 for uniqueness test");
			
			Assert.assertTrue(false);
		}
		
		Assert.assertNotNull("class 2 shouldn't be null in uniqueness test", clasz2);
		
		Assert.assertEquals("the classes should be equal", clasz1, clasz2);
		
		cleanUpDB(clasz1);
		
		LOG.debug("end class uniqueness test");
	}
	
	@Ignore
	@Override
	public void testPUTObject() throws Exception {

		// TODO: [@fniederlein] implement test
	}
	
	@Ignore
	@Override
	public void testDELETEObject() throws Exception {

		//super.testDELETEObject();
	}
}
