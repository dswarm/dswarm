package de.avgl.dmp.persistence.model.job;

import java.util.Set;

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.resource.DataModel;

/**
 * A job is a collection of {@link Mapping}s that can be execution on a given input {@link DataModel} and be written to a given
 * output {@link DataModel}.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class Job extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The collection of mappings of the job.
	 */
	// @JsonSerialize(using = SetMappingReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @XmlIDREF
	@XmlList
	private Set<Mapping>		mappings;

	/**
	 * Gets the collection of mappings of the job.
	 * 
	 * @return the collection of mappings of the job
	 */
	public Set<Mapping> getMappings() {

		return mappings;
	}

	/**
	 * Sets the collection of mappings of the job.
	 * 
	 * @param mappingsArg a new collection of mappings
	 */
	public void setMappings(final Set<Mapping> mappingsArg) {

		mappings = mappingsArg;
	}
}
