package de.avgl.dmp.persistence.services.impl;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.services.InternalService;

@Singleton
public class InternalTripleService implements InternalService {

	@Override
	public void createObject(final Long id, final Long id1, final String subject, final String predicate, final String object) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public Optional<Map<String, Model>> getObjects(Long id, Long configurationId, Optional<Integer> atMost) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteObject(final Long id, final Long configurationId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Set<String>> getSchema(final Long id, final Long configurationId) {
		// TODO Auto-generated method stub
		return null;
	}
}
