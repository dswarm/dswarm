package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.utils.DMPJPAObjectReferenceSerializer;

/**
 * @author tgaengler
 */
@XmlRootElement
public class Task extends ExtendedBasicDMPJPAObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "input_data_model")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	//@XmlIDREF
	private DataModel			inputDataModel		= null;

	@XmlElement(name = "output_data_model")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	//@XmlIDREF
	private DataModel			outputDataModel		= null;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	//@XmlIDREF
	private Job					job					= null;

	public DataModel getInputDataModel() {
		
		return inputDataModel;
	}

	public void setInputDataModel(final DataModel inputDataModelArg) {

		inputDataModel = inputDataModelArg;
	}

	public DataModel getOutputDataModel() {

		return outputDataModel;
	}

	public void setOutputDataModel(final DataModel outputDataModelArg) {

		outputDataModel = outputDataModelArg;
	}

	public Job getJob() {

		return job;
	}

	public void setJob(final Job jobArg) {

		job = jobArg;
	}
}
