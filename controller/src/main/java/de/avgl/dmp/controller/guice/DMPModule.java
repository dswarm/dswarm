package de.avgl.dmp.controller.guice;

import com.google.inject.AbstractModule;

import de.avgl.dmp.controller.eventbus.CSVConverterEventRecorder;
import de.avgl.dmp.controller.eventbus.SchemaEventRecorder;
import de.avgl.dmp.controller.eventbus.XMLConverterEventRecorder;
import de.avgl.dmp.controller.eventbus.XMLSchemaEventRecorder;
import de.avgl.dmp.controller.resources.job.utils.ComponentsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.FiltersResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.FunctionsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.MappingsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.ProjectsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.TransformationsResourceUtils;
import de.avgl.dmp.controller.resources.resource.utils.ConfigurationsResourceUtils;
import de.avgl.dmp.controller.resources.resource.utils.DataModelsResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.AttributePathsResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.AttributesResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.ClaszesResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.SchemasResourceUtils;
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

		bind(DMPStatus.class);
		
		// resource utils
		bind(ComponentsResourceUtils.class);
		bind(FiltersResourceUtils.class);
		bind(FunctionsResourceUtils.class);
		bind(MappingsResourceUtils.class);
		bind(ProjectsResourceUtils.class);
		bind(TransformationsResourceUtils.class);
		bind(ConfigurationsResourceUtils.class);
		bind(DataModelsResourceUtils.class);
		bind(AttributePathsResourceUtils.class);
		bind(AttributesResourceUtils.class);
		bind(ClaszesResourceUtils.class);
		bind(SchemasResourceUtils.class);
	}
}
