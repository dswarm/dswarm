package org.dswarm.controller.resources.schema.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.utils.BasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 */
public class SchemasResourceUtils extends BasicDMPResourceUtils<SchemaService, ProxySchema, Schema> {

	@Inject
	public SchemasResourceUtils(final Provider<SchemaService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(Schema.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Schema object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Set<AttributePath> attributePaths = object.getUniqueAttributePaths();

		if (attributePaths != null) {

			for (final AttributePath attributePath : attributePaths) {

				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}

				utilsFactory.get(AttributePathsResourceUtils.class).replaceRelevantDummyIds(attributePath, jsonNode, dummyIdCandidates);
			}
		}

		final Clasz recordClasz = object.getRecordClass();

		if (recordClasz != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(ClaszesResourceUtils.class).replaceRelevantDummyIds(recordClasz, jsonNode, dummyIdCandidates);
		}

		final ContentSchema contentSchema = object.getContentSchema();

		if (contentSchema != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(ContentSchemasResourceUtils.class).replaceRelevantDummyIds(contentSchema, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}
}
