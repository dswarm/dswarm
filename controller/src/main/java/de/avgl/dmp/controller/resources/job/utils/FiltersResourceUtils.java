package de.avgl.dmp.controller.resources.job.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.proxy.ProxyFilter;
import de.avgl.dmp.persistence.service.job.FilterService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class FiltersResourceUtils extends BasicDMPResourceUtils<FilterService, ProxyFilter, Filter> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(FiltersResourceUtils.class);

	@Inject
	public FiltersResourceUtils(final Provider<FilterService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(Filter.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public String prepareObjectJSONString(final String objectJSONString) throws DMPControllerException {

		// a filter is not a complex object

		return objectJSONString;
	}
}
