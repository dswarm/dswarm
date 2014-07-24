package org.dswarm.init;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
	private static final String LOG_CONFIG_ON_START = "dswarm.log-config-on-start";

	@Override
	protected void configure() {
		final Config config = ConfigFactoryWithOffloading.loadConfig();
		if (LOG.isInfoEnabled() && config.hasPath(LOG_CONFIG_ON_START) && config.getBoolean(LOG_CONFIG_ON_START)) {
			LOG.info(config.root().render());
		}

		bind(Config.class).toInstance(config);
		bindConfig(config);
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

	private <T> List<T> collectFrom(final List<Object> objects, final Class<T> cls) {
		final List<T> values = new ArrayList<>(objects.size());
		for (final Object o : objects) {
			if (o != null && cls.isInstance(o)) {
				values.add(cls.cast(o));
			}
		}
		return values;
	}

	private <T> void bindList(final Named key, final TypeLiteral<List<T>> typeLiteral, final List<T> values) {
		bind(typeLiteral).annotatedWith(key).toInstance(values);
		LOG.trace("bound {} to {}", key.value(), values.toString());
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
			}
			else if (head instanceof Long) {
				bindList(key, LONG_LIST, collectFrom(values, Long.class));
			}
			else if (head instanceof Double) {
				bindList(key, DOUBLE_LIST, collectFrom(values, Double.class));
			}
			else if (head instanceof Boolean) {
				bindList(key, BOOL_LIST, collectFrom(values, Boolean.class));
			}
			else if (head instanceof String) {
				bindList(key, STRING_LIST, collectFrom(values, String.class));
			}
			else {
				throw unexpectedListType(head, configValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void bindConfig(final Config config) {
		for (final Map.Entry<String, ConfigValue> entry : config.entrySet()) {
			final ConfigValue configValue = entry.getValue();
			final Named key = Names.named(entry.getKey());
			switch (configValue.valueType()) {
				case BOOLEAN:
					bindPrimitive(key, ((Boolean) configValue.unwrapped()));
					break;

				case NUMBER:
					bindPrimitive(key, ((Number) configValue.unwrapped()).longValue());
					break;

				case STRING:
					try {
						final long duration = config.getDuration(entry.getKey(), TimeUnit.MILLISECONDS);
						bindPrimitive(key, duration);
					} catch (final ConfigException.BadValue ignore) {
						// BadValue is thrown when the string could not be parsed as a duration,
						// but that's OK, just use it as a regular string then.
						bindPrimitive(key, configValue.unwrapped().toString());
					}
					break;

				case LIST:
					bindList(key, ((List<Object>) configValue.unwrapped()), configValue);
					break;

				case OBJECT:
				case NULL:

				default:
					throw unexpectedConfigType(configValue);
			}
		}
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

	private static final TypeLiteral<List<Integer>> INT_LIST = new TypeLiteral<List<Integer>>() {};
	private static final TypeLiteral<List<Long>> LONG_LIST = new TypeLiteral<List<Long>>() {};
	private static final TypeLiteral<List<Double>> DOUBLE_LIST = new TypeLiteral<List<Double>>() {};
	private static final TypeLiteral<List<Boolean>> BOOL_LIST = new TypeLiteral<List<Boolean>>() {};
	private static final TypeLiteral<List<String>> STRING_LIST = new TypeLiteral<List<String>>() {};

	private static class ConfigFactoryWithOffloading {

		private static Config jdniConfig() {
			final String configFileFromJdni;
			try {
				configFileFromJdni = InitialContext.doLookup("java:comp/env/configFile");
			} catch (final NamingException e) {
				LOG.info("JDNI configFile is not set, skipping");
				return ConfigFactory.empty();
			}
			try {
				return ConfigFactory.parseFile(new File(configFileFromJdni));
			} catch (final ConfigException e ) {
				LOG.warn(String.format("Could not load config [%s] from JDNI", configFileFromJdni), e);
			}
			return ConfigFactory.empty();
		}

		private static Config defaultConfig(final Config jdniConfig) {
			return jdniConfig.withFallback(ConfigFactory.load());
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
					defaultConfig(jdniConfig()),
					unresolvedConfig("application.conf"));
		}
	}
}
