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
import java.util.Set;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Sets;

import org.dswarm.persistence.model.DMPObject;

abstract class SetReferenceSerializer<DMPOBJECTIMPL extends DMPObject> extends JsonSerializer<Set<DMPOBJECTIMPL>> {

	@Override
	public void serialize(final Set<DMPOBJECTIMPL> objects, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

		if (objects == null || objects.isEmpty()) {

			generator.writeNull();

			return;
		}

		final Set<Reference> references = Sets.newHashSet();

		for (final DMPOBJECTIMPL object : objects) {

			references.add(new Reference(object.getUuid()));
		}

		generator.writeObject(references);
	}

	@XmlRootElement
	static class Reference {

		@XmlID
		private final String uuid;

		Reference(final String uuidArg) {

			uuid = uuidArg;
		}

		String getUuid() {

			return uuid;
		}
	}

}
