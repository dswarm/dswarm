package de.avgl.dmp.persistence.model.job;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.DMPJPAObject;

/**
 * @author tgaengler
 */
@XmlRootElement
// @Entity
//@Cacheable(true)
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
// @Table(name = "FUNCTION")
public class Function extends DMPJPAObject {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private String name = null;
	
	private String description = null;
	
	private List<String> parameter = null;
	
	public String getDescription() {
		
		return description;
	}
	
	public void setDescription(String description) {
		
		this.description = description;
	}
	
	public List<String> getParameter() {
		
		return parameter;
	}
	
	public void setParameter(List<String> parameter) {
		
		this.parameter = parameter;
	}

	public String getName() {
		
		return name;
	}

	
	public void setName(String name) {
		
		this.name = name;
	}
}
