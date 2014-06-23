package de.avgl.dmp.persistence.model.job;

import java.util.LinkedList;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * A function is a method that can be executed on data via a {@link Job} execution (i.e. a {@link Task}). A function mainly
 * consists of a collection of parameters and a machine processable function description. Complex functions are
 * {@link Transformation}s.
 * 
 * @author tgaengler
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true, defaultImpl = Function.class)
@JsonSubTypes({ @Type(value = Function.class, name = "Function"), @Type(value = Transformation.class, name = "Transformation") })
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "FUNCTION_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Function")
@Table(name = "FUNCTION")
public class Function extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	private static final Logger	LOG					= LoggerFactory.getLogger(Function.class);

	/**
	 * A string that holds the serialised JSON object of a function description.
	 */
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "FUNCTION_DESCRIPTION", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String				functionDescriptionString;

	/**
	 * A function description as JSON object.
	 */
	@Transient
	private ObjectNode			functionDescription;

	/**
	 * A flag that indicates, whether the function description is initialised or not.
	 */
	@Transient
	private boolean				functionDescriptionInitialized;

	/**
	 * A list of parameters.
	 */
	@Transient
	private LinkedList<String>	parameters;

	/**
	 * A JSON array of the parameter list.
	 */
	@Transient
	private ArrayNode			parametersJSON;

	/**
	 * A flag that indicates, whether the parameters are initialized or not.
	 */
	@Transient
	private boolean				parametersInitialized;

	/**
	 * A string that hold the serialised JSON object of the parameters.
	 */
	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "PARAMETERS", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String				parametersString;

	/**
	 * The function type, e.g., function ({@link FunctionType#Function}) or transformation ({@link FunctionType#Transformation}).
	 */
	@XmlElement(name = "type")
	// -> note: separate attribute for entity type is not necessary since Jackson will include this
	// property automatically, when serialising the object
	// however, it needs to be enabled - otherwise on could not serialise a JSON string to a POJO object
	// @JsonIgnore
	@Column(name = "FUNCTION_TYPE")
	@Enumerated(EnumType.STRING)
	private final FunctionType	functionType;

	/**
	 * Creates a new function.
	 */
	public Function() {

		functionType = FunctionType.Function;
	}

	/**
	 * Creates a new function with the given function type, i.e. function or transformation.
	 * 
	 * @param functionTypeArg the type of the function
	 */
	public Function(final FunctionType functionTypeArg) {

		functionType = functionTypeArg;
	}

	/**
	 * Gets the parameters of the function.
	 * 
	 * @return the parameters of the function
	 */
	@XmlElement(name = "parameters")
	public LinkedList<String> getParameters() {

		initParameters(false);

		return parameters;
	}

	/**
	 * Sets the parameters of the function.
	 * 
	 * @param parametersArg new parameters of the function
	 */
	@XmlElement(name = "parameters")
	public void setParameters(final LinkedList<String> parametersArg) {

		if (parametersArg == null && parameters != null) {

			parameters.clear();
		}

		if (parametersArg != null) {

			if (parameters == null) {

				parameters = Lists.newLinkedList();
			}

			if (!parameters.equals(parametersArg)) {

				parameters.clear();
				parameters.addAll(parametersArg);
			}
		}

		refreshParametersString();
	}

	/**
	 * Adds a new parameter to the parameters lists of the function
	 * 
	 * @param parameter a new parameter
	 */
	public void addParameter(final String parameter) {

		if (parameter != null) {

			if (parameters == null) {

				initParameters(true);

				if (null == parameters) {

					parameters = Lists.newLinkedList();
				}
			}

			parameters.add(parameter);

			refreshParametersString();
		}
	}

	/**
	 * Gets the machine processable function description
	 * 
	 * @return the machine processable function description
	 */
	@XmlElement(name = "function_description")
	public ObjectNode getFunctionDescription() {

		initFunctionDescription(false);

		return functionDescription;
	}

	/**
	 * Sets the machine processable function description
	 * 
	 * @param functionDescriptionArg a new machine processable function description
	 */
	public void setFunctionDescription(final ObjectNode functionDescriptionArg) {

		functionDescription = functionDescriptionArg;

		refreshFunctionDescriptionString();
	}

	/**
	 * Gets the function type, e.g., function ({@link FunctionType#Function}) or transformation (
	 * {@link FunctionType#Transformation}).
	 * 
	 * @return the function type
	 */
	public FunctionType getFunctionType() {

		return functionType;
	}

	@Override
	public boolean equals(final Object obj) {

		return Function.class.isInstance(obj) && super.equals(obj);

	}

	/**
	 * Refreshs the string that holds the serialised JSON object of the parameters list. This method should be called after every
	 * manipulation of the parameters list (to keep the states consistent).
	 */
	private void refreshParametersString() {

		if (parameters != null) {

			parametersJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

			for (final String parameter : parameters) {

				parametersJSON.add(parameter);
			}
		}

		if (null != parametersJSON && parametersJSON.size() > 0) {

			parametersString = parametersJSON.toString();
		} else {

			parametersString = null;
		}
	}

	/**
	 * Initialises the parameters list and JSON object from the string that holds the serialised JSON object of the parameters
	 * list.
	 * 
	 * @param fromScratch flag that indicates, whether the parameters should be initialised from scratch or not
	 */
	private void initParameters(final boolean fromScratch) {

		if (parametersJSON == null && !parametersInitialized) {

			if (parametersString == null) {

				Function.LOG.debug("parameters JSON is null for '" + getId() + "'");

				if (fromScratch) {

					parametersJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
					parameters = Lists.newLinkedList();
				}

				parametersInitialized = true;

				return;
			}

			try {

				parameters = Lists.newLinkedList();

				// parse parameters string
				parametersJSON = DMPPersistenceUtil.getJSONArray(parametersString);

				if (null != parametersJSON) {

					for (final JsonNode parameterNode : parametersJSON) {

						final String parameter = parameterNode.asText();

						if (null != parameter) {

							parameters.add(parameter);
						}
					}
				}
			} catch (final DMPException e) {

				Function.LOG.debug("couldn't parse parameters JSON for function '" + getId() + "'");
			}

			parametersInitialized = true;
		}
	}

	/**
	 * Refreshs the string that holds the serialised JSON object of the function description. This method should be called after
	 * every manipulation of the function description (to keep the states consistent).
	 */
	private void refreshFunctionDescriptionString() {

		if (functionDescription == null) {

			functionDescriptionString = null;

			return;
		}

		functionDescriptionString = functionDescription.toString();
	}

	/**
	 * Initialises the function description from the string that holds the serialised JSON object of the function description.
	 * 
	 * @param fromScratch flag that indicates, whether the function description should be initialised from scratch or not
	 */
	private void initFunctionDescription(final boolean fromScratch) {

		if (functionDescription == null && !functionDescriptionInitialized) {

			if (functionDescriptionString == null) {

				Function.LOG.debug("function description JSON string is null");

				if (fromScratch) {

					functionDescription = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
				}

				functionDescriptionInitialized = true;

				return;
			}

			try {

				functionDescription = DMPPersistenceUtil.getJSON(functionDescriptionString);
			} catch (final DMPException e) {

				Function.LOG.debug("couldn't parse function description JSON string for function '" + getId() + "'");
			}

			functionDescriptionInitialized = true;
		}
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Function.class.isInstance(obj) && super.completeEquals(obj) && Objects.equal(((Function) obj).getFunctionType(), getFunctionType())
				&& Objects.equal(((Function) obj).getParameters(), getParameters())
				&& Objects.equal(((Function) obj).getFunctionDescription(), getFunctionDescription());
	}
}
