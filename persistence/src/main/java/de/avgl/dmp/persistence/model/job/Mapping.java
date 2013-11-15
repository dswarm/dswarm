package de.avgl.dmp.persistence.model.job;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPUUIDObject;
import de.avgl.dmp.persistence.model.utils.AttributePathReferenceSerializer;
import de.avgl.dmp.persistence.model.utils.SetAttributePathReferenceSerializer;

/**
 * @author tgaengler
 */
@XmlRootElement
// @Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
// @Table(name = "MAPPING")
public class Mapping extends DMPUUIDObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private String				name				= null;

	@XmlElement(name = "input_attribute_paths")
	@JsonSerialize(using = SetAttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlList
	private Set<AttributePath>	inputAttributePaths	= null;

	@XmlElement(name = "output_attribute_path")
	@JsonSerialize(using = AttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private AttributePath		outputAttributePath	= null;

	private Component			transformation		= null;

	@XmlElement(name = "input_filter")
	private Filter				inputFilter			= null;

	@XmlElement(name = "output_filter")
	private Filter				outputFilter		= null;

	public String getName() {

		return name;
	}

	public void setName(final String nameArg) {

		name = nameArg;
	}

	public Set<AttributePath> getInputAttributePaths() {

		return inputAttributePaths;
	}

	public void setInputAttributePaths(final Set<AttributePath> inputAttributePathsArg) {

		inputAttributePaths = inputAttributePathsArg;
	}

	public void addInputAttributePath(final AttributePath inputAttributePath) {

		if (null == inputAttributePaths) {

			inputAttributePaths = Sets.newLinkedHashSet();
		}

		inputAttributePaths.add(inputAttributePath);
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
