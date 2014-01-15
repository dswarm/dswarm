package de.avgl.dmp.persistence.service.schema;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.AdvancedJPAService;
import de.avgl.dmp.persistence.service.BasicIDJPAService;

/**
 * A persistence service for {@link AttributePath}s.
 * 
 * @author tgaengler
 *
 */
public class AttributePathService extends BasicIDJPAService<AttributePath> {
	
	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AdvancedJPAService.class);
	
	/**
	 * The attribute path service provider (powered by Guice).
	 */
	private final Provider<AttributeService> attributeServiceProvider;

	/**
	 * Creates a new attribute path persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public AttributePathService(final Provider<EntityManager> entityManagerProvider, final Provider<AttributeService> attributeServiceProvider) {

		super(AttributePath.class, entityManagerProvider);
		this.attributeServiceProvider = attributeServiceProvider;
	}

	/* Creates a new persistent AttributePath when no identical one already exists 
	 * (having the same attributePath). Otherwise the existing object is returned.
	 * (non-Javadoc)
	 * @param path - the path as JSON object string of the AttributePath //TODO remove redundant parameter
	 * @param attributes - a list of the path Attributes
	 * @return the persisted AttributePath with the given path
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
	public AttributePath createObject(final String path, final LinkedList<Attribute> attributes) throws DMPPersistenceException {

		final AttributePath existingObject = getObject(path);
		final LinkedList<Attribute> persistentAttributes = new LinkedList<Attribute>();
		
		// updating all attributes in the list
		// TODO: transform to set before to avoid updating attributes that occur multiple times in the list
		for (Iterator<Attribute> iterator = attributes.iterator(); iterator.hasNext();) {
			Attribute attribute = (Attribute) iterator.next();
			
			// do we need a non-transactional updateOrCreate to simplify this?:
			Attribute persistentAttribute = attributeServiceProvider.get().createObject(attribute.getId());
			persistentAttribute.setName(attribute.getName());
			
			persistentAttributes.add(persistentAttribute);
		}	

		final AttributePath object;

		if (null == existingObject) {

			object = new AttributePath(persistentAttributes);
					
			persistObject(object);
			
		} else {

			AttributePathService.LOG.debug(AttributePath.class.getName() + " with path '" + path
					+ "' exists already in the database. Will return the existing object, instead of creating a new one");

			object = existingObject;
		}

		return object;
	}
	
	
	/**
	 * Gets the AttributePath with the given path (as JSON object string)<br>
	 * Created by: polowins
	 * 
	 * @return the AttributePath with the given path (as JSON object string) or null if no such object exists
	 */
	public AttributePath getObject(String jsonPath) {

		AttributePath object = null;
		
		String queryString = "from " + AttributePath.class.getName() + " where attributePath = '" + jsonPath + "'";
		//final EntityManager entityManager = entityManagerProvider.get();
		final EntityManager entityManager = acquire();
		final TypedQuery<AttributePath> query = entityManager.createQuery(queryString, AttributePath.class);

		try {
			object = query.getSingleResult();
		}
		catch (NoResultException e) {
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
	
	
	public List<AttributePath> getAttributePathsWithPath(String jsonPath){
		String queryString = "from " + AttributePath.class.getName() + " where attributePath = '" + jsonPath + "'";
		final EntityManager entityManager = acquire();
		final TypedQuery<AttributePath> query = entityManager.createQuery(queryString, AttributePath.class);
		return query.getResultList();
	}

}
