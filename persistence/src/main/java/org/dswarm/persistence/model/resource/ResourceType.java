package org.dswarm.persistence.model.resource;

import org.dswarm.persistence.DMPPersistenceException;

/**
 * An enum for describing resource types, e.g., file.
 * 
 * @author tgaengler
 */
public enum ResourceType {

	/**
	 * Indicates that the data resource is a file.
	 */
	FILE("file");

	/**
	 * The name of the resource type as it can occur in a transformation.
	 */
	private final String	rName;

	/**
	 * Creates a new resource with the given name.
	 * 
	 * @param name the name of the resource type
	 */
	private ResourceType(final String name) {

		rName = name;
	}

	/**
	 * Gets the name of the resource type.<br>
	 * Created by: tgaengler
	 * 
	 * @return the name of the resource type
	 */
	String getName() {

		return rName;
	}

	/**
	 * Tries to get a resource type by its name.<br>
	 * Created by: tgaengler
	 * 
	 * @param name the name of the resource type
	 * @return the resource type that matches the given name
	 * @throws DMPPersistenceException if the resource type by the given name is not part of this enumeration, i.e., this resource
	 *             model is probably not implement yet.
	 */
	public static ResourceType getResourceTypeByName(final String name) throws DMPPersistenceException {

		if (name == null) {

			throw new DMPPersistenceException("resource name shouldn't be null");
		}

		for (final ResourceType resourceType : ResourceType.values()) {

			if (resourceType.getName().equals(name)) {

				return resourceType;
			}
		}

		throw new DMPPersistenceException("couldn't determine resource type for resource name = " + name);
	}
}
