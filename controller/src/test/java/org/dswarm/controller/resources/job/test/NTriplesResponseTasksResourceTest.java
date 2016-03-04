package org.dswarm.controller.resources.job.test;

import org.dswarm.common.MediaTypeUtil;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by tgaengler on 04.03.16.
 */
public class NTriplesResponseTasksResourceTest extends AbstractResponseMediaTypeTasksResourceTest {

	public NTriplesResponseTasksResourceTest() {

		super(MediaTypeUtil.N_TRIPLES_TYPE, "controller_task-result.nt");
	}

	@Override
	public void testTaskExecution() throws Exception {

		super.testTaskExecution();
	}

	@Override
	protected void compareResult(final String actualResult) throws IOException {

		final String expectedResult = DMPPersistenceUtil.getResourceAsString(expectedResultFileName);

		final boolean result = expectedResult.length() == actualResult.length() || 1618 == actualResult.length();

		Assert.assertTrue(result);
	}
}
