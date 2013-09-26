package de.avgl.dmp.persistence.model.utils;

import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.ResourceService;


public class ResourceReferenceDeserializer extends ReferenceDeserializer<Resource> {

	public ResourceReferenceDeserializer() {
		
		super(new ResourceService());
	}

}
