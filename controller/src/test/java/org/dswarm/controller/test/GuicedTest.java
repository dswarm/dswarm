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
package org.dswarm.controller.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.dswarm.controller.guice.DMPModule;
import org.dswarm.controller.guice.DMPServletModule;
import org.dswarm.converter.ConverterModule;
import org.dswarm.init.ConfigModule;
import org.dswarm.init.LoggingConfigurator;
import org.dswarm.persistence.JacksonObjectMapperModule;
import org.dswarm.persistence.JpaHibernateModule;
import org.dswarm.persistence.PersistenceModule;

public abstract class GuicedTest {

	protected static volatile Injector injector;

	public static Injector getInjector() {
		final ConfigModule configModule = new ConfigModule();
		final Config config = configModule.getConfig();
		LoggingConfigurator.configureFrom(config);

		final JacksonObjectMapperModule objectMapperModule = new JacksonObjectMapperModule()
				.include(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_EMPTY)
				.withoutTransformation();

		return Guice.createInjector(
				new DMPServletModule(),
				configModule,
				objectMapperModule,
				new JpaHibernateModule(config),
				new PersistenceModule(),
				new ConverterModule(),
				new DMPModule(),
				new TestModule()
		);
	}

	@BeforeClass
	public static void startUp() throws Exception {

		final Injector newInjector = GuicedTest.getInjector();
		startUp(newInjector);
		// no manual PersistService start needed when the DMPServletModule is installed
	}

	public static void startUp(final Injector newInjector) {
		GuicedTest.injector = newInjector;
		org.dswarm.converter.GuicedTest.startUp(newInjector);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// no manual PersistService stop needed when the DMPServletModule is installed
	}

	static class TestModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(Integer.class).annotatedWith(Names.named("NumberOfIterations")).toInstance(10);
			bind(Integer.class).annotatedWith(Names.named("SleepingTime")).toInstance(1000);
		}
	}
}
