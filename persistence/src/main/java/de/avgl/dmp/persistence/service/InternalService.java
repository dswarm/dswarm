package de.avgl.dmp.persistence.service;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.schema.Schema;

public interface InternalService {

	@Deprecated
	void createObject(Long resourceId, Long configurationId, Object model) throws DMPPersistenceException;

	@Deprecated
	Optional<Map<String, Model>> getObjects(Long resourceId, Long configurationId, Optional<Integer> atMost) throws DMPPersistenceException;

	@Deprecated
	void deleteObject(Long resourceId, Long configurationId) throws DMPPersistenceException;

	@Deprecated
	Optional<Set<String>> getSchema(Long resourceId, Long configurationId);

	void createObject(Long dataModelId, Object model) throws DMPPersistenceException;

	Optional<Map<String, Model>> getObjects(Long dataModelId, Optional<Integer> atMost) throws DMPPersistenceException;

	void deleteObject(Long dataModelId) throws DMPPersistenceException;

	Optional<Schema> getSchema(Long dataModelId) throws DMPPersistenceException;
}
