package de.avgl.dmp.persistence.services;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

public interface InternalService {

	void createObject(Long id, Long id1, String subject, String predicate, String object);

	Optional<Map<String, Map<String, String>>> getObjects(Long id, Long configurationId, Optional<Integer> atMost);

	void deleteObject(Long id, Long configurationId);

	Optional<Set<String>> getSchema(Long id, Long configurationId);
}
