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

import de.avgl.dmp.persistence.model.DMPJPAObject;

public abstract class ReferenceSerializer<DMPJPAOBJECTIMPL extends DMPJPAObject> extends JsonSerializer<Set<DMPJPAOBJECTIMPL>> {

	@Override
	public void serialize(final Set<DMPJPAOBJECTIMPL> objects, final JsonGenerator generator, final SerializerProvider provider) throws IOException,
			JsonProcessingException {
		
		if(objects == null || objects.isEmpty()) {
			
			generator.writeObject(null);
		}

		final Set<Reference> references = Sets.newHashSet();

		for (final DMPJPAOBJECTIMPL object : objects) {

			references.add(new Reference(object.getId()));
		}

		generator.writeObject(references);
	}

	@XmlRootElement
	static class Reference {

		@XmlID
		private Long	id;

		Reference(final Long idArg) {

			id = idArg;
		}

		Long getId() {

			return id;
		}
	}

}
