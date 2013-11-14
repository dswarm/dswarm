package de.avgl.dmp.persistence.model.job;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tgaengler
 */
@XmlRootElement
//@Entity
//@Cacheable(true)
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Table(name = "TRANSFORMATION")
public class Transformation extends Function {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private Set<Component>		components;

	//private Component			inputComponent;

	//private Component			outputComponent;

	public Set<Component> getComponents() {

		return components;
	}

	public void setComponents(final Set<Component> components) {

		this.components = components;
	}

//	public Component getInputComponent() {
//		
//		return inputComponent;
//	}
//
//	public void setInputComponent(final Component inputComponent) {
//		
//		this.inputComponent = inputComponent;
//	}
//
//	public Component getOutputComponent() {
//		
//		return outputComponent;
//	}
//
//	public void setOutputComponent(final Component outputComponent) {
//		
//		this.outputComponent = outputComponent;
//	}
}
