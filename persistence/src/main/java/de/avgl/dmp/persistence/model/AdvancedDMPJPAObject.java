package de.avgl.dmp.persistence.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The abstract POJO class for entities where the identifier should be provided at object creation.
 * 
 * @author tgaengler
 */
@XmlRootElement
@MappedSuperclass
public abstract class AdvancedDMPJPAObject extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	// @Id
	@Access(AccessType.FIELD)
	@Column(name = "URI", columnDefinition = "VARCHAR(255)", length = 255)
	private String				uri;

	protected AdvancedDMPJPAObject() {

		uri = null;
	}

	protected AdvancedDMPJPAObject(final String uriArg) {

		uri = uriArg;
	}

	protected AdvancedDMPJPAObject(final String uriArg, final String name) {

		uri = uriArg;
		setName(name);
	}

	public String getUri() {

		return uri;
	}
}
