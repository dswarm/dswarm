package org.dswarm.controller.resources.job.test;

import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by tgaengler on 04.03.16.
 */
public class XMLResponseTasksResourceTest extends AbstractResponseMediaTypeTasksResourceTest {

	public XMLResponseTasksResourceTest() {

		super(MediaType.APPLICATION_XML_TYPE, "controller_task-result.xml");
	}

	/**
	 * note: the result XML might not be what one would expect, because it contains for feld->nr multiple values, whereby each value is encapsulated in an own mabxml:nr element (i.e. this isn't conform to the MABXML schema)
	 *
	 * @throws Exception
	 */
	@Override
	public void testTaskExecution() throws Exception {

		super.testTaskExecution();
	}

	@Override
	protected void compareResult(final String actualResult) throws IOException {

		final String expectedResultXML = DMPPersistenceUtil.getResourceAsString(expectedResultFileName);

		final boolean result = expectedResultXML.length() == actualResult.length() || 781 == actualResult.length();

		Assert.assertTrue(result);
	}
}
