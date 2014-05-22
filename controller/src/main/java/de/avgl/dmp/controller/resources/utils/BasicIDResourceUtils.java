package de.avgl.dmp.controller.resources.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicIDJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicIDResourceUtils<POJOCLASSPERSISTENCESERVICE extends BasicIDJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPJPAObject<POJOCLASS>, POJOCLASS extends DMPJPAObject>
		extends BasicResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicIDResourceUtils.class);

	public BasicIDResourceUtils(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, Long.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	protected ObjectNode replaceDummyId(final JsonNode idNode, final Long dummyId, final Long realId, final ObjectNode objectJSON) {

		if (idNode.canConvertToLong()) {

			final long longId = idNode.asLong();

			if (Long.class.isAssignableFrom(pojoClassIdType) && dummyId.equals(Long.valueOf(longId))) {

				// replace long id

				objectJSON.put("id", Long.valueOf(realId.toString()));
			}
		}

		return objectJSON;
	}

	@Override
	protected void checkObjectId(final JsonNode idNode) {

		if (idNode.canConvertToLong()) {

			final long longId = idNode.asLong();

			if (longId < 0) {

				addDummyIdCandidate(longId);
			}
		}
	}

	@Override
	protected ObjectNode addDummyId(final ObjectNode objectJSON) {

		final long randomDummyId = DMPPersistenceUtil.generateRandomDummyId();

		// add dummy id to object
		objectJSON.put("id", randomDummyId);

		return objectJSON;
	}
}
