package org.dswarm.controller.resources.schema.utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.utils.BasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * @author tgaengler
 */
public class ContentSchemasResourceUtils extends BasicDMPResourceUtils<ContentSchemaService, ProxyContentSchema, ContentSchema> {

	@Inject
	public ContentSchemasResourceUtils(final Provider<ContentSchemaService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(ContentSchema.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final ContentSchema object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		final Map<Long, AttributePath> keyAttributeDummyIds;

		final LinkedList<AttributePath> keyAttributePaths = object.getKeyAttributePaths();
		final LinkedList<AttributePath> newKeyAttributePaths = Lists.newLinkedList();

		JsonNode enhancedJsonNode = jsonNode;

		if (keyAttributePaths != null) {

			keyAttributeDummyIds = Maps.newHashMap();

			for (final AttributePath keyAttributePath : keyAttributePaths) {

				if (keyAttributeDummyIds.containsKey(keyAttributePath.getId())) {

					newKeyAttributePaths.add(keyAttributeDummyIds.get(keyAttributePath.getId()));

					continue;
				}

				if (dummyIdCandidates.contains(object.getId()) || (object.getId() != null && object.getId() < 0)) {

					final AttributePathsResourceUtils attributePathsResourceUtils = utilsFactory.get(AttributePathsResourceUtils.class);

					if (!attributePathsResourceUtils.hasObjectAlreadyBeenProcessed(keyAttributePath.getId())) {

						final ProxyAttributePath proxyNewAttributePath = attributePathsResourceUtils.createNewObject(keyAttributePath);

						if (proxyNewAttributePath == null) {

							throw new DMPControllerException("couldn't create or retrieve attribute path");
						}

						final AttributePath newAttributePath = proxyNewAttributePath.getObject();

						if (proxyNewAttributePath.getType().equals(RetrievalType.CREATED)) {

							if (newAttributePath == null) {

								throw new DMPControllerException("couldn't create new attribute path");
							}

							newAttributePath.setAttributePath(keyAttributePath.getAttributePath());
						}

						enhancedJsonNode = attributePathsResourceUtils.processDummyId(enhancedJsonNode, keyAttributePath.getId(),
								newAttributePath.getId(), dummyIdCandidates);
						keyAttributeDummyIds.put(keyAttributePath.getId(), newAttributePath);

						newKeyAttributePaths.add(newAttributePath);

						continue;
					}
				}

				newKeyAttributePaths.add(keyAttributePath);
			}
		}

		object.setKeyAttributePaths(newKeyAttributePaths);

		super.replaceRelevantDummyIds(object, enhancedJsonNode, dummyIdCandidates);

		final AttributePath valueAttributePath = object.getValueAttributePath();

		if (valueAttributePath != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return enhancedJsonNode;
			}

			utilsFactory.get(AttributePathsResourceUtils.class).replaceRelevantDummyIds(valueAttributePath, enhancedJsonNode, dummyIdCandidates);
		}

		return enhancedJsonNode;
	}
}
