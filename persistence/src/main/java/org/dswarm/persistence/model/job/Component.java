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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.DMPException;
import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.representation.SetComponentReferenceSerializer;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A component is part of a concrete {@link Transformation} (i.e. a component belongs to concrete transformation and is not
 * sharable with other transformations). A component refers to or instantiates a {@link Function} by a collection of parameter
 * mappings. A component can be related to several input and output components.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "COMPONENT")
public class Component extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(Component.class);

	/**
	 * The input components collection.
	 */
	@ManyToMany(mappedBy = "outputComponents", fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	@XmlElement(name = "input_components")
	@JsonSerialize(using = SetComponentReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlIDREF
	@XmlList
	private Set<Component> inputComponents;

	/**
	 * The output components collection.
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { /* CascadeType.DETACH, CascadeType.MERGE, */CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "INPUT_COMPONENTS_OUTPUT_COMPONENTS", joinColumns = {
			@JoinColumn(name = "INPUT_COMPONENT_UUID", referencedColumnName = "UUID") }, inverseJoinColumns = {
			@JoinColumn(name = "OUTPUT_COMPONENT_UUID", referencedColumnName = "UUID") })
	@XmlElement(name = "output_components")
	@JsonSerialize(using = SetComponentReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlIDREF
	@XmlList
	private Set<Component> outputComponents;

	/**
	 * The function that is instantiated by this component.
	 */
	// @ManyToOne(fetch = FetchType.LAZY/*, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
	// CascadeType.REFRESH }*/)
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "FUNCTION")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	// @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Function function;

	/**
	 * The map of parameter mappings.
	 */
	@Transient
	private Map<String, String> parameterMappings;

	/**
	 * The JSON object of the parameter mappings map.
	 */
	@Transient
	private ObjectNode parameterMappingsJSON;

	/**
	 * A flag that indicates, whether the parameter mappings are initialized or not.
	 */
	@Transient
	private boolean parameterMappingsInitialized;

	/**
	 * The string that holds the serialised JSON object of the parameter mappings map.
	 */
	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "PARAMETER_MAPPINGS", columnDefinition = "BLOB")
	private byte[] parameterMappingsString;

	// @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH })
	// @JoinColumn(name = "transformation")
	// @JsonIgnore
	// private Transformation transformation = null;

	protected Component() {

	}

	public Component(final String uuidArg) {

		super(uuidArg);
	}

	/**
	 * Gets the input components collection.
	 *
	 * @return the input components collection
	 */
	public Set<Component> getInputComponents() {

		return inputComponents;
	}

	/**
	 * Sets the input components collections.
	 *
	 * @param inputComponentsArg the new input components collection
	 */
	public void setInputComponents(final Set<Component> inputComponentsArg) {

		if (inputComponentsArg == null && inputComponents != null) {

			// remove component from input components, if component will be prepared for removal

			final Set<Component> componentsToBeDeleted = Sets.newCopyOnWriteArraySet(inputComponents);

			for (final Component inputComponent : componentsToBeDeleted) {

				inputComponent.removeOutputComponent(this);
			}

			inputComponents.clear();
		}

		if (inputComponentsArg != null) {

			if (inputComponents == null) {

				inputComponents = Sets.newCopyOnWriteArraySet();
			}

			// if (!inputComponents.equals(inputComponentsArg)) {
			if (!DMPPersistenceUtil.getComponentUtils().completeEquals(inputComponents, inputComponentsArg)) {

				inputComponents.clear();
				inputComponents.addAll(inputComponentsArg);
			}

			for (final Component inputComponent : inputComponentsArg) {

				inputComponent.addOutputComponent(this);
			}
		}
	}

	/**
	 * Adds a new input component to the collection of input components of this component.<br>
	 * Created by: tgaengler
	 *
	 * @param inputComponent a new input component
	 */
	public void addInputComponent(final Component inputComponent) {

		if (inputComponent != null) {

			if (inputComponents == null) {

				inputComponents = Sets.newCopyOnWriteArraySet();
			}

			if (!inputComponents.contains(inputComponent)) {

				inputComponents.add(inputComponent);
				inputComponent.addOutputComponent(this);
			}
		}
	}

	/**
	 * Removes an existing input component from the collection of input components of this component.<br>
	 * Created by: tgaengler
	 *
	 * @param inputComponent an existing input component that should be removed
	 */
	public void removeInputComponent(final Component inputComponent) {

		if (inputComponents != null && inputComponent != null && inputComponents.contains(inputComponent)) {

			inputComponents.remove(inputComponent);

			inputComponent.removeOutputComponent(this);
		}
	}

	/**
	 * Gets the output components collection.
	 *
	 * @return the output components collection
	 */
	public Set<Component> getOutputComponents() {

		return outputComponents;
	}

	/**
	 * Sets the output component collection.
	 *
	 * @param outputComponentsArg a new output component collection
	 */
	public void setOutputComponents(final Set<Component> outputComponentsArg) {

		if (outputComponentsArg == null && outputComponents != null) {

			// remove component from output component, if component will be prepared for removal

			final Set<Component> componentsToBeDeleted = Sets.newCopyOnWriteArraySet(outputComponents);

			for (final Component outputComponent : componentsToBeDeleted) {

				outputComponent.removeInputComponent(this);
			}

			outputComponents.clear();
		}

		if (outputComponentsArg != null) {

			if (outputComponents == null) {

				outputComponents = Sets.newCopyOnWriteArraySet();
			}

			// if (!outputComponents.equals(outputComponentsArg)) {
			if (!DMPPersistenceUtil.getComponentUtils().completeEquals(outputComponents, outputComponentsArg)) {

				outputComponents.clear();
				outputComponents.addAll(outputComponentsArg);
			}

			for (final Component outputComponent : outputComponentsArg) {

				outputComponent.addInputComponent(this);
			}
		}
	}

	/**
	 * Adds a new output component to the collection of output components of this component.<br>
	 * Created by: tgaengler
	 *
	 * @param outputComponent a new output component
	 */
	public void addOutputComponent(final Component outputComponent) {

		if (outputComponent != null) {

			if (outputComponents == null) {

				outputComponents = Sets.newCopyOnWriteArraySet();
			}

			if (!outputComponents.contains(outputComponent)) {

				outputComponents.add(outputComponent);
				outputComponent.addInputComponent(this);
			}
		}
	}

	/**
	 * Removes an existing output component from the collection of output components of this component.<br>
	 * Created by: tgaengler
	 *
	 * @param outputComponent an existing output component that should be removed
	 */
	public void removeOutputComponent(final Component outputComponent) {

		if (outputComponents != null && outputComponent != null && outputComponents.contains(outputComponent)) {

			outputComponents.remove(outputComponent);

			outputComponent.removeInputComponent(this);
		}
	}

	/**
	 * Gets the function that is instantiated by this component.
	 *
	 * @return the function that is instantiated by this component
	 */
	public Function getFunction() {

		return function;
	}

	/**
	 * Sets the function that should be instantiated by this component.
	 *
	 * @param functionArg the function that should be instantiated by this component
	 */
	public void setFunction(final Function functionArg) {

		function = functionArg;
	}

	/**
	 * Gets the parameter mappings map to process this function instantiation.
	 *
	 * @return the parameter mappings map
	 */
	@XmlElement(name = "parameter_mappings")
	public Map<String, String> getParameterMappings() {

		initParameterMappings(false);

		return parameterMappings;
	}

	/**
	 * Sets the parameters mappings map to be able to process this function instantiation.
	 *
	 * @param parameterMappingsArg a new parameter mappings map
	 */
	public void setParameterMappings(final Map<String, String> parameterMappingsArg) {

		if (parameterMappingsArg == null && parameterMappings != null) {

			parameterMappings.clear();
		}

		if (parameterMappingsArg != null) {

			if (parameterMappings == null) {

				parameterMappings = Maps.newLinkedHashMap();
			}

			if (!parameterMappings.equals(parameterMappingsArg)) {

				parameterMappings.clear();
				parameterMappings.putAll(parameterMappingsArg);
			}
		}

		refreshParameterMappingsString();
	}

	/**
	 * Adds a new parameter mapping to the parameter mappings collection.
	 *
	 * @param keyParameter   the key of the parameter mapping
	 * @param valueParameter the value fo the parameter mapping
	 */
	public void addParameterMapping(final String keyParameter, final String valueParameter) {

		if (keyParameter != null && valueParameter != null) {

			if (parameterMappings == null) {

				initParameterMappings(false);

				if (null == parameterMappings) {

					parameterMappings = Maps.newLinkedHashMap();
				}
			}

			parameterMappings.put(keyParameter, valueParameter);

			refreshParameterMappingsString();
		}
	}

	// /**
	// * Gets the related transformation.
	// *
	// * @return the related transformation
	// */
	// public Transformation getTransformation() {
	//
	// return transformation;
	// }
	//
	// /**
	// * Sets the related transformation.
	// *
	// * @param transformationArg the related transformation
	// */
	// public void setTransformation(final Transformation transformationArg) {
	//
	// if (transformationArg == null && transformation != null) {
	//
	// // remove component from transformation when component, will be prepared for removal
	//
	// transformation.removeComponent(this);
	// }
	//
	// transformation = transformationArg;
	//
	// if (transformationArg != null) {
	//
	// transformationArg.addComponent(this);
	// }
	// }

	/**
	 * Refreshs the string that holds the serialised JSON object of the parameter mappings map. This method should be called after
	 * every manipulation of the parameter mappings map (to keep the states consistent).
	 */
	private void refreshParameterMappingsString() {

		if (parameterMappings != null) {

			parameterMappingsJSON = new ObjectNode(DMPPersistenceUtil.getJSONFactory());

			for (final Entry<String, String> paremeterMappingEntry : parameterMappings.entrySet()) {

				parameterMappingsJSON.put(paremeterMappingEntry.getKey(), paremeterMappingEntry.getValue());
			}
		}

		if (null != parameterMappingsJSON && parameterMappingsJSON.size() > 0) {

			parameterMappingsString = parameterMappingsJSON.toString().getBytes(Charsets.UTF_8);
		} else {

			parameterMappingsString = null;
		}
	}

	/**
	 * Initialises the parameter mappings map and JSON object from the string that holds the serialised JSON object of the
	 * parameter mappings map.
	 *
	 * @param fromScratch flag that indicates, whether the parameter mappings should be initialised from scratch or not
	 */
	private void initParameterMappings(final boolean fromScratch) {

		if (parameterMappingsJSON == null && !parameterMappingsInitialized) {

			if (parameterMappingsString == null) {

				Component.LOG.debug("parameter mappings JSON is null for '" + getUuid() + "'");

				if (fromScratch) {

					parameterMappingsJSON = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
					parameterMappings = Maps.newLinkedHashMap();
				}

				parameterMappingsInitialized = true;

				return;
			}

			try {

				parameterMappings = Maps.newLinkedHashMap();

				parameterMappingsJSON = DMPPersistenceUtil.getJSON(StringUtils.toEncodedString(parameterMappingsString, Charsets.UTF_8));

				if (null != parameterMappingsJSON) {

					final Iterator<Entry<String, JsonNode>> iter = parameterMappingsJSON.fields();

					while (iter.hasNext()) {

						final Entry<String, JsonNode> entry = iter.next();

						final String key = entry.getKey();
						final JsonNode valueNode = entry.getValue();

						if (valueNode == null) {

							Component.LOG.debug("value for key '" + key + "' in  parameter mappings JSON for component '" + getUuid() + "' is null");

							continue;
						}

						final String value = valueNode.asText();

						parameterMappings.put(key, value);
					}
				}
			} catch (final DMPException e) {

				Component.LOG.debug("couldn't parse parameter mappings JSON for component '" + getUuid() + "'");
			}

			parameterMappingsInitialized = true;
		}
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Component.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((Component) obj).getParameterMappings(), getParameterMappings())
				&& DMPPersistenceUtil.getFunctionUtils().completeEquals(((Component) obj).getFunction(), getFunction())
				&& Objects.equal(((Component) obj).getInputComponents(), getInputComponents())
				&& Objects.equal(((Component) obj).getOutputComponents(), getOutputComponents());
	}

	/**
	 * Create a new {@code Component} as a copy from a existing component with a specific id. <br>
	 * <b>Use with care!</b>
	 * <p>
	 * This factory is to be used by {@link org.dswarm.persistence.model.job.utils.TransformationDeserializer} to avoid reflection
	 * based access to a private/protected field, since the Json deserializer needs a way to set the id that was provided by the
	 * JSON.
	 * </p>
	 * <p>
	 * The id is otherwise assigned by the database/Hibernate layer. You should never need this outside of
	 * {@code TransformationDeserializer}.
	 * </p>
	 *
	 * @param component the base component that will be copied
	 * @param uuid      the target component's id value
	 * @return a new component with the given id and all other attributes copied from the provided component.
	 */
	public static Component withId(final Component component, final String uuid) {
		final Component newComponent = new Component(uuid);

		newComponent.setFunction(component.getFunction());
		newComponent.setInputComponents(component.getInputComponents());
		newComponent.setOutputComponents(component.getOutputComponents());
		newComponent.setParameterMappings(component.getParameterMappings());
		newComponent.setDescription(component.getDescription());
		newComponent.setName(component.getName());

		return newComponent;
	}
}
