package de.avgl.dmp.persistence.model.schema;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;

/**
 * A class is a type. In a graph a node or edge can have a type, e.g., foaf:Document.
 * 
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CLASS")
public class Clasz extends AdvancedDMPJPAObject {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(Clasz.class);

	/**
	 *
	 */
	private static final long						serialVersionUID	= 1L;

	/**
	 * Creates new class with no identifier.
	 */
	public Clasz() {

		super(null);
	}

	/**
	 * Creates a new class with the given identifier.
	 * 
	 * @param id a class identifier
	 */
	public Clasz(final String id) {

		super(id);
	}

	/**
	 * Creates a new class with the given identifier and name-
	 * 
	 * @param id a class identifier
	 * @param name a class name
	 */
	public Clasz(final String id, final String name) {

		super(id);
		setName(name);
	}

	@Override
	public boolean equals(final Object obj) {

		return Clasz.class.isInstance(obj) && super.equals(obj);

	}
}
