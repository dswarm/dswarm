package de.avgl.dmp.persistence.model.job;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPJPAObject;

/**
 * @author tgaengler
 */
@XmlRootElement
//@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Table(name = "MAPPING")
public class Mapping extends DMPJPAObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID		= 1L;

	Set<AttributePath>			inputAttributePaths		= null;

	Set<AttributePath>			outputAttributePaths	= null;

	Transformation				transformation			= null;

	public Set<AttributePath> getInputAttributePaths() {

		return inputAttributePaths;
	}

	public void setInputAttributePaths(final Set<AttributePath> inputAttributePaths) {

		this.inputAttributePaths = inputAttributePaths;
	}
	
	public void addInputAttributePath(final AttributePath inputAttributePath) {
		
		if(null == inputAttributePaths) {
			
			inputAttributePaths = Sets.newLinkedHashSet();
		}
		
		inputAttributePaths.add(inputAttributePath);
	}

	public Set<AttributePath> getOutputAttributePaths() {

		return outputAttributePaths;
	}

	public void setOutputAttributePaths(final Set<AttributePath> outputAttributePaths) {

		this.outputAttributePaths = outputAttributePaths;
	}
	
	public void addOutputAttributePath(final AttributePath outputAttributePath) {
		
		if(null == outputAttributePaths) {
			
			outputAttributePaths = Sets.newLinkedHashSet();
		}
		
		outputAttributePaths.add(outputAttributePath);
	}

	public Transformation getTransformation() {

		return transformation;
	}

	public void setTransformation(final Transformation transformation) {

		this.transformation = transformation;
	}
}
