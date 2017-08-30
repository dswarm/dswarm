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

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import ch.lambdaj.Lambda;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;
import org.hamcrest.Matchers;

import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A mapping is an instantiation of a {@link Function} or {@link Transformation} with a given collection of input
 * {@link AttributePath}s and an output {@link AttributePath}. Optionally, a mapping can consist of an input {@link Filter} and an
 * output {@link Filter}.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "MAPPING")
public class Mapping extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The input attribute path collection of the mapping.
	 */
	@XmlElement(name = "input_attribute_paths")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "MAPPINGS_INPUT_ATTRIBUTE_PATHS", joinColumns = { @JoinColumn(name = "MAPPING_UUID", referencedColumnName = "UUID") },
			inverseJoinColumns = { @JoinColumn(name = "INPUT_ATTRIBUTE_PATH_UUID", referencedColumnName = "UUID") })
	// @JsonSerialize(using = SetAttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlList
	private Set<MappingAttributePathInstance> inputAttributePaths;

	/**
	 * The output attribute path of the mapping.
	 */
	@XmlElement(name = "output_attribute_path")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "OUTPUT_ATTRIBUTE_PATH")
	// @JsonSerialize(using = AttributePathReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private MappingAttributePathInstance outputAttributePath;

	/**
	 * The instantiation ({@link Component}) of the function or transformation that should be applied at this mapping.
	 */
	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH,
			CascadeType.REMOVE }, orphanRemoval = true)
	@JoinColumn(name = "TRANSFORMATION")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Component transformation;

	public Mapping(final String uuidArg) {

		super(uuidArg);
	}

	protected Mapping() {

		// y?
	}

	/**
	 * Gets the input attribute paths of the mapping.
	 *
	 * @return the input attribute paths of the mapping
	 */
	public Set<MappingAttributePathInstance> getInputAttributePaths() {

		return inputAttributePaths;
	}

	/**
	 * Sets the input attribute paths of the mapping
	 *
	 * @param inputAttributePathsArg a new collection of input attribute paths
	 */
	public void setInputAttributePaths(final Set<MappingAttributePathInstance> inputAttributePathsArg) {

		if (inputAttributePathsArg == null && inputAttributePaths != null) {

			inputAttributePaths.clear();
		}

		if (inputAttributePathsArg != null) {

			if (inputAttributePaths == null) {

				inputAttributePaths = Sets.newLinkedHashSet();
			}

			if (!DMPPersistenceUtil.getMappingAttributePathInstanceUtils().completeEquals(inputAttributePaths, inputAttributePathsArg)) {

				inputAttributePaths.clear();
				inputAttributePaths.addAll(inputAttributePathsArg);
			}
		}
	}

	/**
	 * Gets an input attribute path by a given identifier
	 *
	 * @param uuid the input attribute path identifier
	 * @return the matched input attribute path or null
	 */
	public MappingAttributePathInstance getInputAttributePath(final String uuid) {

		if (uuid == null) {

			return null;
		}

		if (inputAttributePaths == null || inputAttributePaths.isEmpty()) {

			return null;
		}

		final List<MappingAttributePathInstance> inputAttributePathsFiltered = Lambda.filter(
				Lambda.having(Lambda.on(MappingAttributePathInstance.class).getUuid(), Matchers.equalTo(uuid)), inputAttributePaths);

		if (inputAttributePathsFiltered == null || inputAttributePathsFiltered.isEmpty()) {

			return null;
		}

		return inputAttributePathsFiltered.get(0);
	}

	/**
	 * Adds a new input attribute path to the collection of input attribute paths of this mapping.<br>
	 * Created by: tgaengler
	 *
	 * @param inputAttributePath a new input attribute path
	 */
	public void addInputAttributePath(final MappingAttributePathInstance inputAttributePath) {

		if (inputAttributePath != null) {

			if (inputAttributePaths == null) {

				inputAttributePaths = Sets.newLinkedHashSet();
			}

			if (!inputAttributePaths.contains(inputAttributePath)) {

				inputAttributePaths.add(inputAttributePath);
			}
		}
	}

	/**
	 * Removes an existing input attribute path from the collection of input attribute paths of this mapping.<br>
	 * Created by: tgaengler
	 *
	 * @param inputAttributePath an existing input attribute path that should be removed
	 */
	public void removeInputAttributePath(final MappingAttributePathInstance inputAttributePath) {

		if (inputAttributePaths != null && inputAttributePath != null && inputAttributePaths.contains(inputAttributePath)) {

			inputAttributePaths.remove(inputAttributePath);
		}
	}

	/**
	 * Gets the output attribute path of the mapping.
	 *
	 * @return the output attribute path of the mapping
	 */
	public MappingAttributePathInstance getOutputAttributePath() {

		return outputAttributePath;
	}

	/**
	 * Sets the output attribute path of the mapping
	 *
	 * @param outputAttributePathArg a new output attribute path
	 */
	public void setOutputAttributePath(final MappingAttributePathInstance outputAttributePathArg) {

		outputAttributePath = outputAttributePathArg;
	}

	/**
	 * Gets the function or transformation instantiation (component) of the mapping.
	 *
	 * @return the function or transformation instantiation (component) of the mapping
	 */
	public Component getTransformation() {

		return transformation;
	}

	/**
	 * Sets the function or transformation instantiation (component) of the mapping.
	 *
	 * @param transformationArg a new function or transformation instantiation (component)
	 */
	public void setTransformation(final Component transformationArg) {

		transformation = transformationArg;
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Mapping.class.isInstance(obj)
				&& super.completeEquals(obj)
				&& DMPPersistenceUtil.getMappingAttributePathInstanceUtils().completeEquals(((Mapping) obj).getInputAttributePaths(),
				getInputAttributePaths())
				&& DMPPersistenceUtil.getMappingAttributePathInstanceUtils().completeEquals(((Mapping) obj).getOutputAttributePath(),
				getOutputAttributePath())
				&& DMPPersistenceUtil.getComponentUtils().completeEquals(((Mapping) obj).getTransformation(), getTransformation());
	}
}
