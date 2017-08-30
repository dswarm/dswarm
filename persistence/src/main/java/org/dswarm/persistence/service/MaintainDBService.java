/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class MaintainDBService {

	private static final Logger LOG = LoggerFactory.getLogger(MaintainDBService.class);

	/**
	 * The entity manager provider (powered by Guice).
	 */
	private final Provider<EntityManager> entityManagerProvider;

	private final String dbName;

	private static final String DROP_DB_TEMPL = "DROP DATABASE IF EXISTS %s;";

	private static final String CREATE_DB_TEMPL = "CREATE DATABASE IF NOT EXISTS %s DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_bin;";

	private static final String USE_DB_TMPL = "USE %s;";

	/**
	 * @param entityManagerProvider
	 */
	@Inject MaintainDBService(final Provider<EntityManager> entityManagerProvider, @Named("dswarm.db.metadata.schema") String dbNameArg) {

		this.entityManagerProvider = entityManagerProvider;
		dbName = dbNameArg;
	}

	/**
	 * Acquire a new or reused EntityManager with its cache cleared
	 *
	 * @return the EntityManager
	 */
	protected EntityManager acquire() {
		return acquire(true);
	}

	/**
	 * Acquire a new or reused EntityManager
	 *
	 * @param clear true if the EM's cache should be cleared
	 * @return the EntityManager
	 */
	protected EntityManager acquire(final boolean clear) {

		final EntityManager entityManager = entityManagerProvider.get();
		if (clear) {
			entityManager.clear();
		}

		return entityManager;
	}

	/**
	 * Initializes the DMP DB with necessary values.
	 *
	 * @throws DMPPersistenceException
	 */
	public void initDB() throws DMPPersistenceException {

		resetDB();
		createTables();
		truncateTables();
		initFunctions();
		initSchemas();
	}

	/**
	 * re-creates the DMP DB.
	 *
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public void resetDB() throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		MaintainDBService.LOG.debug("try to re-create the DB");

		final String dropDBStmt = String.format(DROP_DB_TEMPL, dbName);
		final String createDBStmt = String.format(CREATE_DB_TEMPL, dbName);
		final String useDBStmt = String.format(USE_DB_TMPL, dbName);

		final List<String> sqlStmts = Lists.newArrayListWithCapacity(3);

		sqlStmts.add(dropDBStmt);
		sqlStmts.add(createDBStmt);
		sqlStmts.add(useDBStmt);

		executeSQLScript(entityManager, sqlStmts);

		MaintainDBService.LOG.debug("re-created the DB");
	}

	/**
	 * Executes an SQL script at the DMP DB.
	 *
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public void executeSQLScript(final String sqlScriptName) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		MaintainDBService.LOG.debug("try to execute sql script '{}' at the DB", sqlScriptName);

		executeSQLScriptStatementWise(sqlScriptName, entityManager);

		MaintainDBService.LOG.debug("execute sql script '{}' at the DB", sqlScriptName);
	}

	/**
	 * Truncates all tables of the DMP DB.
	 *
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public void createTables() throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		MaintainDBService.LOG.debug("try to create the tables of the DB");

		executeSQLScriptStatementWise("schema.sql", entityManager);

		MaintainDBService.LOG.debug("created tables of the DB");
	}

	/**
	 * Truncates all tables of the DMP DB.
	 *
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public void truncateTables() throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		MaintainDBService.LOG.debug("try to truncate the tables of the DB");

		executeSQLScriptLineWise("truncate_tables.sql", entityManager);

		MaintainDBService.LOG.debug("truncated tables of the DB");
	}

	/**
	 * Drop all tables of the DMP DB.
	 *
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public void dropTables() throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		MaintainDBService.LOG.debug("try to drop the tables of the DB");

		executeSQLScriptLineWise("drop_tables.sql", entityManager);

		MaintainDBService.LOG.debug("drop tables of the DB");
	}

	/**
	 * Initializes all inbuilt functions of the DMP.
	 *
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public void initFunctions() throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		MaintainDBService.LOG.debug("try to initialize the functions in the DB");

		executeSQLScriptLineWise("functions.sql", entityManager);

		MaintainDBService.LOG.debug("initialized the functions in the DB");
	}

	/**
	 * Initializes all schemas that should be initially available.
	 *
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public void initSchemas() throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		MaintainDBService.LOG.debug("try to initialize the schemas in the DB");

		executeSQLScriptStatementWise("init_internal_schema.sql", entityManager);

		MaintainDBService.LOG.debug("initialized the schemas in the DB");
	}

	/**
	 * Reads a SQL script from the given file and processes its content line-wise, i.e., each SQL statement must be on one line.
	 *
	 * @param sqlScriptFileName the SQL script file name
	 * @param entityManager     the entity manager
	 * @throws DMPPersistenceException
	 */
	private void executeSQLScriptLineWise(final String sqlScriptFileName, final EntityManager entityManager) throws DMPPersistenceException {

		final List<String> sqlScript = readSQLScriptLineWise(sqlScriptFileName);

		executeSQLScript(entityManager, sqlScript);
	}

	/**
	 * Reads a SQL script from the given file and processes its content statement-wise.
	 *
	 * @param sqlScriptFileName the SQL script file name
	 * @param entityManager     the entity manager
	 * @throws DMPPersistenceException
	 */
	private void executeSQLScriptStatementWise(final String sqlScriptFileName, final EntityManager entityManager) throws DMPPersistenceException {

		final List<String> sqlScript = readSQLScriptStatementWise(sqlScriptFileName);

		executeSQLScript(entityManager, sqlScript);
	}

	/**
	 * Executes an SQL script statement-wise.
	 *
	 * @param entityManager the entity manager
	 * @param sqlScript     a list of SQL statements
	 */
	private void executeSQLScript(final EntityManager entityManager, final List<String> sqlScript) {
		for (final String sqlScriptLine : sqlScript) {

			final Query query = entityManager.createNativeQuery(sqlScriptLine);
			query.executeUpdate();
		}
	}

	/**
	 * Reads a SQL script line-wise from the given file, i.e., each SQL statement must be on one line.
	 *
	 * @param sqlScriptFileName the SQL script file name
	 * @return a list of SQL statements
	 * @throws DMPPersistenceException
	 */
	private List<String> readSQLScriptLineWise(final String sqlScriptFileName) throws DMPPersistenceException {

		final List<String> sqlScript;

		try {

			sqlScript = DMPPersistenceUtil.getResourceLinesAsString(sqlScriptFileName);
		} catch (final IOException e) {

			final String message = "couldn't parse SQL script file";

			MaintainDBService.LOG.error(message);

			throw new DMPPersistenceException(message);
		}
		return sqlScript;
	}

	/**
	 * Reads a SQL script statement-wise from the given file.
	 *
	 * @param sqlScriptFileName the SQL script file name
	 * @return a list of SQL statements
	 * @throws DMPPersistenceException
	 */
	private List<String> readSQLScriptStatementWise(final String sqlScriptFileName) throws DMPPersistenceException {

		final List<String> sqlScript;

		try {

			final String sqlScriptString = DMPPersistenceUtil.getResourceAsString(sqlScriptFileName);
			final String[] sqlScriptStatements = sqlScriptString.split(";");
			sqlScript = Lists.newLinkedList();

			for (final String sqlScriptStatement : sqlScriptStatements) {

				final String cleanedSQLStatement = cleanSQLStatement(sqlScriptStatement);

				if (cleanedSQLStatement.trim().isEmpty()) {

					continue;
				}

				sqlScript.add(cleanedSQLStatement + ";");
			}
		} catch (final IOException e) {

			final String message = "couldn't parse SQL script file";

			MaintainDBService.LOG.error(message);

			throw new DMPPersistenceException(message);
		}
		return sqlScript;
	}

	/**
	 * Cleans a given SQL statement and formats the statement into one line.
	 *
	 * @param originalSQLStatement the original SQL statement
	 * @return the cleaned SQL statement
	 */
	private String cleanSQLStatement(final String originalSQLStatement) {

		final String[] lines = originalSQLStatement.split(System.lineSeparator());

		final StringBuilder sb = new StringBuilder();

		for (final String line : lines) {

			if (line.trim().isEmpty()) {

				continue;
			}

			if (line.startsWith("--")) {

				continue;
			}

			if (line.startsWith("#")) {

				continue;
			}

			sb.append(line);
		}

		return sb.toString();
	}
}
