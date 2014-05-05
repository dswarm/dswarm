package de.avgl.dmp.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.job.ComponentService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ComponentsResourceUtils extends ExtendedBasicDMPResourceUtils<ComponentService, ProxyComponent, Component> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ComponentsResourceUtils.class);

	@Inject
	public ComponentsResourceUtils(final Provider<ComponentService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(Component.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Component object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Function function = object.getFunction();

		if (function != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(FunctionsResourceUtils.class).replaceRelevantDummyIds(function, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}

	@Override
	public String prepareObjectJSONString(final String objectJSONString) throws DMPControllerException {

		// TODO: remove id from parameter mappings (?) -> avoid dummy id creation there

		return objectJSONString;
	}
}
