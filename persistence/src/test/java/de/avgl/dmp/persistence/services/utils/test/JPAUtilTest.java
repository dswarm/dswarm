package de.avgl.dmp.persistence.services.utils.test;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.avgl.dmp.persistence.services.utils.JPAUtil;

/**
 * Testing the {@link JPAUtil} class, which be utilised to create and handle entity manager ({@link EntityManager}), entity
 * manager factories ({@link EntityManagerFactory}) and transactions ({@link EntityTransaction}).<br>
 * 
 * @author tgaengler
 */

public class JPAUtilTest {

	private static final org.apache.log4j.Logger	LOG			= org.apache.log4j.Logger.getLogger(JPAUtilTest.class);

	private static Map								properties	= null;

	/**
	 * Retrieves the original entity manager factory (persistence unit) properties of the default {@link JPAUtil} entity manager
	 * factory that are taken from the persistence unit definition in the persistence.xml<br>
	 * Created by: tgaengler
	 */
	@BeforeClass
	public static void setup() {

		final EntityManager entityManager = JPAUtil.getEntityManager();

		JPAUtilTest.properties = entityManager.getEntityManagerFactory().getProperties();
	}

	/**
	 * Creates a modified entity manager factory ({@link EntityManagerFactory}) and checks whether the properties are set as
	 * expected.<br>
	 * Created by: tgaengler
	 */
	@Test
	public void modifiedEntityManagerFactoryTest() {

		final String connectionURL = "jdbc:hsqldb:.";
		final String connectionUserName = "foo";
		//final String connectionPassword = "bla";
		final String dialect = "org.hibernate.dialect.HSQLDialect";
		final String sharedCacheMode = "NONE";
		final boolean enableL1Cache = false;
		final boolean enableL2Cache = false;

		JPAUtil.initEntityManagerFactory(connectionURL, connectionUserName, "", dialect, sharedCacheMode, enableL1Cache,
				enableL2Cache);

		final EntityManager entityManager = JPAUtil.getEntityManager();

		final Map properties = entityManager.getEntityManagerFactory().getProperties();

		Assert.assertEquals("connection URL is not equal", connectionURL, properties.get("javax.persistence.jdbc.url"));
		Assert.assertEquals("connection user name is not equal", connectionUserName, properties.get("javax.persistence.jdbc.user"));
		//Assert.assertEquals("connection password is not equal", connectionPassword, properties.get("javax.persistence.jdbc.password"));
		Assert.assertEquals("dialect is not equal", dialect, properties.get("hibernate.dialect"));
		Assert.assertEquals("shared cache mode is not equal", sharedCacheMode, properties.get("javax.persistence.sharedCache.mode"));
		Assert.assertEquals("enable L1 cache flag is not equal", enableL1Cache, properties.get("hibernate.cache.use_query_cache"));
		Assert.assertEquals("enable L2 cache flag is not equal", enableL2Cache, properties.get("hibernate.cache.use_second_level_cache"));
	}

	/**
	 * Tests the {@link JPAUtil#checkDBConnection()} method that runs a test query against the set DB connection of
	 * {@link JPAUtil} ({@link JPAUtil#getEntityManager()}).<br>
	 * Created by: tgaengler
	 */
	@Test
	public void checkDBConnectionTest() {

		final String connectionURL = (String) JPAUtilTest.properties.get("javax.persistence.jdbc.url");
		final String connectionUserName = (String) JPAUtilTest.properties.get("javax.persistence.jdbc.user");
		//final String connectionPassword = (String) JPAUtilTest.properties.get("javax.persistence.jdbc.password");
		final String dialect = (String) JPAUtilTest.properties.get("hibernate.dialect");
		final String sharedCacheMode = (String) JPAUtilTest.properties.get("javax.persistence.sharedCache.mode");
		final boolean enableL1Cache = Boolean.valueOf((String) JPAUtilTest.properties.get("hibernate.cache.use_query_cache"));
		final boolean enableL2Cache = Boolean.valueOf((String) JPAUtilTest.properties.get("hibernate.cache.use_second_level_cache"));

		JPAUtilTest.LOG.debug("connection url = " + connectionURL);
		JPAUtilTest.LOG.debug("connection user name = " + connectionUserName);
		//JPAUtilTest.LOG.debug("connection password = " + connectionPassword);
		JPAUtilTest.LOG.debug("connection dialect = " + dialect);
		JPAUtilTest.LOG.debug("connection shared cache mode = " + sharedCacheMode);
		JPAUtilTest.LOG.debug("connection enable L1 cache = " + enableL1Cache);
		JPAUtilTest.LOG.debug("connection enable L2 cache = " + enableL2Cache);

		JPAUtil.initEntityManagerFactory(connectionURL, connectionUserName, "", dialect, sharedCacheMode, enableL1Cache,
				enableL2Cache);

		final boolean result = JPAUtil.checkDBConnection();

		Assert.assertTrue("database connection check should be true", result);
	}
}
