package de.avgl.dmp.controller.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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

			/**
			 * Provides the {@link ObjectMapper} instance for JSON de-/serialisation.
			 *
			 * @return a {@link ObjectMapper} instance as singleton
			 */
			@Provides
			@Singleton
			protected ObjectMapper provideObjectMapper() {
				final ObjectMapper mapper = new ObjectMapper();
				final JaxbAnnotationModule module = new JaxbAnnotationModule();
				mapper.registerModule(module).registerModule(new Hibernate4Module()).setSerializationInclusion(JsonInclude.Include.NON_NULL)
						.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

				return mapper;
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
