/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.typesafe.config.Config;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.dswarm.init.ConfigModule;
import org.dswarm.init.LoggingConfigurator;
import org.dswarm.persistence.JacksonObjectMapperModule;
import org.dswarm.persistence.JpaHibernateModule;
import org.dswarm.persistence.PersistenceModule;


public abstract class GuicedTest {

	protected static Injector injector;

	public static Injector getInjector() {
		final ConfigModule configModule = new ConfigModule();
		final Injector configInjector = Guice.createInjector(configModule);

		final Config config = configInjector.getInstance(Config.class);
		LoggingConfigurator.configureFrom(config);

		final JacksonObjectMapperModule objectMapperModule = new JacksonObjectMapperModule()
				.include(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_EMPTY)
				.withoutTransformation();

		return configInjector.createChildInjector(
				objectMapperModule,
				new PersistenceModule(),
				new JpaHibernateModule(configInjector));
	}

	@BeforeClass
	public static void startUp() throws Exception {

		GuicedTest.injector = GuicedTest.getInjector();
		GuicedTest.injector.getInstance(PersistService.class).start();
		org.dswarm.persistence.GuicedTest.startUp();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		GuicedTest.injector.getInstance(PersistService.class).stop();
		org.dswarm.persistence.GuicedTest.tearDown();
	}
}
