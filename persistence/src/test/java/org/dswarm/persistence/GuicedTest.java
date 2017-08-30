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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.typesafe.config.Config;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.dswarm.init.ConfigModule;
import org.dswarm.init.ExecutionScope;
import org.dswarm.init.LoggingConfigurator;
import org.dswarm.persistence.service.MaintainDBService;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;

public abstract class GuicedTest {

	protected static Injector injector;

	protected MaintainDBService maintainDBService;

	public static Injector getInjector() {

		final ConfigModule configModule = new ConfigModule();
		final Config config = configModule.getConfig();
		LoggingConfigurator.configureFrom(config);

		return Guice.createInjector(
				configModule,
				new PersistenceModule(),
				new JacksonObjectMapperModule(),
				new JpaHibernateModule(config)
		);
	}

	public static <T> T configValue(final String configPath, final Class<T> cls) {
		return Preconditions.checkNotNull(injector).getInstance(Key.get(cls, Names.named(configPath)));
	}

	public static <T> Optional<T> getInstance(final Class<T> tClass) {
		if (injector != null) {
			return Optional.fromNullable(injector.getInstance(tClass));
		} else {
			return Optional.absent();
		}
	}

	@Before
	public void prepare() throws Exception {

		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.initDB();
		//		maintainDBService.truncateTables();
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	@After
	public void tearDown3() throws Exception {

		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.initDB();
		//		maintainDBService.truncateTables();
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	protected void initObjects() {

		maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);
	}

	@BeforeClass
	public static void startUp() throws Exception {

		final Injector newInjector = GuicedTest.getInjector();
		newInjector.getInstance(PersistService.class).start();
		newInjector.getInstance(ExecutionScope.class).enter();
		startUp(newInjector);
	}

	public static void startUp(final Injector newInjector) {
		GuicedTest.injector = newInjector;
	}

	@AfterClass
	public static void tearDown() throws Exception {

		if (GuicedTest.injector != null) {
			try {
				GuicedTest.injector.getInstance(PersistService.class).stop();
				GuicedTest.injector.getInstance(ExecutionScope.class).leave();
			} catch (final IllegalStateException ignore) {
				// was already closed, ignore this
			}
			GuicedTest.injector = null;
		}
	}
}
