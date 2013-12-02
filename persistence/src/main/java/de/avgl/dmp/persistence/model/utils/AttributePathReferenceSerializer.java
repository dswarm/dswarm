package de.avgl.dmp.persistence.model.utils;

import java.io.IOException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import de.avgl.dmp.persistence.model.job.AttributePath;

public class AttributePathReferenceSerializer extends JsonSerializer<AttributePath> {

	@Override
	public void serialize(final AttributePath object, final JsonGenerator generator, final SerializerProvider provider) throws IOException,
			JsonProcessingException {

		if (object == null) {

			generator.writeNull();

			return;
		}
		
		System.out.println("attribute path = '" + object.toAttributePath() + "'");

		final AttributePathReference reference = new AttributePathReference(object.getId(), object.toAttributePath());

		generator.writeObject(reference);
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
