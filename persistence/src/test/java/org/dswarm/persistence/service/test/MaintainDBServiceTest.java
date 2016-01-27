/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public void testResetDB() {

		final MaintainDBService maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);

		try {

			maintainDBService.resetDB();
		} catch (final DMPPersistenceException e) {

			Assert.assertFalse(true);
		}
	}

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
