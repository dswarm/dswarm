package de.avgl.dmp.controller.resources.schema.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class SchemasResourceUtils extends BasicDMPResourceUtils<SchemaService, Schema> {

	private static final org.apache.log4j.Logger		LOG	= org.apache.log4j.Logger.getLogger(SchemasResourceUtils.class);

	private final Provider<AttributePathsResourceUtils>	attributePathsResourceUtilsProvider;

	@Inject
	public SchemasResourceUtils(final Provider<SchemaService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final Provider<AttributePathsResourceUtils> attributePathsResourceUtilsProviderArg) {

		super(Schema.class, persistenceServiceProviderArg, objectMapperProviderArg);

		attributePathsResourceUtilsProvider = attributePathsResourceUtilsProviderArg;
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

				attributePathsResourceUtilsProvider.get().replaceRelevantDummyIds(attributePath, jsonNode, dummyIdCandidates);
			}
		}

		return jsonNode;
	}
}
