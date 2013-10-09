package de.avgl.dmp.persistence.services.impl;

import com.google.common.base.Optional;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.model.jsonschema.JSRoot;
import de.avgl.dmp.persistence.services.SchemaService;


@Singleton
public class SchemaServiceImpl extends BaseMemoryServiceImpl<Long, Long, JSRoot> implements SchemaService {

	@Override
	public Optional<JSRoot> getSchema(Long id, Long configurationId) {
		return getObjects(id, configurationId);
	}
}
