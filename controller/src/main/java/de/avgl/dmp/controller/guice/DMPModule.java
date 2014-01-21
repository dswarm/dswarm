package de.avgl.dmp.controller.guice;

import com.google.inject.AbstractModule;

import de.avgl.dmp.controller.eventbus.CSVConverterEventRecorder;
import de.avgl.dmp.controller.eventbus.SchemaEventRecorder;
import de.avgl.dmp.controller.eventbus.XMLConverterEventRecorder;
import de.avgl.dmp.controller.eventbus.XMLSchemaEventRecorder;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.controller.utils.DataModelUtil;

/**
 * The Guice configuration of the controller module. Interface/classes that are registered here can be utilised for injection.
 * Mainly event recorders, e.g., {@link XMLConverterEventRecorder}, are registered here.
 *
 * @author phorn
 */
public class DMPModule extends AbstractModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {

		bind(SchemaEventRecorder.class).asEagerSingleton();
		bind(CSVConverterEventRecorder.class).asEagerSingleton();
		bind(XMLConverterEventRecorder.class).asEagerSingleton();
		bind(XMLSchemaEventRecorder.class).asEagerSingleton();

		bind(DataModelUtil.class);
		bind(ResourceUtilsFactory.class);

		bind(DMPStatus.class);
	}
}
