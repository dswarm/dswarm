package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tgaengler
 */
@XmlRootElement
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
}
