package de.avgl.dmp.persistence.services;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;

public interface InternalService {

	void createObject(Long resourceId, Long configurationId, Object model) throws DMPPersistenceException;

	Optional<Map<String, Model>> getObjects(Long resourceId, Long configurationId, Optional<Integer> atMost) throws DMPPersistenceException;

	void deleteObject(Long resourceId, Long configurationId) throws DMPPersistenceException;

	Optional<Set<String>> getSchema(Long resourceId, Long configurationId);
}
