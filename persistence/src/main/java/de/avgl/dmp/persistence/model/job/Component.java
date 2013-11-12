package de.avgl.dmp.persistence.model.job;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Component extends DMPObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	Set<Component>				inputComponents		= null;

	Set<Component>				outputComponents	= null;

	Function					function			= null;

	Map<String, String>			parameterMappings	= null;

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
