package de.avgl.dmp.controller.resources.schema.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
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

		final Set<AttributePath> attributePaths = object.getAttributePaths();

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

		return jsonNode;
	}
}
