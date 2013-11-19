package de.avgl.dmp.persistence.model.job;

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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.utils.DMPUUIDObjectReferenceSerializer;
import de.avgl.dmp.persistence.model.utils.SetComponentReferenceSerializer;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "COMPONENT")
public class Component extends BasicDMPJPAObject {

	/**
	 * 
	 */
	private static final long						serialVersionUID				= 1L;

	private static final org.apache.log4j.Logger	LOG								= org.apache.log4j.Logger.getLogger(Component.class);

	@ManyToMany(mappedBy = "outputComponents", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	@XmlElement(name = "input_components")
	@JsonSerialize(using = SetComponentReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlIDREF
	@XmlList
	private Set<Component>							inputComponents					= null;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "OUTPUT_COMPONENTS_INPUT_COMPONENTS", joinColumns = { @JoinColumn(name = "INPUT_COMPONENT_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "OUTPUT_COMPONENT_ID", referencedColumnName = "ID") })
	@XmlElement(name = "output_components")
	@JsonSerialize(using = SetComponentReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@XmlIDREF
	@XmlList
	private Set<Component>							outputComponents				= null;

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "FUNCTION")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonSerialize(using = DMPUUIDObjectReferenceSerializer.class)
	@XmlIDREF
	private Function								function						= null;

	@Transient
	private Map<String, String>						parameterMappings				= null;

	@Transient
	private ObjectNode								parameterMappingsJSON;

	@Transient
	private boolean									parameterMappingsInitialized	= false;

	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "PARAMETER_MAPPINGS", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									parameterMappingsString			= null;

	public Set<Component> getInputComponents() {

		return inputComponents;
	}

	public void setInputComponents(final Set<Component> inputComponentsArg) {

		if (inputComponentsArg == null && inputComponents != null) {

			// remove component from input components, if component will be prepared for removal

			for (final Component inputComponent : inputComponents) {

				inputComponent.removeOutputComponent(this);
			}
		}

		inputComponents = inputComponentsArg;

		if (inputComponentsArg != null) {

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

				inputComponents = Sets.newLinkedHashSet();
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

	public Set<Component> getOutputComponents() {

		return outputComponents;
	}

	public void setOutputComponents(final Set<Component> outputComponentsArg) {

		if (outputComponentsArg == null && outputComponents != null) {

			// remove component from output component, if component will be prepared for removal

			for (final Component outputComponent : outputComponents) {

				outputComponent.removeInputComponent(this);
			}
		}

		outputComponents = outputComponentsArg;

		if (outputComponentsArg != null) {

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

				outputComponents = Sets.newLinkedHashSet();
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

	public Function getFunction() {

		return function;
	}

	public void setFunction(final Function functionArg) {

		function = functionArg;
	}

	@XmlElement(name = "parameter_mappings")
	public Map<String, String> getParameterMappings() {

		initParameterMappings(false);

		return parameterMappings;
	}

	public void setParameterMapping(final Map<String, String> parameterMappingsArg) {

		parameterMappings = parameterMappingsArg;

		refreshParameterMappingsString();
	}

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

	@Override
	public boolean equals(final Object obj) {

		if (!Component.class.isInstance(obj)) {

			return false;
		}

		return super.equals(obj);
	}

	private void refreshParameterMappingsString() {

		if (parameterMappings != null) {

			parameterMappingsJSON = new ObjectNode(DMPPersistenceUtil.getJSONFactory());

			for (final Entry<String, String> paremeterMappingEntry : parameterMappings.entrySet()) {

				parameterMappingsJSON.put(paremeterMappingEntry.getKey(), paremeterMappingEntry.getValue());
			}
		}

		if (null != parameterMappingsJSON && parameterMappingsJSON.size() > 0) {

			parameterMappingsString = parameterMappingsJSON.toString();
		} else {

			parameterMappingsString = null;
		}
	}

	private void initParameterMappings(final boolean fromScratch) {

		if (parameterMappingsJSON == null && !parameterMappingsInitialized) {

			if (parameterMappingsString == null) {

				Component.LOG.debug("parameter mappings JSON is null for '" + getId() + "'");

				if (fromScratch) {

					parameterMappingsJSON = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
					parameterMappings = Maps.newLinkedHashMap();

					parameterMappingsInitialized = true;
				}

				return;
			}

			try {

				parameterMappings = Maps.newLinkedHashMap();

				parameterMappingsJSON = DMPPersistenceUtil.getJSON(parameterMappingsString);

				if (null != parameterMappingsJSON) {

					Iterator<Entry<String, JsonNode>> iter = parameterMappingsJSON.fields();

					while (iter.hasNext()) {

						final Entry<String, JsonNode> entry = iter.next();

						final String key = entry.getKey();
						final JsonNode valueNode = entry.getValue();

						if (valueNode == null) {

							Component.LOG.debug("value for key '" + key + "' in  parameter mappings JSON for component '" + getId() + "' is null");

							continue;
						}

						final String value = valueNode.asText();

						parameterMappings.put(key, value);
					}
				}
			} catch (final DMPException e) {

				Component.LOG.debug("couldn't parse parameter mappings JSON for component '" + getId() + "'");
			}

			parameterMappingsInitialized = true;
		}
	}
}
