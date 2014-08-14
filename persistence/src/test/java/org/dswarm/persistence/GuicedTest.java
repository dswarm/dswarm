package org.dswarm.persistence;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.typesafe.config.Config;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.dswarm.init.ConfigModule;
import org.dswarm.init.LoggingConfigurator;


public abstract class GuicedTest {

	protected static Injector injector;

	public static Injector getInjector() {
		final ConfigModule configModule = new ConfigModule();
		final Injector configInjector = Guice.createInjector(configModule);

		final Config config = configInjector.getInstance(Config.class);
		LoggingConfigurator.configureFrom(config);

		return configInjector.createChildInjector(
				new PersistenceModule(), new JacksonObjectMapperModule(), new JpaHibernateModule(configInjector));
	}

	public static <T> T configValue(final String configPath, final Class<T> cls) {
		return Preconditions.checkNotNull(injector).getInstance(Key.get(cls, Names.named(configPath)));
	}

	@BeforeClass
	public static void startUp() throws Exception {

		GuicedTest.injector = GuicedTest.getInjector();
		GuicedTest.injector.getInstance(PersistService.class).start();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		GuicedTest.injector.getInstance(PersistService.class).stop();
	}
}
