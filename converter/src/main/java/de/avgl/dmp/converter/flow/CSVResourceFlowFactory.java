package de.avgl.dmp.converter.flow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.persistence.model.resource.Configuration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CSVResourceFlowFactory {

	public static <T, U extends AbstractCSVResourceFlow<T>> U fromConfiguration(
			final Configuration configuration,
			final Class<U> clazz) throws DMPConverterException {

		final Constructor<U> constructor;
		try {
			constructor = clazz.getConstructor(Configuration.class);
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			throw new DMPConverterException("no Configuration constructor for class " + clazz.getSimpleName());
		}

		final U flow;
		try {
			flow = constructor.newInstance(configuration);
		} catch (final InstantiationException e) {
			e.printStackTrace();
			throw new DMPConverterException("Error while instantiating Configuration constructor class " + clazz.getSimpleName());
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
			throw new DMPConverterException("Error while accessing Configuration constructor for class " + clazz.getSimpleName());
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
			throw new DMPConverterException(e.getCause().getMessage());
		}

		return checkNotNull(flow, "something went wrong while apply configuration to resource");
	}
	public static <T, U extends AbstractCSVResourceFlow<T>> U fromConfigurationParameters(
			final String encoding, final Character escapeCharacter,
			final Character quoteCharacter, final Character columnDelimiter,
			final String rowDelimiter,
			final Class<U> clazz) throws DMPConverterException {


		final Constructor<U> constructor;
		try {
			constructor = clazz.getConstructor(String.class, Character.class, Character.class, Character.class, String.class);
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			throw new DMPConverterException("no Configuration constructor for class " + clazz.getSimpleName());
		}


		final U flow;
		try {
			flow = constructor.newInstance(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
		} catch (final InstantiationException e) {
			e.printStackTrace();
			throw new DMPConverterException("Error while instantiating Configuration constructor class " + clazz.getSimpleName());
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
			throw new DMPConverterException("Error while accessing Configuration constructor for class " + clazz.getSimpleName());
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
			throw new DMPConverterException(e.getCause().getMessage());
		}

		return checkNotNull(flow, "something went wrong while apply configuration to resource");
	}
}
