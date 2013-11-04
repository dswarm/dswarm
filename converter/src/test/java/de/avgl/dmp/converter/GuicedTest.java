package de.avgl.dmp.converter;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
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

		return Guice.createInjector(new TestModule());
	}

	@BeforeClass
	public static void startUp() throws Exception {

		injector = getInjector();
	}
}
