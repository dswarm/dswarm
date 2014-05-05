package de.avgl.dmp.controller.resources.schema.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.AttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePathInstance;
import de.avgl.dmp.persistence.service.schema.AttributePathInstanceService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class AttributePathInstancesResourceUtils<POJOCLASSPERSISTENCESERVICE extends AttributePathInstanceService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAttributePathInstance<POJOCLASS>, POJOCLASS extends AttributePathInstance>
		extends BasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributePathInstancesResourceUtils.class);

	public AttributePathInstancesResourceUtils(final Class<POJOCLASS> pojoClassArg,
			final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final POJOCLASS object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final AttributePath attributePath = object.getAttributePath();

		if (attributePath != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(AttributePathsResourceUtils.class).replaceRelevantDummyIds(attributePath, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}
}
