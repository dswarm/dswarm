package de.avgl.dmp.controller.providers;

import javax.persistence.EntityManager;

import com.google.common.eventbus.EventBus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import de.avgl.dmp.controller.eventbus.ConverterEventRecorder;
import de.avgl.dmp.controller.factories.DMPEntityManagerFactory;
import de.avgl.dmp.controller.factories.EventBusFactory;
import de.avgl.dmp.persistence.model.internal.InternalMemoryDb;

public class DMPBinder extends AbstractBinder {
	@Override
	protected void configure() {
		final InternalMemoryDb db = new InternalMemoryDb();
		final ConverterEventRecorder eventRecorder = new ConverterEventRecorder(db);


		bind(db).to(InternalMemoryDb.class);

		bindFactory(new EventBusFactory(eventRecorder)).to(EventBus.class);

		bindFactory(DMPEntityManagerFactory.class).to(EntityManager.class).in(RequestScoped.class);
	}
}
