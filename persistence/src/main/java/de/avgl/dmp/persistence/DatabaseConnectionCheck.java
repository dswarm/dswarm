package de.avgl.dmp.persistence;


import java.math.BigInteger;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
public class DatabaseConnectionCheck {

	private final Provider<EntityManager> entityManagerProvider;
	private final String preferredTestQuery;
	private final String jdbcUrl;

	@Inject
	public DatabaseConnectionCheck(final Provider<EntityManager> entityManagerProvider, final Provider<EntityManagerFactory> entityManagerFactoryProvider) {

		this.entityManagerProvider = entityManagerProvider;

		final EntityManagerFactory entityManagerFactory = entityManagerFactoryProvider.get();
		final Map<String, Object> properties = entityManagerFactory.getProperties();

		this.preferredTestQuery = getPersistenceProperty(properties, "hibernate.c3p0.preferredTestQuery", "SELECT 1", String.class);
		this.jdbcUrl = getPersistenceProperty(properties, "javax.persistence.jdbc.url", "", String.class);
	}

	private <T> T getPersistenceProperty(final Map<String, Object> properties, final String key, final T defaultValue, final Class<T> tClass) {

		final Object value = properties.get(key);
		if (value == null) {
			return defaultValue;
		}

		return tClass.cast(value);
	}

	public boolean isConnected() {

		final EntityManager entityManager = entityManagerProvider.get();
		final Query nativeQuery = entityManager.createNativeQuery(preferredTestQuery);

		// TODO: Very much tied to SELECT 1
		final int firstResult = ((BigInteger) nativeQuery.getSingleResult()).intValue();

		return firstResult == 1;
	}

	public String getUrl() {
		return jdbcUrl;
	}
}
