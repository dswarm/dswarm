package de.avgl.dmp.persistence.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The abstract POJO class for entities where the identifier should be provided at object creation.
 * 
 * @author tgaengler
 */
@XmlRootElement
@MappedSuperclass
public abstract class BasicDMPObject extends DMPObject<String> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The identifier of the entity.
	 */
	@Id
	@XmlID
	@Access(AccessType.FIELD)
	@Column(name = "ID", columnDefinition = "VARCHAR(100)", length = 100)
	private final String		id;

	/**
	 * The name of the entity.
	 */
	@Column(name = "NAME")
	private String				name;

	protected BasicDMPObject(final String idArg) {

		id = idArg;
	}

	@Override
	public String getId() {

		return id;
	}

	/**
	 * Gets the name of the entity.
	 * 
	 * @return the name of the entity
	 */
	public String getName() {

		return name;
	}

	/**
	 * Sets the name of the entity.
	 * 
	 * @param name the name of the entity
	 */
	public void setName(final String name) {

		this.name = name;
	}
}
