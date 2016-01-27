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
package org.dswarm.persistence.service.util.test;

import java.io.IOException;

import org.junit.Test;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.service.util.MetadataRepositoryMigrator;

/**
 * @author tgaengler
 */
public class MetadataRepositoryMigratorTest extends GuicedTest {

	@Override
	public void prepare() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
	}

	@Override public void tearDown3() throws Exception {

	}

	/**
	 * requires that the metadata repository state that should be migrated is given (i.e. the current state of the metadata repository is taken)
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	//@Test
	public void migrateDataTest() throws IOException, DMPPersistenceException {

		final MetadataRepositoryMigrator metadataRepositoryMigrator = GuicedTest.injector.getInstance(MetadataRepositoryMigrator.class);

		metadataRepositoryMigrator.migrateData();
	}
}
