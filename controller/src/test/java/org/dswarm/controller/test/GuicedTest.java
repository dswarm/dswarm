package org.dswarm.controller.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.typesafe.config.Config;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.dswarm.controller.guice.DMPModule;
import org.dswarm.init.ConfigModule;
import org.dswarm.init.LoggingConfigurator;
import org.dswarm.persistence.JacksonObjectMapperModule;
import org.dswarm.persistence.JpaHibernateModule;
import org.dswarm.persistence.PersistenceModule;


public abstract class GuicedTest {

	protected static volatile Injector injector;

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
				new JpaHibernateModule(configInjector),
				new PersistenceModule(),
				new DMPModule(),
				new TestModule());
	}

	@BeforeClass
	public static void startUp() throws Exception {

		GuicedTest.injector = GuicedTest.getInjector();
		GuicedTest.injector.getInstance(PersistService.class).start();
		org.dswarm.persistence.GuicedTest.startUp();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		final PersistService persistService = GuicedTest.injector.getInstance(PersistService.class);
		persistService.stop();
		org.dswarm.persistence.GuicedTest.tearDown();
	}


	static class TestModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(Integer.class).annotatedWith(Names.named("NumberOfIterations")).toInstance(10);
			bind(Integer.class).annotatedWith(Names.named("SleepingTime")).toInstance(1000);
		}
	}
}
