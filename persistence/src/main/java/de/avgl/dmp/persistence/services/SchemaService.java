package de.avgl.dmp.persistence.services;


import com.google.common.base.Optional;

import de.avgl.dmp.persistence.model.jsonschema.JSRoot;


public interface SchemaService {

	void createObject(Long id, Long id1, JSRoot schema);

	Optional<JSRoot> getObjects(Long id, Long configurationId);

	void deleteObject(Long id, Long configurationId);

	Optional<JSRoot> getSchema(Long id, Long configurationId);
}
