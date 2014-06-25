package org.dswarm.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A task is an executable {@link Job}, i.e., a job with a concrete input {@link DataModel} and output {@link DataModel}.
 *
 * @author tgaengler
 */
@XmlRootElement
public class Task extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The input data model of the task, i.e., the data source.
	 */
	@XmlElement(name = "input_data_model")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private DataModel			inputDataModel;

	/**
	 * The output data model of the task, i.e., the data target.
	 */
	@XmlElement(name = "output_data_model")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private DataModel			outputDataModel;

	/**
	 * The job that contains the mappings.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private Job					job;

	/**
	 * Gets the input data model.
	 *
	 * @return the input data model
	 */
	public DataModel getInputDataModel() {

		return inputDataModel;
	}

	/**
	 * Sets the input data model.
	 *
	 * @param inputDataModelArg a new input data model
	 */
	public void setInputDataModel(final DataModel inputDataModelArg) {

		inputDataModel = inputDataModelArg;
	}

	/**
	 * Gets the output data model.
	 *
	 * @return the output data model
	 */
	public DataModel getOutputDataModel() {

		return outputDataModel;
	}

	/**
	 * Sets the output data model.
	 *
	 * @param outputDataModelArg a new output data model
	 */
	public void setOutputDataModel(final DataModel outputDataModelArg) {

		outputDataModel = outputDataModelArg;
	}

	/**
	 * Gets the job of the task.
	 *
	 * @return the job of the task
	 */
	public Job getJob() {

		return job;
	}

	/**
	 * Sets the job of the task.
	 *
	 * @param jobArg a new job
	 */
	public void setJob(final Job jobArg) {

		job = jobArg;
	}

	@Override
	public boolean equals(final Object obj) {

		return Task.class.isInstance(obj) && super.equals(obj);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Task.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getDataModelUtils().completeEquals(((Task) obj).getInputDataModel(), getInputDataModel())
				&& DMPPersistenceUtil.getDataModelUtils().completeEquals(((Task) obj).getOutputDataModel(), getOutputDataModel())
				&& DMPPersistenceUtil.getJobUtils().completeEquals(((Task) obj).getJob(), getJob());
	}
}
