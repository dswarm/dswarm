package de.avgl.dmp.persistence.services;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Attribute;
import de.avgl.dmp.persistence.model.job.AttributePath;
import de.avgl.dmp.persistence.model.job.Schema;

public class AttributePathService extends BasicJPAService<AttributePath, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributePathService.class);

	@Inject
	public AttributePathService(final Provider<EntityManager> entityManagerProvider) {

		super(AttributePath.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final AttributePath object) {

		// should clear the relationship to the schemas and to the attributes
		object.setSchemas(null);
		object.setAttributes(null);
	}

	@Override
	protected void updateObjectInternal(final AttributePath object, final AttributePath updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final Set<Schema> schemas = object.getSchemas();
		final List<Attribute> attributes = object.getAttributes();

		updateObject.setSchemas(schemas);

		updateObject.setAttributes(attributes);

		AttributePathService.LOG.debug("passed internal object update");
	}

}
