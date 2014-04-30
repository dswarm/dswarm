package de.avgl.dmp.controller.resources.schema.utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.BasicIDResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.proxy.RetrievalType;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;

/**
 * @author tgaengler
 */
public class AttributePathsResourceUtils extends BasicIDResourceUtils<AttributePathService, ProxyAttributePath, AttributePath> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributePathsResourceUtils.class);

	@Inject
	public AttributePathsResourceUtils(final Provider<AttributePathService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(AttributePath.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final AttributePath object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		final Map<Long, Attribute> attributeDummyIds;

		final LinkedList<Attribute> attributes = object.getAttributePath();
		final LinkedList<Attribute> newAttributes = Lists.newLinkedList();

		JsonNode enhancedJsonNode = jsonNode;

		if (attributes != null) {

			attributeDummyIds = Maps.newHashMap();

			for (final Attribute attribute : attributes) {

				if (attributeDummyIds.containsKey(attribute.getId())) {

					newAttributes.add(attributeDummyIds.get(attribute.getId()));

					continue;
				}

				if (dummyIdCandidates.contains(object.getId()) || (object.getId() != null && object.getId().longValue() < 0)) {

					final AttributesResourceUtils attributesResourceUtils = utilsFactory.get(AttributesResourceUtils.class);

					if (!attributesResourceUtils.hasObjectAlreadyBeenProcessed(attribute.getId())) {

						final ProxyAttribute proxyNewAttribute = attributesResourceUtils.createNewObject(attribute);

						if (proxyNewAttribute == null) {

							throw new DMPControllerException("couldn't create or retrieve attribute");
						}

						final Attribute newAttribute = proxyNewAttribute.getObject();

						if (proxyNewAttribute.getType().equals(RetrievalType.CREATED)) {

							if (newAttribute == null) {

								throw new DMPControllerException("couldn't create new attribute");
							}

							newAttribute.setName(attribute.getName());
						}

						enhancedJsonNode = attributesResourceUtils.processDummyId(enhancedJsonNode, attribute.getId(), newAttribute.getId(),
								dummyIdCandidates);
						attributeDummyIds.put(attribute.getId(), newAttribute);

						newAttributes.add(newAttribute);

						continue;
					}
				}

				newAttributes.add(attribute);
			}
		}

		object.setAttributePath(newAttributes);

		super.replaceRelevantDummyIds(object, enhancedJsonNode, dummyIdCandidates);

		return enhancedJsonNode;
	}

	@Override
	public ProxyAttributePath createObject(final AttributePath objectFromJSON, final AttributePathService persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createOrGetObjectTransactional(objectFromJSON.getAttributePath());
	}
}
