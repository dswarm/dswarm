package de.avgl.dmp.persistence.model.job;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.schema.AttributePath;

/**
 * A mapping is an instantiation of a {@link Function} or {@link Transformation} with a given collection of input
 * {@link AttributePath}s and an output {@link AttributePath}. Optionally, a mapping can consist of an input {@link Filter} and an
 * output {@link Filter}.
 * 
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "MAPPING")
public class Mapping extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The input attribute path collection of the mapping.
	 */
	@XmlElement(name = "input_attribute_paths")
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "INPUT_ATTRIBUTE_PATHS_MAPPINGS", joinColumns = { @JoinColumn(name = "MAPPING_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "INPUT_ATTRIBUTE_PATH_ID", referencedColumnName = "ID") })
	// @JsonSerialize(using = SetAttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlList
	private Set<AttributePath>	inputAttributePaths;

	/**
	 * The output attribute path of the mapping.
	 */
	@XmlElement(name = "output_attribute_path")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "OUTPUT_ATTRIBUTE_PATH")
	// @JsonSerialize(using = AttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private AttributePath		outputAttributePath;

	/**
	 * The instantiation ({@link Component}) of the function or transformation that should be applied at this mapping.
	 */
	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH,
			CascadeType.REMOVE }, orphanRemoval = true)
	@JoinColumn(name = "TRANSFORMATION")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Component			transformation;

	/**
	 * The input filter of this mapping.
	 */
	@XmlElement(name = "input_filter")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "INPUT_FILTER")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Filter				inputFilter;

	/**
	 * The output filter of this mapping.
	 */
	@XmlElement(name = "output_filter")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "OUTPUT_FILTER")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Filter				outputFilter;

	/**
	 * Gets the input attribute paths of the mapping.
	 * 
	 * @return the input attribute paths of the mapping
	 */
	public Set<AttributePath> getInputAttributePaths() {

		return inputAttributePaths;
	}

	/**
	 * Sets the input attribute paths of the mapping
	 * 
	 * @param inputAttributePathsArg a new collection of input attribute paths
	 */
	public void setInputAttributePaths(final Set<AttributePath> inputAttributePathsArg) {

		if (inputAttributePathsArg == null && inputAttributePaths != null) {

			inputAttributePaths.clear();
		}

		if (inputAttributePathsArg != null) {

			if (inputAttributePaths == null) {

				inputAttributePaths = Sets.newLinkedHashSet();
			}

			if (!inputAttributePaths.equals(inputAttributePathsArg)) {

				inputAttributePaths.clear();
				inputAttributePaths.addAll(inputAttributePathsArg);
			}
		}
	}

	/**
	 * Gets an input attribute path by a given identifier
	 * 
	 * @param id the input attribute path identifier
	 * @return the matched input attribute path or null
	 */
	public AttributePath getInputAttributePath(final Long id) {

		if (id == null) {

			return null;
		}

		if (this.inputAttributePaths == null || this.inputAttributePaths.isEmpty()) {

			return null;
		}

		final List<AttributePath> inputAttributePathsFiltered = filter(having(on(AttributePath.class).getId(), equalTo(id)), this.inputAttributePaths);

		if (inputAttributePathsFiltered == null || inputAttributePathsFiltered.isEmpty()) {

			return null;
		}

		return inputAttributePathsFiltered.get(0);
	}

	/**
	 * Adds a new input attribute path to the collection of input attribute paths of this mapping.<br>
	 * Created by: tgaengler
	 * 
	 * @param inputAttributePath a new input attribute path
	 */
	public void addInputAttributePath(final AttributePath inputAttributePath) {

		if (inputAttributePath != null) {

			if (inputAttributePaths == null) {

				inputAttributePaths = Sets.newLinkedHashSet();
			}

			if (!inputAttributePaths.contains(inputAttributePath)) {

				inputAttributePaths.add(inputAttributePath);
			}
		}
	}

	/**
	 * Removes an existing input attribute path from the collection of input attribute paths of this mapping.<br>
	 * Created by: tgaengler
	 * 
	 * @param inputAttributePath an existing input attribute path that should be removed
	 */
	public void removeInputAttributePath(final AttributePath inputAttributePath) {

		if (inputAttributePaths != null && inputAttributePath != null && inputAttributePaths.contains(inputAttributePath)) {

			inputAttributePaths.remove(inputAttributePath);
		}
	}

	/**
	 * Gets the output attribute path of the mapping.
	 * 
	 * @return the output attribute path of the mapping
	 */
	public AttributePath getOutputAttributePath() {

		return outputAttributePath;
	}

	/**
	 * Sets the output attribute path of the mapping
	 * 
	 * @param outputAttributePathArg a new output attribute path
	 */
	public void setOutputAttributePath(final AttributePath outputAttributePathArg) {

		outputAttributePath = outputAttributePathArg;
	}

	/**
	 * Gets the function or transformation instantiation (component) of the mapping.
	 * 
	 * @return the function or transformation instantiation (component) of the mapping
	 */
	public Component getTransformation() {

		return transformation;
	}

	/**
	 * Sets the function or transformation instantiation (component) of the mapping.
	 * 
	 * @param transformationArg a new function or transformation instantiation (component)
	 */
	public void setTransformation(final Component transformationArg) {

		transformation = transformationArg;
	}

	/**
	 * Gets the input filter of the mapping.
	 * 
	 * @return the input filter of the mapping
	 */
	public Filter getInputFilter() {

		return inputFilter;
	}

	/**
	 * Sets the input filter of the mapping.
	 * 
	 * @param inputFilterArg a new input filter
	 */
	public void setInputFilter(final Filter inputFilterArg) {

		inputFilter = inputFilterArg;
	}

	/**
	 * Gets the output filter of the mapping.
	 * 
	 * @return the output filter of the mapping
	 */
	public Filter getOutputFilter() {

		return outputFilter;
	}

	/**
	 * Sets the output filter of the mapping.
	 * 
	 * @param outputFilterArg a new output filter
	 */
	public void setOutputFilter(final Filter outputFilterArg) {

		outputFilter = outputFilterArg;
	}

	@Override
	public boolean equals(final Object obj) {

		return Mapping.class.isInstance(obj) && super.equals(obj);

	}
}
