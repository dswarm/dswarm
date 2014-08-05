package org.dswarm.persistence.service.test;

import org.junit.Assert;
import org.junit.Test;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.service.MaintainDBService;

/**
 * @author tgaengler
 */
public class MaintainDBServiceTest extends GuicedTest {

	@Test
	public void testTruncateTables() {

		final MaintainDBService maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);

		try {

			maintainDBService.truncateTables();
		} catch (final DMPPersistenceException e) {

			Assert.assertFalse(true);
		}
	}

	@Test
	public void testInitFunctions() {

		final MaintainDBService maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);

		try {

			maintainDBService.truncateTables();
			maintainDBService.initFunctions();
		} catch (final DMPPersistenceException e) {

			Assert.assertFalse(true);
		}
	}

	@Test
	public void testInitSchemas() {

		final MaintainDBService maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);

		try {

			maintainDBService.truncateTables();
			maintainDBService.initSchemas();
		} catch (final DMPPersistenceException e) {

			Assert.assertFalse(true);
		}
	}

	@Test
	public void testInitDB() {

		final MaintainDBService maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);

		try {

			maintainDBService.initDB();
		} catch (final DMPPersistenceException e) {

			Assert.assertFalse(true);
		}
	}

}
