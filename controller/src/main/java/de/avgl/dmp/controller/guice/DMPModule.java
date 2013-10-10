package de.avgl.dmp.controller.guice;

import com.google.inject.AbstractModule;

import de.avgl.dmp.controller.eventbus.ConverterEventRecorder;
import de.avgl.dmp.controller.eventbus.XMLSchemaEventRecorder;
import de.avgl.dmp.controller.status.DMPStatus;

public class DMPModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConverterEventRecorder.class).asEagerSingleton();
		bind(XMLSchemaEventRecorder.class).asEagerSingleton();

		bind(DMPStatus.class);
	}
}
