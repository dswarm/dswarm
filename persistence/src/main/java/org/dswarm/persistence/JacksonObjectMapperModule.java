package org.dswarm.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.module.guice.ObjectMapperModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class JacksonObjectMapperModule extends AbstractModule {

	private JsonInclude.Include[] includes;

	private boolean customTransformation = true;

	public JacksonObjectMapperModule include(final JsonInclude.Include... includes) {
		this.includes = includes;
		return this;
	}

	public JacksonObjectMapperModule withoutTransformation() {
		this.customTransformation = false;
		return this;
	}

	@Override
	protected void configure() {
		install(jacksonModule());
	}

	private Module jacksonModule() {

		final ObjectMapperModule module = new ObjectMapperModule()
				.registerModule(new JaxbAnnotationModule())
				.registerModule(new Hibernate4Module());

		if (includes != null && includes.length > 0) {
			module.registerModule(new SerializationInclusionModule(includes));
		}
		if (customTransformation) {
			module.registerModule(new PersistenceModule.DmpDeserializerModule());
		}

		return module;
	}

	private static class SerializationInclusionModule extends SimpleModule {
		private final JsonInclude.Include[] includes;

		public SerializationInclusionModule(final JsonInclude.Include[] includes) {
			super("SerializationInclusionModule");
			this.includes = includes;
		}

		@Override
		public void setupModule(final SetupContext context) {
			super.setupModule(context);
			final ObjectCodec owner = context.getOwner();
			if (owner instanceof ObjectMapper) {
				final ObjectMapper mapper = (ObjectMapper) owner;
				for (final JsonInclude.Include include : includes) {
					mapper.setSerializationInclusion(include);
				}
			}
		}
	}
}
