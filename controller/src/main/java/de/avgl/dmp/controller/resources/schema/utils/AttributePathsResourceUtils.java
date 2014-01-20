package de.avgl.dmp.controller.resources.schema.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.BasicIDResourceUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class AttributePathsResourceUtils extends BasicIDResourceUtils<AttributePathService, AttributePath> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributePathsResourceUtils.class);

	private final Provider<AttributesResourceUtils>	attributesResourceUtilsProvider;

	@Inject
	public AttributePathsResourceUtils(final Provider<AttributePathService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final Provider<AttributesResourceUtils> attributesResourceUtilsProviderArg) {

		super(AttributePath.class, persistenceServiceProviderArg, objectMapperProviderArg);

		attributesResourceUtilsProvider = attributesResourceUtilsProviderArg;
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final AttributePath object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {
		
		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);
		
		final Set<Attribute> attributes = object.getAttributes();
		
		if(attributes != null) {
			
			for(final Attribute attribute : attributes) {
				
				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}
				
				attributesResourceUtilsProvider.get().replaceRelevantDummyIds(attribute, jsonNode, dummyIdCandidates);
			}
		}
		
		return jsonNode;
	}
}
