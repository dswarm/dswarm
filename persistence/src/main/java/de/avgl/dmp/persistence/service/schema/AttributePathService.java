package de.avgl.dmp.persistence.service.schema;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.BasicIDJPAService;

/**
 * A persistence service for {@link AttributePath}s.
 * 
 * @author tgaengler
 */
public class AttributePathService extends BasicIDJPAService<AttributePath> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributePathService.class);

	/**
	 * Creates a new attribute path persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public AttributePathService(final Provider<EntityManager> entityManagerProvider) {

		super(AttributePath.class, entityManagerProvider);
	}

	/**
	 * Persists the given attribute path or returns the existing one from the DB.
	 * 
	 * @param attributePath an attribute path that should be persisted
	 * @return the persisted or matched attribute path from the DB
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public AttributePath createObject(final AttributePath attributePath) throws DMPPersistenceException {

		return createObjectInternal(attributePath);
	}

	/**
	 * Creates an attribute path with the given ordered list of attributes or returns the existing one from the DB.
	 * 
	 * @param attributes an ordered list of attributes
	 * @return the persisted or matched attribute path from DB
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public AttributePath createObject(final LinkedList<Attribute> attributes) throws DMPPersistenceException {

		final AttributePath tempAttributePath = new AttributePath(attributes);

		return createObjectInternal(tempAttributePath);
	}

	/**
	 * Tries to retrieve an attribute path object for the given ordered list of attribute paths
	 * 
	 * @param attributePathJSONArrayString
	 * @return
	 */
	public AttributePath getObject(final String attributePathJSONArrayString) {

		final EntityManager entityManager = acquire();

		return getObject(attributePathJSONArrayString, entityManager);
	}

	private AttributePath createObjectInternal(final AttributePath attributePath) throws DMPPersistenceException {

		final EntityManager em = acquire();

		final AttributePath existingObject = getObject(attributePath.getAttributePathAsJSONObjectString(), em);

		final AttributePath object;

		if (null == existingObject) {

			final AttributePath tempAttributePath = new AttributePath();

			final LinkedList<Attribute> attributes = attributePath.getAttributePath();

			for (final Attribute attribute : attributes) {

				final Attribute managedAttribute = em.merge(attribute);
				tempAttributePath.addAttribute(managedAttribute);
			}

			persistObject(tempAttributePath, em);

			object = tempAttributePath;
		} else {

			object = existingObject;

			AttributePathService.LOG.debug("attribute path with path '" + attributePath.toAttributePath()
					+ "' exists already in the database. Will return the existing object, instead of creating a new one");
		}

		return object;
	}

	private AttributePath getObject(final String attributePath, final EntityManager entityManager) {

		final AttributePath object;

		final String queryString = "from " + className + " where attributePath = '" + attributePath + "'";
		final TypedQuery<AttributePath> query = entityManager.createQuery(queryString, clasz);

		try {

			object = query.getSingleResult();
		} catch (final NoResultException e) {

			// TODO: maybe log something here

			return null;
		}

		return object;
	}

	@Override
	protected void prepareObjectForRemoval(final AttributePath object) {

		// should clear the relationship to the attributes
		object.setAttributePath(null);
	}

	@Override
	protected void updateObjectInternal(final AttributePath object, final AttributePath updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final LinkedList<Attribute> attributes = object.getAttributePath();

		updateObject.setAttributePath(attributes);
	}

	public List<AttributePath> getAttributePathsWithPath(final String attributePathJSONArrayString) {
		
		final EntityManager entityManager = acquire(true);
		
		final String queryString = "from " + AttributePath.class.getName() + " where attributePath = '" + attributePathJSONArrayString + "'";
		
		final TypedQuery<AttributePath> query = entityManager.createQuery(queryString, AttributePath.class);
		
		return query.getResultList();
	}

}
