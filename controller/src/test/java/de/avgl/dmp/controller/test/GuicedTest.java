package de.avgl.dmp.controller.test;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.avgl.dmp.controller.guice.DMPModule;
import de.avgl.dmp.persistence.PersistenceModule;

public class GuicedTest {

	protected static transient Injector 					injector;

	protected static Injector getInjector() {

		class TestModule extends PersistenceModule {

			@Override
			protected void configure() {
				super.configure();

				bind(Integer.class).annotatedWith(Names.named("NumberOfIterations")).toInstance(10);
				bind(Integer.class).annotatedWith(Names.named("SleepingTime")).toInstance(1000);
			}

			@Override
			protected EventBus provideEventBus() {
				return new EventBus();
			}
		}

		return Guice.createInjector(
				new TestModule(),
				new DMPModule(),
				new JpaPersistModule("DMPApp"));

	}

	@BeforeClass
	public static void startUp() throws Exception {

		injector = getInjector();

		injector.getInstance(PersistService.class).start();
		
		de.avgl.dmp.persistence.GuicedTest.startUp();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		injector.getInstance(PersistService.class).stop();
		
		de.avgl.dmp.persistence.GuicedTest.tearDown();
	}
}
