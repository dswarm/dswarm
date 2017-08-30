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
package org.dswarm.persistence.model.representation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;

class BeanReferenceDeserializer extends BeanDeserializer {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Constructor used by {@link BeanDeserializerBuilder}.
	 */
	public BeanReferenceDeserializer(final BeanDeserializerBuilder builder, final BeanDescription beanDesc, final BeanPropertyMap properties,
			final Map<String, SettableBeanProperty> backRefs, final HashSet<String> ignorableProps, final boolean ignoreAllUnknown,
			final boolean hasViews) {

		super(builder, beanDesc, properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
	}

	protected BeanReferenceDeserializer(final BeanDeserializerBase src) {

		super(src);
	}

	@Override
	public Object deserialize(final JsonParser jp, final DeserializationContext ctxt, final Object bean) throws IOException {
		// TODO Auto-generated method stub
		return super.deserialize(jp, ctxt, bean);
	}
}
