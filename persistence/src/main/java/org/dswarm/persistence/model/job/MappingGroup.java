package org.dswarm.persistence.model.job;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.AdvancedDMPJPAObject;

/**
 * @author tgaengler
 */
@XmlRootElement
public class MappingGroup extends AdvancedDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	private List<Mapping>		mappings;

	private String				description;

	public MappingGroup(final String id) {

		super(id);
	}

	public List<Mapping> getMappings() {

		return mappings;
	}

	public void setMappings(final List<Mapping> mappingsArg) {

		mappings = mappingsArg;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(final String descriptionArg) {

		description = descriptionArg;
	}
}
