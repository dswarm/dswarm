package de.avgl.dmp.persistence.model.utils;

import java.io.IOException;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import de.avgl.dmp.persistence.model.DMPUUIDObject;

public class DMPUUIDObjectReferenceSerializer

// extends ReferenceSerializer<DMPUUIDObject, String> {
 extends JsonSerializer<DMPUUIDObject> {

	@Override
	public void serialize(final DMPUUIDObject object, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

		if (object == null) {

			generator.writeNull();

			return;
		}

		final Reference reference = new Reference(object.getId());

		generator.writeObject(reference);
	}

	@XmlRootElement
	static class Reference {

		@XmlID
		private final String	id;

		Reference(final String idArg) {

			id = idArg;
		}

		String getId() {

			return id;
		}
	}

}
