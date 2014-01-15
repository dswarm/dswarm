package de.avgl.dmp.controller.resources.schema.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class SchemasResourceUtils extends BasicDMPResourceUtils<SchemaService, Schema> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(SchemasResourceUtils.class);

	@Inject
	public SchemasResourceUtils(final Provider<SchemaService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Schema.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
