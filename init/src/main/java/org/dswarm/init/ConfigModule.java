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
package org.dswarm.init;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.impl.Parseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ConfigModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigModule.class);
	private static final String OLD_LOG_CONFIG_ON_START = "dswarm.log-config-on-start";
	private static final String LOG_CONFIG_ON_START = "dswarm.logging.log-config-on-start";

	private static final AtomicReference<Optional<Config>> LOADED_CONFIG = new AtomicReference<>(Optional.empty());
	private static final TypeLiteral<List<Integer>> INT_LIST = new TypeLiteral<List<Integer>>() {
	};
	private static final TypeLiteral<List<Long>> LONG_LIST = new TypeLiteral<List<Long>>() {
	};
	private static final TypeLiteral<List<Double>> DOUBLE_LIST = new TypeLiteral<List<Double>>() {
	};
	private static final TypeLiteral<List<Boolean>> BOOL_LIST = new TypeLiteral<List<Boolean>>() {
	};
	private static final TypeLiteral<List<String>> STRING_LIST = new TypeLiteral<List<String>>() {
	};
	private final Config config;

	public ConfigModule() {
		config = loadConfig();
	}

	public static Config loadConfig() {
		Optional<Config> prev, next;
		do {
			prev = LOADED_CONFIG.get();
			if (prev.isPresent()) {
				return prev.get();
			}
			next = Optional.of(ConfigFactoryWithOffloading.loadConfig());
		} while (!LOADED_CONFIG.compareAndSet(prev, next));
		return LOADED_CONFIG.get().get();
	}

	static boolean hasEnabled(final Config config, final String... paths) {
		return Arrays.stream(paths)
				.filter(config::hasPath)
				.map(config::getBoolean)
				.findFirst()
				.orElse(false);
	}

	static Optional<String> firstString(final Config config, final String... paths) {
		return Arrays.stream(paths)
				.filter(config::hasPath)
				.map(config::getString)
				.findFirst();
	}

	private static <T> List<T> collectFrom(final Collection<Object> objects, final Class<T> cls) {
		final List<T> values = new ArrayList<>(objects.size());
		values.addAll(objects.stream()
						.filter(o -> o != null && cls.isInstance(o))
						.map(cls::cast)
						.collect(Collectors.toList())
		);
		return values;
	}

	private static RuntimeException unexpectedConfigType(final ConfigValue configValue) {
		final String msg = String.format(
				"Did not expect a value of type [%s] at [%s]", configValue.valueType(), configValue.origin());
		return new IllegalArgumentException(msg);
	}

	private static RuntimeException unexpectedListType(final Object head, final ConfigValue configValue) {
		final String msg = String.format(
				"Unsupported list type: [%s] at [%s]", head.getClass(), configValue.origin());
		return new IllegalArgumentException(msg);
	}

	public Config getConfig() {
		return config;
	}

	@Override
	protected void configure() {
		if (LOG.isInfoEnabled() && hasEnabled(config, LOG_CONFIG_ON_START, OLD_LOG_CONFIG_ON_START)) {
			LOG.info(config.root().render());
		}

		bind(Config.class).toInstance(config);
		bindConfig();
	}

	private void bindPrimitive(final Named key, final Boolean value) {
		bindConstant().annotatedWith(key).to(value);
		LOG.trace("bound {} to {}", key.value(), value);
	}

	private void bindPrimitive(final Named key, final String value) {
		bindConstant().annotatedWith(key).to(value);
		LOG.trace("bound {} to {}", key.value(), value);
	}

	private void bindPrimitive(final Named key, final long value) {
		bindConstant().annotatedWith(key).to(value);
		LOG.trace("bound {} to {}", key.value(), value);
	}

	private <T> void bindList(final Named key, final TypeLiteral<List<T>> typeLiteral, final List<T> values) {
		bind(typeLiteral).annotatedWith(key).toInstance(values);
		LOG.trace("bound {} to {}", key.value(), values);
	}

	private void bindList(final Named key, final List<Object> values, final ConfigValue configValue) {
		if (values.isEmpty()) {
			bindList(key, INT_LIST, Collections.<Integer>emptyList());
			bindList(key, DOUBLE_LIST, Collections.<Double>emptyList());
			bindList(key, BOOL_LIST, Collections.<Boolean>emptyList());
			bindList(key, STRING_LIST, Collections.<String>emptyList());
		} else {
			final Object head = values.get(0);
			if (head instanceof Integer) {
				bindList(key, INT_LIST, collectFrom(values, Integer.class));
			} else if (head instanceof Long) {
				bindList(key, LONG_LIST, collectFrom(values, Long.class));
			} else if (head instanceof Double) {
				bindList(key, DOUBLE_LIST, collectFrom(values, Double.class));
			} else if (head instanceof Boolean) {
				bindList(key, BOOL_LIST, collectFrom(values, Boolean.class));
			} else if (head instanceof String) {
				bindList(key, STRING_LIST, collectFrom(values, String.class));
			} else {
				throw unexpectedListType(head, configValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void bindConfig() {
		for (final Map.Entry<String, ConfigValue> entry : config.entrySet()) {
			final ConfigValue configValue = entry.getValue();
			final Named key = Names.named(entry.getKey());
			switch (configValue.valueType()) {
				case BOOLEAN:
					bindPrimitive(key, (Boolean) configValue.unwrapped());
					break;

				case NUMBER:
					bindPrimitive(key, ((Number) configValue.unwrapped()).longValue());
					break;

				case STRING:
					final String configKey = entry.getKey();
					final boolean specialValue =
							tryParseDuration(configKey, key) ||
									tryParseBoolean(configKey, key);
					if (!specialValue) {
						bindPrimitive(key, configValue.unwrapped().toString());
					}
					break;

				case LIST:
					bindList(key, (List<Object>) configValue.unwrapped(), configValue);
					break;

				case OBJECT:
				case NULL:

				default:
					throw unexpectedConfigType(configValue);
			}
		}
	}

	private boolean tryParseDuration(final String configKey, final Named bindKey) {
		try {
			final long duration = config.getDuration(configKey, TimeUnit.MILLISECONDS);
			bindPrimitive(bindKey, duration);
			return true;
		} catch (final ConfigException.BadValue | ConfigException.WrongType ignore) {
			return false;
		}
	}

	private boolean tryParseBoolean(final String configKey, final Named bindKey) {
		try {
			final boolean duration = config.getBoolean(configKey);
			bindPrimitive(bindKey, duration);
			return true;
		} catch (final ConfigException.BadValue | ConfigException.WrongType ignore) {
			return false;
		}
	}

	private static class ConfigFactoryWithOffloading {

		private static Config jndiConfig() {
			final String configFileFromJndi;
			try {
				configFileFromJndi = InitialContext.doLookup("java:comp/env/configFile");
			} catch (final NamingException e) {
				LOG.info("JNDI configFile is not set, skipping");
				return ConfigFactory.empty();
			}
			try {
				return ConfigFactory.parseFile(new File(configFileFromJndi));
			} catch (final ConfigException e) {
				LOG.warn(String.format("Could not load config [%s] from JNDI", configFileFromJndi), e);
			}
			return ConfigFactory.empty();
		}

		private static Config defaultConfig(final Config jndiConfig) {
			return jndiConfig.withFallback(ConfigFactory.load());
		}

		private static Config unresolvedConfig(final String configFileName) {
			return Parseable
					.newResources(configFileName, ConfigParseOptions.defaults())
					.parse().toConfig();
		}

		private static Config mergedConfig(final Config defaultConf, final Config unresolvedConf) {
			return unresolvedConf
					.resolveWith(defaultConf)
					.withFallback(defaultConf);
		}

		private static Config loadConfig() {
			return mergedConfig(
					defaultConfig(jndiConfig()),
					unresolvedConfig("application.conf"));
		}
	}
}
