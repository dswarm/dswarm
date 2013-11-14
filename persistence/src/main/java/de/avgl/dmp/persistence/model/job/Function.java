package de.avgl.dmp.persistence.model.job;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

import de.avgl.dmp.persistence.model.DMPUUIDObject;

/**
 * @author tgaengler
 */
@XmlRootElement
// @Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
// @Table(name = "FUNCTION")
public class Function extends DMPUUIDObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private String				name				= null;

	private String				description			= null;

	private LinkedList<String>	parameters			= null;

	public String getDescription() {

		return description;
	}

	public void setDescription(final String description) {

		this.description = description;
	}

	public LinkedList<String> getParameters() {

		return parameters;
	}

	public void setParameters(final LinkedList<String> parametersArg) {

		this.parameters = parametersArg;
	}
	
	public void addParameter(final String parameter) {
		
		if(null == parameters) {
			
			parameters = Lists.newLinkedList();
		}
		
		parameters.add(parameter);
	}

	public String getName() {

		return name;
	}

	public void setName(final String name) {

		this.name = name;
	}
}
