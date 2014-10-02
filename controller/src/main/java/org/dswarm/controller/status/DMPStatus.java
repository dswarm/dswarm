/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.status;

import java.lang.management.ManagementFactory;
import java.util.Map;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.dswarm.controller.resources.BasicResource;
import org.dswarm.controller.resources.resource.ResourcesResource;

/**
 * A metrics registry for the backend API. Every method of every resource can be registered here for statistical purpose.
 * 
 * @author phorn
 * @author tgaengler
 */
@Singleton
public class DMPStatus {

	private final Meter					allRequestsMeter;

	private final Timer					createNewResourceTimer;
	private final Timer					updateResourceTimer;
	private final Timer					deleteResourceTimer;
	private final Timer					configurationsPreviewTimer;
	private final Timer					createNewConfigurationsTimer;

	private final Timer					getAllResourcesTimer;
	private final Timer					getSingleResourcesTimer;
	private final Timer					getAllConfigurationsTimer;
	private final Timer					getSingleConfigurationTimer;
	private final Timer					getConfigurationsSchemaTimer;
	private final Timer					getConfigurationsDataTimer;

	private final Map<String, Timer>	getAllObjectsTimers		= Maps.newHashMap();
	private final Map<String, Timer>	createNewObjectTimers	= Maps.newHashMap();
	private final Map<String, Timer>	getObjectTimers			= Maps.newHashMap();
	private final Map<String, Timer>	updateObjectTimers		= Maps.newHashMap();
	private final Map<String, Timer>	deleteObjectTimers		= Maps.newHashMap();

	private final MetricRegistry		registry;

	/**
	 * Creates a new metrics registry for the backend API.
	 * 
	 * @param registryArg a metrics registry (that will be wrapped)
	 */
	@Inject
	public DMPStatus(final MetricRegistry registryArg) {

		registry = registryArg;

		allRequestsMeter = registry.meter(MetricRegistry.name(ResourcesResource.class, "requests", "all"));

		createNewResourceTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "post-requests", "resources", "create"));
		updateResourceTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "put-requests", "resources", "update"));
		deleteResourceTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "delete-requests", "resources", "delete"));
		configurationsPreviewTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "post-requests", "configurations", "preview"));
		createNewConfigurationsTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "post-requests", "configurations", "create"));

		getAllResourcesTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "get-requests", "resources", "all"));
		getSingleResourcesTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "get-requests", "resources", "specific"));
		getAllConfigurationsTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "get-requests", "configurations", "all"));
		getSingleConfigurationTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "get-requests", "configurations", "specific"));
		getConfigurationsSchemaTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "get-requests", "configurations", "schema"));
		getConfigurationsDataTimer = registry.timer(MetricRegistry.name(ResourcesResource.class, "get-requests", "configurations", "data"));
	}

	public Timer.Context createNewResource() {
		allRequestsMeter.mark();
		return createNewResourceTimer.time();
	}

	public Timer.Context updateResource() {
		allRequestsMeter.mark();
		return updateResourceTimer.time();
	}

	public Timer.Context deleteResource() {
		allRequestsMeter.mark();
		return deleteResourceTimer.time();
	}

	/**
	 * A generic create-method-timer creation method. Creates a timer for object creation of the given resource.
	 * 
	 * @param objectType the resource type
	 * @param clasz the resource class
	 * @return a new timing context (timer)
	 */
	public Timer.Context createNewObject(final String objectType, final Class<? extends BasicResource> clasz) {

		if (!createNewObjectTimers.containsKey(objectType)) {

			final Timer createNewObjectTimer = registry.timer(MetricRegistry.name(clasz, "post-requests", objectType, "create"));

			createNewObjectTimers.put(objectType, createNewObjectTimer);
		}

		allRequestsMeter.mark();
		return createNewObjectTimers.get(objectType).time();
	}

	public Timer.Context configurationsPreview() {
		allRequestsMeter.mark();
		return configurationsPreviewTimer.time();
	}

	public Timer.Context createNewConfiguration() {
		allRequestsMeter.mark();
		return createNewConfigurationsTimer.time();
	}

	public Timer.Context getAllResources() {
		allRequestsMeter.mark();
		return getAllResourcesTimer.time();
	}

	/**
	 * A generic retrieve-all-objects-method-timer creation method. Creates a timer for all objects retrieval of the given
	 * resource.
	 * 
	 * @param objectType the resource type
	 * @param clasz the resource class
	 * @return a new timing context (timer)
	 */
	public Timer.Context getAllObjects(final String objectType, final Class<? extends BasicResource> clasz) {

		if (!getAllObjectsTimers.containsKey(objectType)) {

			final Timer allObjectsTimer = registry.timer(MetricRegistry.name(clasz, "get-requests", objectType, "all"));

			getAllObjectsTimers.put(objectType, allObjectsTimer);
		}

		allRequestsMeter.mark();
		return getAllObjectsTimers.get(objectType).time();
	}

	public Timer.Context getSingleResource() {
		allRequestsMeter.mark();
		return getSingleResourcesTimer.time();
	}

	/**
	 * A generic retrieve-specific-object-method-timer creation method. Creates a timer for specific object retrieval of the given
	 * resource.
	 * 
	 * @param objectType the resource type
	 * @param clasz the resource class
	 * @return a new timing context (timer)
	 */
	public Timer.Context getSingleObject(final String objectType, final Class<? extends BasicResource> clasz) {

		if (!getObjectTimers.containsKey(objectType)) {

			final Timer getObjectTimer = registry.timer(MetricRegistry.name(clasz, "get-requests", objectType, "specific"));

			getObjectTimers.put(objectType, getObjectTimer);
		}

		allRequestsMeter.mark();
		return getObjectTimers.get(objectType).time();
	}

	/**
	 * A generic update-specific-object-method-timer creation method. Creates a timer for specific object update of the given
	 * resource.
	 * 
	 * @param objectType the resource type
	 * @param clasz the resource class
	 * @return a new timing context (timer)
	 */
	public Timer.Context updateSingleObject(final String objectType, final Class<? extends BasicResource> clasz) {

		if (!updateObjectTimers.containsKey(objectType)) {

			final Timer updateObjectTimer = registry.timer(MetricRegistry.name(clasz, "put-requests", objectType, "specific"));

			updateObjectTimers.put(objectType, updateObjectTimer);
		}

		allRequestsMeter.mark();
		return updateObjectTimers.get(objectType).time();
	}

	public Timer.Context getAllConfigurations() {
		allRequestsMeter.mark();
		return getAllConfigurationsTimer.time();
	}

	public Timer.Context getSingleConfiguration() {
		allRequestsMeter.mark();
		return getSingleConfigurationTimer.time();
	}

	public Timer.Context getConfigurationSchema() {
		allRequestsMeter.mark();
		return getConfigurationsSchemaTimer.time();
	}

	public Timer.Context getConfigurationData() {
		allRequestsMeter.mark();
		return getConfigurationsDataTimer.time();
	}

	/**
	 * A generic delete-specific-object-method-timer creation method. Creates a timer for specific object deletion of the given
	 * resource.
	 * 
	 * @param objectType the resource type
	 * @param clasz the resource class
	 * @return a new timing context (timer)
	 */
	public Timer.Context deleteObject(final String objectType, final Class<? extends BasicResource> clasz) {

		if (!deleteObjectTimers.containsKey(objectType)) {

			final Timer deleteObjectTimer = registry.timer(MetricRegistry.name(clasz, "delete-requests", objectType, "delete"));

			deleteObjectTimers.put(objectType, deleteObjectTimer);
		}

		allRequestsMeter.mark();
		return deleteObjectTimers.get(objectType).time();
	}

	public void stop(final Timer.Context context) {
		context.stop();
	}

	public long getUptime() {
		return ManagementFactory.getRuntimeMXBean().getUptime();
	}
}
