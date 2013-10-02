package de.avgl.dmp.controller.providers;

import javax.persistence.EntityManager;

import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.EventBus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import de.avgl.dmp.controller.eventbus.ConverterEventRecorder;
import de.avgl.dmp.controller.factories.DMPEntityManagerFactory;
import de.avgl.dmp.controller.factories.EventBusFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.internal.InternalMemoryDb;
import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.InternalService;
import de.avgl.dmp.persistence.services.ResourceService;

public class DMPBinder extends AbstractBinder {
	@Override
	protected void configure() {
		final InternalMemoryDb internalMemoryDb = new InternalMemoryDb();

		final InternalService internalService = new InternalService();
		internalService.setMemoryDb(internalMemoryDb);

		final ConverterEventRecorder eventRecorder = new ConverterEventRecorder(internalService);

		final MetricRegistry registry = new MetricRegistry();
		bind(registry).to(MetricRegistry.class);
		bind(new DMPStatus(registry)).to(DMPStatus.class);


		bind(internalMemoryDb).to(InternalMemoryDb.class);
		bind(internalService).to(InternalService.class);
		bind(new ResourceService()).to(ResourceService.class);
		bind(new ConfigurationService()).to(ConfigurationService.class);

		bindFactory(new EventBusFactory(eventRecorder)).to(EventBus.class);
		bindFactory(DMPEntityManagerFactory.class).to(EntityManager.class).in(RequestScoped.class);
	}
}
