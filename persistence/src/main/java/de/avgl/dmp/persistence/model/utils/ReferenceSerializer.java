package de.avgl.dmp.persistence.model.utils;

import java.io.IOException;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import de.avgl.dmp.persistence.model.job.DMPObject;

public abstract class ReferenceSerializer<DMPOBJECTIMPL extends DMPObject<DMPOBJECTIDTYPE>, DMPOBJECTIDTYPE> extends
		JsonSerializer<DMPOBJECTIMPL> {

	@Override
	public void serialize(final DMPOBJECTIMPL object, final JsonGenerator generator, final SerializerProvider provider) throws IOException,
			JsonProcessingException {

		if (object == null) {

			generator.writeNull();

			return;
		}

		final Reference<DMPOBJECTIDTYPE> reference = new Reference<DMPOBJECTIDTYPE>(object.getId());

		generator.writeObject(reference);
	}

	@XmlRootElement
	static class Reference<IDTYPE> {

		@XmlID
		private final IDTYPE	id;

		Reference(final IDTYPE idArg) {

			id = idArg;
		}

		IDTYPE getId() {

			return id;
		}
	}

}
