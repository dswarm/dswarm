package de.avgl.dmp.persistence.model.utils;

import java.io.IOException;
import java.util.Set;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPUUIDObject;

public abstract class SetDMPUUIDObjectReferenceSerializer<DMPOBJECTIMPL extends DMPUUIDObject> extends JsonSerializer<Set<DMPOBJECTIMPL>> {

	@Override
	public void serialize(final Set<DMPOBJECTIMPL> objects, final JsonGenerator generator, final SerializerProvider provider) throws IOException,
			JsonProcessingException {

		if (objects == null || objects.isEmpty()) {

			generator.writeNull();

			return;
		}

		final Set<Reference> references = Sets.newHashSet();

		for (final DMPOBJECTIMPL object : objects) {

			references.add(new Reference(object.getId()));
		}

		generator.writeObject(references);
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
