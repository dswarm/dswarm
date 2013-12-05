package de.avgl.dmp.persistence.model.job;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.BasicDMPObject;

/**
 * @author tgaengler
 */
@XmlRootElement
public class Job extends BasicDMPObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private List<Mapping>		mappings;

	public Job(final String id) {

		super(id);
	}

	public List<Mapping> getMappings() {

		return mappings;
	}

	public void setMappings(final List<Mapping> mappingsArg) {

		mappings = mappingsArg;
	}
}
