package de.avgl.dmp.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class TransformationsResourceUtils extends BasicFunctionsResourceUtils<TransformationService, Transformation> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(TransformationsResourceUtils.class);

	private final Provider<ComponentsResourceUtils>	componentsResourceUtilsProvider;

	@Inject
	public TransformationsResourceUtils(final Provider<TransformationService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final Provider<ComponentsResourceUtils> componentsResourceUtilsProviderArg) {

		super(Transformation.class, persistenceServiceProviderArg, objectMapperProviderArg);

		componentsResourceUtilsProvider = componentsResourceUtilsProviderArg;
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Transformation object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Set<Component> components = object.getComponents();

		if (components != null) {

			for (final Component component : components) {

				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}

				componentsResourceUtilsProvider.get().replaceRelevantDummyIds(component, jsonNode, dummyIdCandidates);
			}
		}

		return jsonNode;
	}
}
