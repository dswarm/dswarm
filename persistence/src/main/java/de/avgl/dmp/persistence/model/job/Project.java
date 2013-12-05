package de.avgl.dmp.persistence.model.job;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.utils.DMPJPAObjectReferenceSerializer;
import de.avgl.dmp.persistence.model.utils.SetFunctionReferenceSerializer;
import de.avgl.dmp.persistence.model.utils.SetMappingReferenceSerializer;

/**
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "PROJECT")
public class Project extends ExtendedBasicDMPJPAObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "input_data_model")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "INPUT_DATA_MODEL")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	//@XmlIDREF
	private DataModel			inputDataModel;

	@XmlElement(name = "output_data_model")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "OUTPUT_DATA_MODEL")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	//@XmlIDREF
	private DataModel			outputDataModel;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "PROJECTS_MAPPINGS", joinColumns = { @JoinColumn(name = "MAPPING_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID") })
	//@JsonSerialize(using = SetMappingReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@XmlIDREF
	@XmlList
	private Set<Mapping>		mappings;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "PROJECTS_FUNCTIONS", joinColumns = { @JoinColumn(name = "FUNCTION_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID") })
	//@JsonSerialize(using = SetFunctionReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	//@XmlIDREF
	@XmlList
	private Set<Function>		functions;

	public DataModel getInputDataModel() {

		return inputDataModel;
	}

	public void setInputDataModel(final DataModel inputDataModel) {

		this.inputDataModel = inputDataModel;
	}

	public DataModel getOutputDataModel() {

		return outputDataModel;
	}

	public void setOutputDataModel(final DataModel outputDataModel) {

		this.outputDataModel = outputDataModel;
	}

	public Set<Mapping> getMappings() {

		return mappings;
	}

	public void setMappings(final Set<Mapping> mappingsArg) {

		if (mappingsArg == null && mappings != null) {

			mappings.clear();
		}

		if (mappingsArg != null) {

			if (!mappingsArg.equals(mappings)) {

				if (mappings != null) {

					mappings.clear();
					mappings.addAll(mappingsArg);
				} else {

					mappings = mappingsArg;
				}
			}
		}
	}

	public Set<Function> getFunctions() {

		return functions;
	}

	public void setFunctions(final Set<Function> functionsArg) {

		if (functionsArg == null && functions != null) {

			functions.clear();
		}

		if (functionsArg != null) {

			if (!functionsArg.equals(functions)) {

				if (functions != null) {

					functions.clear();
					functions.addAll(functionsArg);
				} else {

					functions = functionsArg;
				}
			}
		}
	}
}
