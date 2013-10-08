package de.avgl.dmp.controller.guice;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;

import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import de.avgl.dmp.controller.eventbus.ConverterEventRecorder;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.internal.InternalMemoryDb;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.InternalService;
import de.avgl.dmp.persistence.services.ResourceService;
import de.avgl.dmp.persistence.services.impl.InternalServiceImpl;
import de.avgl.dmp.persistence.services.utils.JPAUtil;

public class DMPModule extends AbstractModule {

	@Override

		//	synchronous event bus
		//	bind(EventBus.class).asEagerSingleton();

		bind(InternalMemoryDb.class).asEagerSingleton();
	protected void configure() {
		bind(ConverterEventRecorder.class).asEagerSingleton();

		bind(ResourceService.class).in(Scopes.SINGLETON);
		bind(ConfigurationService.class).in(Scopes.SINGLETON);

		bind(InternalService.class).to(InternalServiceImpl.class);
		bind(DMPStatus.class);
	}

	@Provides
	protected EntityManager provideEntityManager() {
		return JPAUtil.getEntityManagerFactory().createEntityManager();
	}


	@Provides @Singleton
	protected MetricRegistry provideMetricRegistry() {
		return new MetricRegistry();
	}

	@Provides @Singleton
	protected EventBus provideEventBus() {
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(
				2, 10, 1, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>()
		);

		return new AsyncEventBus(executor);
	}

}
