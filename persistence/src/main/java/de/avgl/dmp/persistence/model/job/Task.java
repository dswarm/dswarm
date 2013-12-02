package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tgaengler
 */
@XmlRootElement
public class Task extends BasicDMPObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "input_data_model")
	private DataModel			inputDataModel		= null;

	@XmlElement(name = "output_data_model")
	private DataModel			outputDataModel		= null;

	private Job					job					= null;

	public Task(final String id) {

		super(id);
	}

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
