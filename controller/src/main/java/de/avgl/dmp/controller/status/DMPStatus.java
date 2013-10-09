package de.avgl.dmp.controller.status;

import java.lang.management.ManagementFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.controller.resources.ResourcesResource;

import static com.codahale.metrics.MetricRegistry.name;

@Singleton
public class DMPStatus {

	private final Meter allRequestsMeter;

	private final Timer createNewResourceTimer;
	private final Timer configurationsPreviewTimer;
	private final Timer createNewConfigurationsTimer;

	private final Timer getAllResourcesTimer;
	private final Timer getSingleResourcesTimer;
	private final Timer getAllConfigurationsTimer;
	private final Timer getSingleConfigurationTimer;
	private final Timer getConfigurationsSchemaTimer;
	private final Timer getConfigurationsDataTimer;


	@Inject
	public DMPStatus(final MetricRegistry registry) {
		allRequestsMeter = registry.meter(name(ResourcesResource.class, "requests", "all"));

		createNewResourceTimer = registry.timer(name(ResourcesResource.class, "post-requests", "resources", "create"));
		configurationsPreviewTimer = registry.timer(name(ResourcesResource.class, "post-requests", "configurations", "preview"));
		createNewConfigurationsTimer = registry.timer(name(ResourcesResource.class, "post-requests", "configurations", "create"));

		getAllResourcesTimer = registry.timer(name(ResourcesResource.class, "get-requests", "resources", "all"));
		getSingleResourcesTimer = registry.timer(name(ResourcesResource.class, "get-requests", "resources", "specific"));
		getAllConfigurationsTimer = registry.timer(name(ResourcesResource.class, "get-requests", "configurations", "all"));
		getSingleConfigurationTimer = registry.timer(name(ResourcesResource.class, "get-requests", "configurations", "specific"));
		getConfigurationsSchemaTimer = registry.timer(name(ResourcesResource.class, "get-requests", "configurations", "schema"));
		getConfigurationsDataTimer = registry.timer(name(ResourcesResource.class, "get-requests", "configurations", "data"));
	}

	public Timer.Context createNewResource() {
		allRequestsMeter.mark();
		return createNewResourceTimer.time();
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

	public Timer.Context getSingleResource() {
		allRequestsMeter.mark();
		return getSingleResourcesTimer.time();
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


	public long stop(final Timer.Context context) {
		return context.stop();
	}

	public long getUptime() {
		return ManagementFactory.getRuntimeMXBean().getUptime();
	}
}
