/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.flow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.common.base.Preconditions;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * @author phorn
 */
public final class CSVResourceFlowFactory {

	public static <T, U extends AbstractCSVResourceFlow<T>> U fromConfiguration(final Configuration configuration, final Class<U> clazz)
			throws DMPConverterException {

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

		return Preconditions.checkNotNull(flow, "something went wrong while apply configuration to resource");
	}

	public static <T, U extends AbstractCSVResourceFlow<T>> U fromDataModel(final DataModel dataModel, final Class<U> clazz)
			throws DMPConverterException {

		final Constructor<U> constructor;
		try {
			constructor = clazz.getConstructor(DataModel.class);
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
			throw new DMPConverterException("no DataModel constructor for class " + clazz.getSimpleName());
		}

		final U flow;
		try {
			flow = constructor.newInstance(dataModel);
		} catch (final InstantiationException e) {
			e.printStackTrace();
			throw new DMPConverterException("Error while instantiating DataModel constructor class " + clazz.getSimpleName());
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
			throw new DMPConverterException("Error while accessing DataModel constructor for class " + clazz.getSimpleName());
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
			throw new DMPConverterException(e.getCause().getMessage());
		}

		return Preconditions.checkNotNull(flow, "something went wrong while apply data model to resource");
	}

	public static <T, U extends AbstractCSVResourceFlow<T>> U fromConfigurationParameters(final String encoding, final Character escapeCharacter,
			final Character quoteCharacter, final Character columnDelimiter, final String rowDelimiter, final Class<U> clazz)
			throws DMPConverterException {

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

		return Preconditions.checkNotNull(flow, "something went wrong while apply configuration to resource");
	}
}
