/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.model.job;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import org.dswarm.persistence.converter.StringOrderedListConverter;
import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A project is a container that hold the current working state of a job creation, i.e., it knows all relevant parts of a
 * {@link Job}, e.g., a collection of {@link Mapping}s, the sample input {@link DataModel}, the output {@link DataModel} and other
 * related settings.
 *
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
	private static final long serialVersionUID = 1L;

	/**
	 * The sample input data model that will be utilised for demonstration or testing of the mappings of the project.
	 */
	@XmlElement(name = "input_data_model")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "INPUT_DATA_MODEL")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private DataModel inputDataModel;

	/**
	 * The output data model that contains the output schema.
	 */
	@XmlElement(name = "output_data_model")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "OUTPUT_DATA_MODEL")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private DataModel outputDataModel;

	/**
	 * The collection of mappings the project.
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "PROJECTS_MAPPINGS", joinColumns = { @JoinColumn(name = "PROJECT_UUID", referencedColumnName = "UUID") }, inverseJoinColumns = {
			@JoinColumn(name = "MAPPING_UUID", referencedColumnName = "UUID") })
	// @JsonSerialize(using = SetMappingReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @XmlIDREF
	@XmlList
	private Set<Mapping> mappings;

	/**
	 * The skip filter of this project. A skip filter to skip all mappings for records that doesn't match the filter conditions.
	 */
	@XmlElement(name = "skip_filter")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "SKIP_FILTER")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Filter skipFilter;

	/**
	 * The collection of functions that are created in this project, i.e., those functions are only visible to this project.
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "PROJECTS_FUNCTIONS", joinColumns = {
			@JoinColumn(name = "PROJECT_UUID", referencedColumnName = "UUID") }, inverseJoinColumns = {
			@JoinColumn(name = "FUNCTION_UUID", referencedColumnName = "UUID") })
	// @JsonSerialize(using = SetFunctionReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @XmlIDREF
	@XmlList
	private Set<Function> functions;

	/**
	 * An ordered list of selected record identifiers (that are utilised for display purpose or task execution).
	 */
	@Lob
	@XmlElement(name = "selected_records")
	@XmlList
	@Convert(converter = StringOrderedListConverter.class)
	@Column(name = "SELECTED_RECORDS", columnDefinition = "BLOB")
	private Set<String> selectedRecords;

	public Project(final String uuidArg) {

		super(uuidArg);
	}

	protected Project() {

	}

	/**
	 * Gets the sample input data model.
	 *
	 * @return the sample input data model
	 */
	public DataModel getInputDataModel() {

		return inputDataModel;
	}

	/**
	 * Sets the sample input data model
	 *
	 * @param inputDataModel a new sample input data model
	 */
	public void setInputDataModel(final DataModel inputDataModel) {

		this.inputDataModel = inputDataModel;
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
	 * Sets the output data model
	 *
	 * @param outputDataModel a new output data model
	 */
	public void setOutputDataModel(final DataModel outputDataModel) {

		this.outputDataModel = outputDataModel;
	}

	/**
	 * Gets the mappings of the project.
	 *
	 * @return the mappings of the project
	 */
	public Set<Mapping> getMappings() {

		return mappings;
	}

	/**
	 * Sets the mappings of the project.
	 *
	 * @param mappingsArg a new collection of mappings
	 */
	public void setMappings(final Set<Mapping> mappingsArg) {

		if (mappingsArg == null && mappings != null) {

			mappings.clear();
		}

		if (mappingsArg != null) {

			if (!DMPPersistenceUtil.getMappingUtils().completeEquals(mappings, mappingsArg)) {

				if (mappings == null) {

					mappings = Sets.newCopyOnWriteArraySet();
				}

				mappings.clear();
				mappings.addAll(mappingsArg);
			}
		}
	}

	/**
	 * Gets the skip filter of the project.
	 *
	 * @return the skip filter of the project
	 */
	public Filter getSkipFilter() {

		return skipFilter;
	}

	/**
	 * Sets the skip filter of the project.
	 *
	 * @param skipFilterArg a new skip filter
	 */
	public void setSkipFilter(final Filter skipFilterArg) {

		skipFilter = skipFilterArg;
	}

	/**
	 * Gets the functions of the project.
	 *
	 * @return the functions of the project
	 */
	public Set<Function> getFunctions() {

		return functions;
	}

	/**
	 * Sets the functions of the project
	 *
	 * @param functionsArg
	 */
	public void setFunctions(final Set<Function> functionsArg) {

		if (functionsArg == null && functions != null) {

			functions.clear();
		}

		if (functionsArg != null) {

			if (!DMPPersistenceUtil.getFunctionUtils().completeEquals(functions, functionsArg)) {

				if (functions == null) {

					functions = Sets.newCopyOnWriteArraySet();
				}

				functions.clear();
				functions.addAll(functionsArg);
			}
		}
	}

	/**
	 * Gets the ordered list of selected record identifiers (that are utilised for display purpose or task execution).
	 *
	 * @return the ordered list of selected record identifiers
	 */
	public Set<String> getSelectedRecords() {

		return selectedRecords;
	}

	/**
	 * Sets the ordered list of selected record identifiers (that are utilised for display purpose or task execution).
	 *
	 * @param selectedRecordsArg the ordered list of selected record identifiers
	 */
	public void setSelectedRecords(final Set<String> selectedRecordsArg) {

		selectedRecords = selectedRecordsArg;
	}

	/**
	 * Adds a record identifier to the ordered list of selected record identifiers (thar are utilised for display purpose or task execution).
	 *
	 * @param recordIdentifier a record identifier
	 */
	public void addSelectedRecord(final String recordIdentifier) {

		if (selectedRecords == null) {

			selectedRecords = new LinkedHashSet<>();
		}

		selectedRecords.add(recordIdentifier);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Project.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getDataModelUtils().completeEquals(((Project) obj).getInputDataModel(), getInputDataModel())
				&& DMPPersistenceUtil.getDataModelUtils().completeEquals(((Project) obj).getOutputDataModel(), getOutputDataModel())
				&& DMPPersistenceUtil.getMappingUtils().completeEquals(((Project) obj).getMappings(), getMappings())
				&& DMPPersistenceUtil.getFilterUtils().completeEquals(((Project) obj).getSkipFilter(), getSkipFilter())
				&& DMPPersistenceUtil.getFunctionUtils().completeEquals(((Project) obj).getFunctions(), getFunctions())
				&& Objects.equal(((Project) obj).getSelectedRecords(), getSelectedRecords());
	}
}
