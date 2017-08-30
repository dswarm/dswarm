/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.Provider;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.Producing;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEvent.Type;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import static com.codahale.metrics.MetricRegistry.name;

@Provider
public class InstrumentedMetricsEventListener implements ApplicationEventListener {

	private final MetricRegistry metrics;
	private final AtomicReference<RequestEventListener> listener = new AtomicReference<>();

	/**
	 * Construct an application event listener using the given metrics registry.
	 * <p/>
	 * <p/>
	 * When using this constructor, the {@link InstrumentedMetricsEventListener}
	 * should be added to a Jersey {@code ResourceConfig} as a singleton.
	 *
	 * @param metrics a {@link MetricRegistry}
	 */
	@Inject
	public InstrumentedMetricsEventListener(final MetricRegistry metrics) {
		this.metrics = metrics;
	}

	@SuppressWarnings("TypeMayBeWeakened")
	private static class GenericRequestEventListener implements RequestEventListener {

		private static final String METHOD_PREFIX = "httpMethods";
		private static final String STATUS_CODE_PREFIX = "responseCodes";
		private static final String STATUS_FAMILY_PREFIX = "responseFamilies";

		private final MetricRegistry registry;
		private final Timer requestTimer;
		private final Counter activeRequests;

		private Context requestTimerContext;

		private GenericRequestEventListener(final MetricRegistry registry) {
			this.registry = registry;
			activeRequests = registry.counter(name("active_requests"));
			requestTimer = registry.timer(name("all_requests"));

			registerMeters();
		}

		private static void markMethod(final String method, final MetricRegistry registry) {
			registry.meter(name(METHOD_PREFIX, method)).mark();
		}

		private static void markStatus(final StatusType status, final MetricRegistry registry) {
			if (status != null) {
				registry.meter(name(STATUS_CODE_PREFIX, status.getReasonPhrase())).mark();
				registry.meter(name(STATUS_FAMILY_PREFIX, status.getFamily().name())).mark();
			}
		}

		private void registerMeters() {
			registerStatuses();
			registerMethods();
		}

		private void registerMethods() {
			final String[] methods = {
					HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE,
					HttpMethod.HEAD, HttpMethod.OPTIONS};
			for (final String method : methods) {
				registry.meter(name(METHOD_PREFIX, method));
			}
		}

		private void registerStatuses() {
			for (final Status status : Status.values()) {
				registry.meter(name(STATUS_CODE_PREFIX, status.getReasonPhrase()));
				registry.meter(name(STATUS_FAMILY_PREFIX, status.getFamily().name()));
			}
		}

		@Override
		public void onEvent(final RequestEvent event) {
			if (event.getType() == RequestEvent.Type.MATCHING_START) {
				activeRequests.inc();
				requestTimerContext = requestTimer.time();

				markMethod(event.getContainerRequest().getMethod(), registry);

			} else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
				@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
				final Throwable cause = event.getException().getCause();
				if (cause != null) {
					registry.meter(name("exceptions", cause.getClass().getName())).mark();
				}

			} else if (event.getType() == RequestEvent.Type.FINISHED) {
				activeRequests.dec();
				if (requestTimerContext != null) {
					requestTimerContext.close();
					requestTimerContext = null;
				}

				if (event.getContainerResponse() != null) {
					markStatus(event.getContainerResponse().getStatusInfo(), registry);
				}
			}
		}

	}

	private static class TimerRequestEventListener implements RequestEventListener {
		private final ImmutableMap<Method, Timer> timers;
		private final Map<ContainerRequest, Context> contexts;

		public TimerRequestEventListener(final ImmutableMap<Method, Timer> timers) {
			this.timers = timers;
			contexts = Maps.newHashMap();
		}

		@Override
		public void onEvent(final RequestEvent event) {
			final ContainerRequest containerRequest = event.getContainerRequest();
			if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
				getDefinitionMethod(event)
						.flatMap(m -> Optional.ofNullable(timers.get(m)))
						.ifPresent(timer -> {
							final Context context = timer.time();
							contexts.put(containerRequest, context);
						});
			} else if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED) {
				Optional.ofNullable(contexts.get(containerRequest))
						.ifPresent(context -> {
							context.close();
							contexts.remove(containerRequest, context);
						});
			} else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
				Optional.ofNullable(contexts.get(containerRequest))
						.ifPresent(context -> {
							context.close();
							contexts.remove(containerRequest, context);
						});
			}
		}
	}

	private static class MeterRequestEventListener implements RequestEventListener {
		private final ImmutableMap<Method, Meter> meters;

		public MeterRequestEventListener(final ImmutableMap<Method, Meter> meters) {
			this.meters = meters;
		}

		@Override
		public void onEvent(final RequestEvent event) {
			if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
				getDefinitionMethod(event)
						.flatMap(m -> Optional.ofNullable(meters.get(m)))
						.ifPresent(Meter::mark);
			}
		}
	}

	private static Optional<Method> getDefinitionMethod(final RequestEvent event) {
		return Optional.ofNullable(event.getUriInfo())
				.map(ExtendedUriInfo::getMatchedResourceMethod)
				.map(ResourceMethod::getInvocable)
				.map(Invocable::getDefinitionMethod);
	}

	private static class ChainedRequestEventListener implements RequestEventListener {
		private final RequestEventListener[] listeners;

		private ChainedRequestEventListener(final RequestEventListener... listeners) {
			this.listeners = listeners;
		}

		@Override
		public void onEvent(final RequestEvent event) {
			for (final RequestEventListener listener : listeners) {
				listener.onEvent(event);
			}
		}
	}

	@Override
	public void onEvent(final ApplicationEvent event) {
		if (event.getType() == Type.INITIALIZATION_APP_FINISHED) {
			final Builder<Method, Timer> timerBuilder = ImmutableMap.<Method, Timer>builder();
			final Builder<Method, Meter> meterBuilder = ImmutableMap.<Method, Meter>builder();

			for (final Resource resource : event.getResourceModel().getResources()) {
				for (final ResourceMethod method : resource.getAllMethods()) {
					registerTimedAnnotations(timerBuilder, method);
					registerMeteredAnnotations(meterBuilder, method);
				}

				for (final Resource childResource : resource.getChildResources()) {
					for (final ResourceMethod method : childResource.getAllMethods()) {
						registerTimedAnnotations(timerBuilder, method);
						registerMeteredAnnotations(meterBuilder, method);
					}
				}
			}

			final ImmutableMap<Method, Timer> timers = timerBuilder.build();
			final ImmutableMap<Method, Meter> meters = meterBuilder.build();

			listener.set(new ChainedRequestEventListener(
					new TimerRequestEventListener(timers),
					new MeterRequestEventListener(meters),
					new GenericRequestEventListener(metrics)));
		}
	}

	@Override
	public RequestEventListener onRequest(final RequestEvent event) {
		return listener.get();
	}

	private void registerTimedAnnotations(final Builder<Method, Timer> builder,
	                                      final ResourceMethod method) {
		registerMetricsAnnotations(builder, method, Timed.class, metrics::timer);
	}

	private void registerMeteredAnnotations(final Builder<Method, Meter> builder,
	                                        final ResourceMethod method) {
		registerMetricsAnnotations(builder, method, Metered.class, metrics::meter);
	}

	private static <M extends Metric, A extends Annotation> void registerMetricsAnnotations(
			final Builder<Method, M> builder,
			final ResourceMethod method,
			final Class<A> annotationClass,
			final Function<String, M> creator) {
		final Method definitionMethod = method.getInvocable().getDefinitionMethod();
		final boolean isAnnotated = isAnnotatedWith(annotationClass, definitionMethod);
		if (isAnnotated) {
			builder.put(definitionMethod, creator.apply(getMetricsName(method)));
		}
	}

	private static <A extends Annotation> boolean isAnnotatedWith(final Class<A> annotationClass, final Method bottomDefinitionMethod) {
		Method definitionMethod = bottomDefinitionMethod;
		while (true) {
			if (definitionMethod.getAnnotation(annotationClass) != null) {
				return true;
			}

			final Class<?> superclass = definitionMethod.getDeclaringClass().getSuperclass();
			if (superclass == null) { // reached top
				return false;
			}

			try {
				definitionMethod = superclass.getMethod(bottomDefinitionMethod.getName(), bottomDefinitionMethod.getParameterTypes());
			} catch (final NoSuchMethodException ignored) { // not defined in super
				return false;
			}
		}
	}

	private static String getMetricsName(final ResourceMethod method) {
		final StringBuilder nameBuilder = new StringBuilder();
		addHttpMethod(method, nameBuilder);
		addPathNames(method, nameBuilder);
		addProducedTypes(method, nameBuilder);
		return nameBuilder.toString();
	}

	private static void addHttpMethod(final ResourceMethod method, final StringBuilder nameBuilder) {
		nameBuilder.append(method.getHttpMethod()).append(" /");
	}

	private static void addPathNames(final ResourceMethod method, final StringBuilder nameBuilder) {
		final List<String> pathElements = Lists.newArrayList();
		Resource parent = method.getParent();
		while (parent != null) {
			pathElements.add(parent.getPath());
			parent = parent.getParent();
		}
		Collections.reverse(pathElements);
		pathElements.forEach(nameBuilder::append);
	}

	private static void addProducedTypes(final Producing method, final StringBuilder nameBuilder) {
		final List<MediaType> producedTypes = method.getProducedTypes();
		if (producedTypes.isEmpty()) {
			return;
		}
		nameBuilder.append(" (");
		producedTypes.stream().map(MediaType::getSubtype).map(String::toUpperCase).forEach(nameBuilder::append);
		nameBuilder.append(")");
	}
}
