package de.avgl.dmp.controller.resources.utils;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.job.utils.ComponentsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.FiltersResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.FunctionsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.MappingsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.ProjectsResourceUtils;
import de.avgl.dmp.controller.resources.job.utils.TransformationsResourceUtils;
import de.avgl.dmp.controller.resources.resource.utils.ConfigurationsResourceUtils;
import de.avgl.dmp.controller.resources.resource.utils.DataModelsResourceUtils;
import de.avgl.dmp.controller.resources.resource.utils.ResourcesResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.AttributePathsResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.AttributesResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.ClaszesResourceUtils;
import de.avgl.dmp.controller.resources.schema.utils.SchemasResourceUtils;

/**
 * A Proxy around Guice's {@code Provider} to emulate Request-scoped behaviour
 * when it's not applicable otherwise.  This might exists due to our restricted
 * knowledge about the internals of Guice...
 *
 * The problem is, that the Utils classes need to be RequestScoped but Guice would
 * somehow refrain from applying that scope. {@code ResourceUtilsFactory} does
 * cache the instances, which are lazily loaded via standard {@code Provider}s
 * and offers a {@code reset()} method, to reset the cache, effectively creating
 * a new scope, given that the Utils class itself are not Singleton-scoped.
 *
 * see https://jira.slub-dresden.de/browse/DD-311
 * @author phorn
 */
@Singleton
public class ResourceUtilsFactory {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ResourceUtilsFactory.class);

	private static final String PROVIDER_NOT_FOUND = "Cannot find a Provider of %s";
	private static final String PROVIDER_CAST_FAIL = "Cannot cast %s to Provider of %s";
	private static final String INSTANCE_CAST_FAIL = "Cannot cast %s to %s";

	private final Map<Class<? extends BasicResourceUtils>, BasicResourceUtils> instances = Maps.newHashMapWithExpectedSize(13);
	private final Map<Class<? extends BasicResourceUtils>, Provider<? extends BasicResourceUtils>> providers = Maps.newHashMapWithExpectedSize(13);

	@Inject
	public ResourceUtilsFactory(final Provider<AttributePathsResourceUtils> attributePathsResourceUtilsProvider,
	                            final Provider<AttributesResourceUtils> attributesResourceUtilsProvider,
	                            final Provider<ClaszesResourceUtils> claszesResourceUtilsProvider,
	                            final Provider<ComponentsResourceUtils> componentsResourceUtilsProvider,
	                            final Provider<ConfigurationsResourceUtils> configurationsResourceUtilsProvider,
	                            final Provider<DataModelsResourceUtils> dataModelsResourceUtilsProvider,
	                            final Provider<FiltersResourceUtils> filtersResourceUtilsProvider,
	                            final Provider<FunctionsResourceUtils> functionsResourceUtilsProvider,
	                            final Provider<MappingsResourceUtils> mappingsResourceUtilsProvider,
	                            final Provider<ProjectsResourceUtils> projectsResourceUtilsProvider,
	                            final Provider<ResourcesResourceUtils> resourceResourceUtils,
	                            final Provider<SchemasResourceUtils> schemasResourceUtilsProvider,
	                            final Provider<TransformationsResourceUtils> transformationsResourceUtilsProvider) {

		providers.put(AttributePathsResourceUtils.class, attributePathsResourceUtilsProvider);
		providers.put(AttributesResourceUtils.class, attributesResourceUtilsProvider);
		providers.put(ClaszesResourceUtils.class, claszesResourceUtilsProvider);
		providers.put(ComponentsResourceUtils.class, componentsResourceUtilsProvider);
		providers.put(ConfigurationsResourceUtils.class, configurationsResourceUtilsProvider);
		providers.put(DataModelsResourceUtils.class, dataModelsResourceUtilsProvider);
		providers.put(FiltersResourceUtils.class, filtersResourceUtilsProvider);
		providers.put(FunctionsResourceUtils.class, functionsResourceUtilsProvider);
		providers.put(MappingsResourceUtils.class, mappingsResourceUtilsProvider);
		providers.put(ProjectsResourceUtils.class, projectsResourceUtilsProvider);
		providers.put(ResourcesResourceUtils.class, resourceResourceUtils);
		providers.put(SchemasResourceUtils.class, schemasResourceUtilsProvider);
		providers.put(TransformationsResourceUtils.class, transformationsResourceUtilsProvider);
	}

	/**
	 * Reset the internal instance cache, effectively creating a new injection scope.
	 * You would use this within an constructor of a Jersey Resource, for example.
	 * @return this instance for a fluent interface;
	 */
	public synchronized ResourceUtilsFactory reset() {
		instances.clear();

		return this;
	}

	/**
	 * Get an instance of any BasicResourceUtils
	 * @param cls   the class of the desired instance
	 * @param <T>   the type of the desired instance
	 * @return      an instance of T, guaranteed to be not null;
	 * @throws DMPControllerException if there is no provider for this type available
	 */
	public synchronized <T extends BasicResourceUtils> T get(final Class<T> cls) throws DMPControllerException {
		LOG.debug(String.format("Lookup ResourceUtils for %s", cls.getSimpleName()));
		final T instance;

		final BasicResourceUtils utils = instances.get(cls);
		if (utils == null) {

			LOG.debug(String.format("No previous instance found, create a new one for %s", cls.getSimpleName()));

			final Provider<? extends BasicResourceUtils> genericProvider = providers.get(cls);
			if (genericProvider == null) {
				throw new DMPControllerException(String.format(PROVIDER_NOT_FOUND, cls.getSimpleName()));
			}

			final Provider<T> provider;
			try {
				//noinspection unchecked
				provider = (Provider<T>) genericProvider;
			} catch (final ClassCastException e) {
				throw new DMPControllerException(String.format(PROVIDER_CAST_FAIL, genericProvider, cls.getSimpleName()), e);
			}

			instance = provider.get();
			instances.put(cls, instance);

		} else {

			try {
				//noinspection unchecked
				instance = (T) utils;
			} catch (final ClassCastException e) {
				throw new DMPControllerException(String.format(INSTANCE_CAST_FAIL, utils, cls.getSimpleName()), e);
			}

		}

		LOG.debug(String.format("Return instance %s for %s", instance, cls.getSimpleName()));

		return instance;
	}
}
