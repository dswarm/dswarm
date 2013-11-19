package de.avgl.dmp.persistence.model.job;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "Class")
public class Clasz extends BasicDMPObject {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(Clasz.class);

	/**
	 * 
	 */
	private static final long						serialVersionUID	= 1L;

	public Clasz() {

		super(null);
	}

	public Clasz(final String id) {

		super(id);
	}

	public Clasz(final String id, final String name) {

		super(id);
		setName(name);
	}

	@Override
	public boolean equals(final Object obj) {

		if (!Clasz.class.isInstance(obj)) {

			return false;
		}

		return super.equals(obj);
	}
}
