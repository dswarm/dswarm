package de.avgl.dmp.persistence.model.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.services.BasicJPAService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * 
 * @author tgaengler
 *
 * @param <DMPJPAOBJECTIMPL> the concrete model class
 */
public abstract class ReferenceDeserializer<DMPJPAOBJECTIMPL extends DMPJPAObject> extends JsonDeserializer<Set<DMPJPAOBJECTIMPL>> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ReferenceDeserializer.class);
	
	private BasicJPAService<DMPJPAOBJECTIMPL>	jpaService;

	public ReferenceDeserializer(final BasicJPAService<DMPJPAOBJECTIMPL> jpaServiceArg) {

		jpaService = jpaServiceArg;
	}

	@Override
	public Set<DMPJPAOBJECTIMPL> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {

		final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().readValue(jp, ArrayNode.class);

		if (arrayNode == null) {

			throw new IOException("couldn't parse JSON array");
		}

		if (arrayNode.size() <= 0) {

			return null;
		}

		final Set<DMPJPAOBJECTIMPL> set = Sets.newHashSet();

		final Iterator<JsonNode> arrayIter = arrayNode.iterator();

		while (arrayIter.hasNext()) {

			final JsonNode reference = arrayIter.next();
			
			if(reference == null) {
				
				LOG.debug("reference node is null");
				
				continue;
			}
			
			final JsonNode idNode = reference.get("id");
			
			if(idNode == null) {
				
				LOG.debug("id node is null");
			}
			
			final Long id = Long.valueOf(idNode.asLong());

			final DMPJPAOBJECTIMPL object = jpaService.getObject(id);
			
			if (object == null) {

				LOG.debug("couldn't find " + jpaService.getClasz().getSimpleName() + " with id '" + id + "'");
				
				continue;
			}
			
			set.add(object);
		}

		return set;
	}

}
