package de.avgl.dmp.persistence.model.job;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.avgl.dmp.persistence.model.DMPUUIDObject;
import de.avgl.dmp.persistence.model.utils.DMPUUIDObjectReferenceSerializer;
import de.avgl.dmp.persistence.model.utils.SetComponentReferenceSerializer;

@XmlRootElement
public class Component extends DMPUUIDObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private String				name;

	@XmlElement(name = "input_components")
	@JsonSerialize(using = SetComponentReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlIDREF
	@XmlList
	private Set<Component>		inputComponents		= null;

	@XmlElement(name = "output_components")
	@JsonSerialize(using = SetComponentReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlIDREF
	@XmlList
	private Set<Component>		outputComponents	= null;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonSerialize(using = DMPUUIDObjectReferenceSerializer.class)
	@XmlIDREF
	private Function			function			= null;

	@XmlElement(name = "parameter_mappings")
	private Map<String, String>	parameterMappings	= null;

	public String getName() {

		return name;
	}

	public void setName(final String name) {

		this.name = name;
	}

	public Set<Component> getInputComponents() {

		return inputComponents;
	}

	public void setInputComponents(final Set<Component> inputComponentsArg) {

		inputComponents = inputComponentsArg;
	}

	public Set<Component> getOutputComponents() {

		return outputComponents;
	}

	public void setOutputComponents(final Set<Component> outputComponentsArg) {

		outputComponents = outputComponentsArg;
	}

	public Function getFunction() {

		return function;
	}

	public void setFunction(final Function functionArg) {

		function = functionArg;
	}

	public Map<String, String> getParameterMappings() {

		return parameterMappings;
	}

	public void setParameterMapping(final Map<String, String> parameterMappingsArg) {

		parameterMappings = parameterMappingsArg;
	}
}
