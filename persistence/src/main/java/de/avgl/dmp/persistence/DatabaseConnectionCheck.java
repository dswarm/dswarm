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

	private final Provider<EntityManager>	entityManagerProvider;
	private final String					preferredTestQuery;
	private final String					jdbcUrl;

	@Inject
	public DatabaseConnectionCheck(final Provider<EntityManager> entityManagerProvider,
			final Provider<EntityManagerFactory> entityManagerFactoryProvider) {

		this.entityManagerProvider = entityManagerProvider;

		final EntityManagerFactory entityManagerFactory = entityManagerFactoryProvider.get();
		final Map<String, Object> properties = entityManagerFactory.getProperties();

		final Object preferredTestQuery = properties.get("hibernate.c3p0.preferredTestQuery");
		this.preferredTestQuery = preferredTestQuery == null ? "SELECT 1" : (String) preferredTestQuery;

		final Object jdbcUrl = properties.get("javax.persistence.jdbc.url");
		this.jdbcUrl = jdbcUrl == null ? "" : (String) jdbcUrl;
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
