package de.avgl.dmp.persistence.model.job;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author tgaengler
 *
 */
@XmlRootElement
public class Job extends DMPObject {

	private List<Transformation>	transformations;

	public List<Transformation> getTransformations() {

		return transformations;
	}

	public void setTransformations(final List<Transformation> transformations) {

		this.transformations = transformations;
	}

}
