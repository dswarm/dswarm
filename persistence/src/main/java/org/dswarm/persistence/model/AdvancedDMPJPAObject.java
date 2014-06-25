package org.dswarm.persistence.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * The abstract POJO class for entities where the uri of the entity should be provided at object creation.
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
	private final String		uri;

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

	@Override
	public boolean completeEquals(final Object obj) {

		return AdvancedDMPJPAObject.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((AdvancedDMPJPAObject) obj).getUri(), getUri());
	}
}
