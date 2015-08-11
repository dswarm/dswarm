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
		//maintainDBService.createTables();
		//maintainDBService.truncateTables();
	}

	@Override public void tearDown3() throws Exception {

		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.truncateTables();
	}

	/**
	 * requires that the metadata repository state that should be migrated is given (i.e. the current state of the metadata repository is taken)
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	@Test
	public void migrateDataTest() throws IOException, DMPPersistenceException {

		//maintainDBService.executeSQLScript("metadata-20150806135818.sql");

		final MetadataRepositoryMigrator metadataRepositoryMigrator = GuicedTest.injector.getInstance(MetadataRepositoryMigrator.class);

		metadataRepositoryMigrator.migrateData();

		System.out.println("here I am");
	}
}
