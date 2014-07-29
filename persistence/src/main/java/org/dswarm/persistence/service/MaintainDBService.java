package org.dswarm.persistence.service;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class MaintainDBService {

	private static final Logger				LOG	= LoggerFactory.getLogger(MaintainDBService.class);

	/**
	 * The entity manager provider (powered by Guice).
	 */
	private final Provider<EntityManager>	entityManagerProvider;

	/**
	 * @param entityManagerProvider
	 */
	@Inject
	MaintainDBService(final Provider<EntityManager> entityManagerProvider) {

		this.entityManagerProvider = entityManagerProvider;
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

		truncateTables();
		initFunctions();
		initSchemas();
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
	 * @param entityManager the entity manager
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
	 * @param entityManager the entity manager
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
	 * @param sqlScript a list of SQL statements
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
