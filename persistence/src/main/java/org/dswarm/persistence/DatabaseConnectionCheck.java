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
package org.dswarm.persistence;

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
		final int firstResult = ((Long) nativeQuery.getSingleResult()).intValue();

		return firstResult == 1;
	}

	public String getUrl() {
		return jdbcUrl;
	}
}
