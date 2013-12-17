package de.avgl.dmp.persistence.model.utils;

import java.io.IOException;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.schema.AttributePath;

public class SetAttributePathReferenceSerializer extends JsonSerializer<Set<AttributePath>> {

	@Override
	public void serialize(final Set<AttributePath> objects, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

		if (objects == null || objects.isEmpty()) {

			generator.writeNull();

			return;
		}

		final Set<AttributePathReference> references = Sets.newHashSet();

		for (final AttributePath object : objects) {

			System.out.println("attribute path = '" + object.toAttributePath() + "'");

			references.add(new AttributePathReference(object.getId(), object.toAttributePath()));
		}

		generator.writeObject(references);
	}

	@XmlRootElement
	static class AttributePathReference {

		@XmlID
		private final Long	id;

		@XmlElement(name = "attribute_path")
		private final String attributePath;

		AttributePathReference(final Long idArg, final String attributePathArg) {

			id = idArg;
			attributePath = attributePathArg;
		}

		Long getId() {

			return id;
		}

		String getAttributePath() {

			return attributePath;
		}
	}
}
