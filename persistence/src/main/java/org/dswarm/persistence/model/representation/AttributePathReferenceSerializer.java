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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.dswarm.persistence.model.schema.AttributePath;

public class AttributePathReferenceSerializer extends JsonSerializer<AttributePath> {

	@Override
	public void serialize(final AttributePath object, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

		if (object == null) {

			generator.writeNull();

			return;
		}

		final AttributePathReference reference = new AttributePathReference(object.getUuid(), object.toAttributePath());

		generator.writeObject(reference);
	}

	@XmlRootElement
	static class AttributePathReference {

		@XmlID
		private final String id;

		@XmlElement(name = "attribute_path")
		private final String attributePath;

		AttributePathReference(final String idArg, final String attributePathArg) {

			id = idArg;
			attributePath = attributePathArg;
		}

		String getId() {

			return id;
		}

		String getAttributePath() {

			return attributePath;
		}
	}
}
