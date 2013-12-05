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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.utils.AttributePathReferenceSerializer;
import de.avgl.dmp.persistence.model.utils.SetAttributePathReferenceSerializer;

/**
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

	@XmlElement(name = "input_attribute_paths")
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "INPUT_ATTRIBUTE_PATHS_MAPPINGS", joinColumns = { @JoinColumn(name = "MAPPING_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "INPUT_ATTRIBUTE_PATH_ID", referencedColumnName = "ID") })
	@JsonSerialize(using = SetAttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlList
	private Set<AttributePath>	inputAttributePaths	= null;

	@XmlElement(name = "output_attribute_path")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "OUTPUT_ATTRIBUTE_PATH")
	@JsonSerialize(using = AttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private AttributePath		outputAttributePath	= null;

	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
	@JoinColumn(name = "TRANSFORMATION")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Component			transformation		= null;

	@XmlElement(name = "input_filter")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "INPUT_FILTER")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Filter				inputFilter			= null;

	@XmlElement(name = "output_filter")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "OUTPUT_FILTER")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Filter				outputFilter		= null;

	public Set<AttributePath> getInputAttributePaths() {

		return inputAttributePaths;
	}

	public void setInputAttributePaths(final Set<AttributePath> inputAttributePathsArg) {

		inputAttributePaths = inputAttributePathsArg;
	}

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

	public AttributePath getOutputAttributePath() {

		return outputAttributePath;
	}

	public void setOutputAttributePath(final AttributePath outputAttributePathArg) {

		outputAttributePath = outputAttributePathArg;
	}

	public Component getTransformation() {

		return transformation;
	}

	public void setTransformation(final Component transformationArg) {

		transformation = transformationArg;
	}

	public Filter getInputFilter() {

		return inputFilter;
	}

	public void setInputFilter(final Filter inputFilterArg) {

		inputFilter = inputFilterArg;
	}

	public Filter getOutputFilter() {

		return outputFilter;
	}

	public void setOutputFilter(final Filter outputFilterArg) {

		outputFilter = outputFilterArg;
	}
}
