package de.avgl.dmp.persistence.model.job;

import java.util.Set;

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

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

		if (mappingsArg == null && mappings != null) {

			mappings.clear();
		}

		if (mappingsArg != null) {

			if (mappings == null) {

				mappings = Sets.newCopyOnWriteArraySet();
			}

			if (!DMPPersistenceUtil.getMappingUtils().completeEquals(mappings, mappingsArg)) {

				mappings.clear();
				mappings.addAll(mappingsArg);
			}
		}
	}

	@Override
	public boolean equals(final Object obj) {

		return Job.class.isInstance(obj) && super.equals(obj);

	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Job.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getMappingUtils().completeEquals(((Job) obj).getMappings(), getMappings());
	}
}
