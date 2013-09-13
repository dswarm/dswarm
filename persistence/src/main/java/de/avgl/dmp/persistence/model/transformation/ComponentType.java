package de.avgl.dmp.persistence.model.transformation;

import de.avgl.dmp.persistence.DMPPersistenceException;

public enum ComponentType {

	EXTENDED("extended"), FUNCTION("fun"), SOURCE("source"), TARGET("target");

	/**
	 * The name of the component type as it can occur in a transformation.
	 */
	private final String	ctName;

	/**
	 * Creates a new component with the given name.
	 *
	 * @param name the name of the component type
	 */
	private ComponentType(final String name) {

		ctName = name;
	}

	/**
	 * Gets the name of the component type.<br>
	 * Created by: tgaengler
	 *
	 * @return the name of the component type
	 */
	public String getName() {

		return ctName;
	}

	/**
	 * Tries to get a component type by its name.<br>
	 * Created by: tgaengler
	 *
	 * @param name the name of the component type
	 * @return the component type that matches the given name
	 * @throws DMPPersistenceException if the component type by the given name is not part of this enumeration, i.e., this
	 *             connection model is probably not implement yet.
	 */
	public static ComponentType getComponentTypeByName(final String name) throws DMPPersistenceException {

		if (name == null) {

			throw new DMPPersistenceException("component name shouldn't be null");
		}

		for (final ComponentType componentType : ComponentType.values()) {

			if (componentType.getName().equals(name)) {

				return componentType;
			}
		}

		throw new DMPPersistenceException("couldn't determine component type for connection name = " + name);
	}

}
