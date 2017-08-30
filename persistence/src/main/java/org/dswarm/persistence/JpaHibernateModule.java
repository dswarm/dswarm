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

import java.util.Properties;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.typesafe.config.Config;

public class JpaHibernateModule extends AbstractModule {

	private final String  uri;
	private final String  username;
	private final String  password;
	private final boolean isLogSql;
	private final String  jpaUnit;

	public JpaHibernateModule(final Config config) {
		Preconditions.checkNotNull(config);
		final Config metadataConfig = config.getConfig("dswarm.db.metadata");

		uri = metadataConfig.getString("uri");
		username = metadataConfig.getString("username");
		password = metadataConfig.getString("password");
		isLogSql = metadataConfig.getBoolean("log-sql");
		jpaUnit = metadataConfig.getString("jpa-unit");
	}

	@Override
	protected void configure() {
		install(persistModule());
	}

	private Properties persistenceConfig() {

		final Properties properties = new Properties();

		properties.setProperty("javax.persistence.jdbc.url", uri);
		properties.setProperty("javax.persistence.jdbc.user", username);
		properties.setProperty("javax.persistence.jdbc.password", password);

		// TODO: load more values from config, e.g. connection pool settings
		/*properties.setProperty("hibernate.c3p0.acquireRetryAttempts", "3");
		properties.setProperty("hibernate.c3p0.acquireRetryDelay", "100");
		properties.setProperty("hibernate.c3p0.breakAfterAcquireFailure", "false");
		properties.setProperty("hibernate.c3p0.checkoutTimeout", "0");
		properties.setProperty("hibernate.c3p0.idle_test_period", "30");
		properties.setProperty("hibernate.c3p0.max_size", "20");
		properties.setProperty("hibernate.c3p0.max_statements", "0");
		properties.setProperty("hibernate.c3p0.maxConnectionAge", "500");
		properties.setProperty("hibernate.c3p0.maxIdleTimeExcessConnections", "45");
		properties.setProperty("hibernate.c3p0.maxStatementsPerConnection", "20");
		properties.setProperty("hibernate.c3p0.min_size", "5");
		properties.setProperty("hibernate.c3p0.numHelperThreads", "1"); */
		properties.setProperty("hibernate.c3p0.preferredTestQuery", "SELECT 1");
		/*properties.setProperty("hibernate.c3p0.testConnectionOnCheckout", "true");
		properties.setProperty("hibernate.c3p0.timeout", "10");
		properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.internal.NoCachingRegionFactory");
		properties.setProperty("hibernate.cache.use_query_cache", "false");
		properties.setProperty("hibernate.cache.use_second_level_cache", "false");
		properties.setProperty("hibernate.connection.autoReconnect", "true");
		properties.setProperty("hibernate.connection.autoReconnectForPools", "true");
		properties.setProperty("hibernate.connection.is-connection-validation-required", "true");
		properties.setProperty("hibernate.connection.provider_class", "org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
		properties.setProperty("hibernate.connection.shutdown", "true");
		properties.setProperty("hibernate.current_session_context_class", "thread");
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
		properties.setProperty("hibernate.discriminator.ignore_explicit_for_joined", "true");
		properties.setProperty("hibernate.show_sql", String.valueOf(isLogSql)); */
		properties.setProperty("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
		/*properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
		properties.setProperty("hibernate.ejb.entitymanager_factory_name", "DMPAppFactory");
		properties.setProperty("hibernate.hbm2ddl.auto", "");*/

		properties.setProperty("eclipselink.logging.level", "WARNING");
//		properties.setProperty("eclipselink.logging.level.sql", "FINE");
//		properties.setProperty("eclipselink.logging.parameters", "true");
		properties.setProperty("eclipselink.cache.shared.default", "false");
		properties.setProperty("eclipselink.weaving", "static");

		return properties;
	}

	private Module persistModule() {

		// to fix depracted hibernate persistence provider issue, see also https://hibernate.atlassian.net/browse/HHH-9141?focusedCommentId=64948&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-64948
		//		PersistenceProviderResolverHolder.setPersistenceProviderResolver(new PersistenceProviderResolver() {
		//
		//			private final List<PersistenceProvider> providers_ = asList((PersistenceProvider) new PersistenceProviderImpl());
		//
		//			@Override
		//			public List<PersistenceProvider> getPersistenceProviders() {
		//				return providers_;
		//			}
		//
		//			@Override public void clearCachedProviders() {
		//
		//				providers_.clear();
		//			}
		//		});

		return new JpaPersistModule(jpaUnit).properties(persistenceConfig());
	}
}
