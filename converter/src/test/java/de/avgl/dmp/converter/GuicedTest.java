package de.avgl.dmp.converter;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.avgl.dmp.persistence.PersistenceModule;

public class GuicedTest {

	protected static transient Injector injector;

	protected static Injector getInjector() {

		class TestModule extends PersistenceModule {
			@Override
			protected EventBus provideEventBus() {
				return new EventBus();
			}
		}

		return Guice.createInjector(
				new TestModule(),
				new JpaPersistModule("DMPApp"));
	}

	@BeforeClass
	public static void startUp() throws Exception {

		injector = getInjector();

		injector.getInstance(PersistService.class).start();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		injector.getInstance(PersistService.class).stop();
	}
}
