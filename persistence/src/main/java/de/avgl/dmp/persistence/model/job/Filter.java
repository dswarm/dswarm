package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.DMPUUIDObject;

/**
 * @author tgaengler
 */
@XmlRootElement
//@Entity
//@Cacheable(true)
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Table(name = "FILTER")
public class Filter extends DMPUUIDObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
	private String expression = null;

	
	public String getExpression() {
		
		return expression;
	}

	
	public void setExpression(final String expressionArg) {
		
		this.expression = expressionArg;
	}
}
