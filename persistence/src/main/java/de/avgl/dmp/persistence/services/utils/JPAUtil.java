package de.avgl.dmp.persistence.services.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.avgl.dmp.persistence.DMPPersistenceException;

/**
 * JPA Utility that can be utilised to handle generic persistence task, such as, getting an {@link EntityManager} instance,
 * configuring an {@link EntityManagerFactory} (persistence unit), or handling transactions.
 */
public final class JPAUtil {

	private JPAUtil() {
	}

	private static final org.apache.log4j.Logger	LOG							= org.apache.log4j.Logger.getLogger(JPAUtil.class);

	/**
	 * The entity manager factory of this utility class.
	 */
	private static EntityManagerFactory				entityManagerFactory		= null;

	/**
	 * A thread local entity manger instance.
	 */
	private static ThreadLocal<EntityManager>		entityManagerThreadLocal	= new ThreadLocal<EntityManager>();

	/**
	 * A thread local stack of boolean values.
	 */
	private static ThreadLocal<Stack<Boolean>>		threadLocal					= new ThreadLocal<Stack<Boolean>>();

	/**
	 * Creates an entity manager factory with the properties from the DMPApp persistence unit in the persistence.xml.
	 */
	static {
		try {
			JPAUtil.entityManagerFactory = Persistence.createEntityManagerFactory("DMPApp");
		} catch (final Exception possibleException) {
			// Make sure you log the exception, as it might be swallowed
			// ---- ---- --- --- --- ---------- -- -- ----- -- ---------
			JPAUtil.LOG.error("Initial EntityManagerFactory creation failed.", possibleException);
			throw new ExceptionInInitializerError(possibleException);
		}
	}

	/**
	 * Get the configured entity manager factory of this utility class
	 *
	 * @return entity manager factory of this utility class
	 */
	public static EntityManagerFactory getEntityManagerFactory() {

		return JPAUtil.entityManagerFactory;
	}

	/**
	 * Initializes/modifies the {@link EntityManagerFactory} instance with the following parameters<br>
	 * <ul>
	 * <li>connection URL</li>
	 * <li>connection user name</li>
	 * <li>connection password</li>
	 * <li>dialect</li>
	 * <li>shared cache mode</li>
	 * <li>enable L1 cache flag</li>
	 * <li>enable L2 cache flag</li>
	 * </ul>
	 * Created by: tgaengler
	 *
	 * @param connectionURL the connection URL string
	 * @param connectionUserName the connection user name
	 * @param connectionPassword the connection password
	 * @param dialect the dialect of the database driver
	 * @param sharedCacheMode the shared cache mode, e.g., 'NONE' or 'ENABLE_SELECTIVE'
	 * @param enableL1Cache the enable L1 cache flag
	 * @param enableL2Cache the enable L2 cache flag
	 * @return the initialized/modified {@link EntityManagerFactory} instance
	 */
	public static EntityManagerFactory initEntityManagerFactory(final String connectionURL, final String connectionUserName,
			final String connectionPassword, final String dialect, final String sharedCacheMode, final boolean enableL1Cache,
			final boolean enableL2Cache) {

		final Map addedOrOverridenProperties = new HashMap();

		// Let's suppose we are using Hibernate as JPA provider
		addedOrOverridenProperties.put("javax.persistence.jdbc.url", connectionURL);
		addedOrOverridenProperties.put("javax.persistence.jdbc.user", connectionUserName);
		addedOrOverridenProperties.put("javax.persistence.jdbc.password", connectionPassword);
		addedOrOverridenProperties.put("hibernate.dialect", dialect);
		addedOrOverridenProperties.put("javax.persistence.sharedCache.mode", sharedCacheMode);
		addedOrOverridenProperties.put("hibernate.cache.use_query_cache", enableL1Cache);
		addedOrOverridenProperties.put("hibernate.cache.use_second_level_cache", enableL2Cache);

		try {
			JPAUtil.entityManagerFactory = Persistence.createEntityManagerFactory("DMPApp", addedOrOverridenProperties);
		} catch (final Exception possibleException) {
			// Make sure you log the exception, as it might be swallowed
			// ---- ---- --- --- --- ---------- -- -- ----- -- ---------
			JPAUtil.LOG.error("Initial modified EntityManagerFactory creation failed.", possibleException);
			throw new ExceptionInInitializerError(possibleException);
		}

		if (JPAUtil.entityManagerThreadLocal.get() != null && JPAUtil.entityManagerThreadLocal.get().isOpen()) {

			JPAUtil.closeEntityManager();
		}

		if (JPAUtil.entityManagerThreadLocal.get() != null) {

			JPAUtil.entityManagerThreadLocal.set(null);
		}

		return JPAUtil.entityManagerFactory;
	}

	/**
	 * Get a thread local entity manger instance
	 *
	 * @return a thread local entity manger instance
	 */
	public static EntityManager getEntityManager() {
		if (JPAUtil.entityManagerThreadLocal.get() == null || !JPAUtil.entityManagerThreadLocal.get().isOpen()) {
			JPAUtil.entityManagerThreadLocal.set(JPAUtil.entityManagerFactory.createEntityManager());
		}
		return JPAUtil.entityManagerThreadLocal.get();
	}

	/**
	 * Resets a thread local entity manager instance.<br>
	 * Created by: tgaengler
	 */
	public static void resetEntityManager() {

		JPAUtil.entityManagerThreadLocal.set(null);
	}

	public static void closeEntityManager() {
		closeEntityManager(JPAUtil.entityManagerThreadLocal.get());
		JPAUtil.entityManagerThreadLocal.remove();
	}

	/**
	 * Closes a thread local entity manager instance.<br>
	 * Created by: tgaengler
	 */
	public static void closeEntityManager(final EntityManager entityManager) {
		if (null != entityManager) {
			final boolean open = entityManager.isOpen();
			final EntityTransaction transaction = entityManager.getTransaction();

			final boolean active = transaction.isActive();
			if (active) {
				if (transaction.getRollbackOnly()) {
					JPAUtil.LOG.trace("Transaction will be ROLLED BACK");
					transaction.rollback();
				} else {
					JPAUtil.LOG.trace("Transaction will be COMMITTED");
					transaction.commit();
				}
			}
			if (open) {
				JPAUtil.LOG.trace("Closing entity manager");
				try {
					entityManager.close();
				} catch (final Exception c) {
					// TODO this should not happen, only if multiple threads
					// access
					JPAUtil.LOG.trace("Exception by  closing EM", c);
				}
			}
		}
	}

	/**
	 * Begins a new transacation for the given entity manager instance or utilises an already open transaction of this entity
	 * manager.<br>
	 * Created by: tgaengler
	 *
	 * @param entityManager the entity manager instance that should be utilised to open a new transaction or where an already open
	 *            transaction should be utilised from
	 */
	public static void beginNewTransaction(final EntityManager entityManager) {
		// TODO Workaround for EMA TX handling
		final boolean txIsAlreadyActive;

		if (null == JPAUtil.threadLocal.get()) {
			JPAUtil.threadLocal.set(new Stack<Boolean>());
		}

		JPAUtil.LOG.trace(JPAUtil.threadLocal.get().size() + ": try to open transcation: ");

		if (entityManager.getTransaction().isActive()) {
			if (JPAUtil.threadLocal.get().size() == 0) {
				try {
					throw new DMPPersistenceException("entity manager transaction stack size shouldn't be 0, when transaction is active.");
				} catch (final DMPPersistenceException ex) {
					JPAUtil.LOG.error(ex.getMessage(), ex);
				}
			}
			JPAUtil.LOG.trace("is already active");
			txIsAlreadyActive = true;
		} else {
			entityManager.getTransaction().begin();
			txIsAlreadyActive = false;
		}

		JPAUtil.threadLocal.get().push(Boolean.valueOf(txIsAlreadyActive));
	}

	/**
	 * Ends an existing transaction of the given entity manager instance if it wouldn't be needed anymore.<br>
	 * Created by: tgaengler
	 *
	 * @param entityManager the entity manager instance, where the transaction should be closed
	 */
	public static void endTransaction(final EntityManager entityManager) {
		// TODO BugFix for EMA TX handling -> only commit if tx was open 3 lines
		// before
		final Boolean txIsAlreadyActiveBoolean = JPAUtil.threadLocal.get().pop();
		if (txIsAlreadyActiveBoolean == null) {
			try {
				throw new DMPPersistenceException("is active state should never be null");
			} catch (final DMPPersistenceException ex) {
				JPAUtil.LOG.error(ex.getMessage(), ex);
			}

			return;
		}

		final boolean txIsAlreadyActive = txIsAlreadyActiveBoolean.booleanValue();
		if (txIsAlreadyActive) {
			JPAUtil.LOG.trace((JPAUtil.threadLocal.get().size()) + ": try to close transcation NO");
		} else {
			entityManager.getTransaction().commit();
		}
	}

	/**
	 * Checks the database connection with the default query 'SELECT 123 FROM dual'.<br>
	 * Created by: tgaengler
	 *
	 * @return true, if everything is okay; otherwise false
	 */
	public static boolean checkDBConnection() {

		final EntityManager entityManager = JPAUtil.getEntityManager();

		if (entityManager == null) {

			JPAUtil.LOG.error("could not create entity manager in database connection test");

			return false;
		}

		try {
			// H2 = SELECT 1
			// HSQL = SELECT TOP 1 current_timestamp FROM INFORMATION_SCHEMA.SYSTEM_TABLES
			final Query query = entityManager.createNativeQuery("SELECT 1");

			if (query == null) {

				JPAUtil.LOG.error("query is null in database connection test");

				return false;
			}

			final List resultList = query.getResultList();

			if (resultList == null) {

				JPAUtil.LOG.error("result list is null in database connection test");

				return false;
			}

			if (resultList.isEmpty()) {

				JPAUtil.LOG.error("result list is empty in database connection test");

				return false;
			}
		} catch (final Exception ex) {

			// TODO Auto-generated catch block
			JPAUtil.LOG.error("Something went wrong while testing the database connection", ex);

			return false;
		}

		return true;
	}
}
