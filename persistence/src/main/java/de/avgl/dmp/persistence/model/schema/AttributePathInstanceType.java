package de.avgl.dmp.persistence.model.schema;

/**
 * The attribute path instance type enum. An attribute path instance type indicates the attribute path instance type of an
 * attribute path instance.<br>
 * 
 * @author tgaengler (created), Mar 18, 2013
 * @author $Author$ (last changed)
 * @version $Rev$, $Date$<br>
 *          $Id: $
 */

public enum AttributePathInstanceType {

	/**
	 * The attribute path instance type to indicate mapping attribute path instances ({@link MappingAttributePathInstance}).
	 */
	MappingAttributePathInstance("MappingAttributePathInstance");

	/**
	 * The name of the attribute path instance type.
	 */
	private final String	name;

	/**
	 * Gets the name of the attribute path instance type.
	 * 
	 * @return the name of the attribute path instance type
	 */
	public String getName() {

		return name;
	}

	/**
	 * Creates a new attribute path instance type with the given name.
	 * 
	 * @param nameArg the name of the attribute path instance type.
	 */
	private AttributePathInstanceType(final String nameArg) {

		name = nameArg;
	}

	/**
	 * Gets the attribute path instance type by the given name, e.g. 'Mapping Attribute Path Instance'.<br>
	 * Created by: ydeng
	 * 
	 * @param name the name of the attribute path instance type
	 * @return the appropriated attribute path instance type
	 */
	public static AttributePathInstanceType getByName(final String name) {

		for (final AttributePathInstanceType attributePathInstanceType : AttributePathInstanceType.values()) {

			if (attributePathInstanceType.name.equals(name)) {

				return attributePathInstanceType;
			}
		}

		throw new IllegalArgumentException(name);
	}

	/**
	 * {@inheritDoc}<br>
	 * Returns the name of the attribute path instance type.
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {

		return name;
	}
}
