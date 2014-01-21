package de.avgl.dmp.controller.resources.schema.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.AdvancedDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class AttributesResourceUtils extends AdvancedDMPResourceUtils<AttributeService, Attribute> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceUtils.class);

	@Inject
	public AttributesResourceUtils(final Provider<AttributeService> persistenceServiceProviderArg,
	                               final Provider<ObjectMapper> objectMapperProviderArg,
	                               final ResourceUtilsFactory utilsFactory) {

		super(Attribute.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
