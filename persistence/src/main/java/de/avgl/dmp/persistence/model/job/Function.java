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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "FUNCTION_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Function")
@Table(name = "FUNCTION")
@JsonIgnoreProperties({ "functionDescription" })
public class Function extends ExtendedBasicDMPJPAObject {

	/**
	 * 
	 */
	private static final long						serialVersionUID				= 1L;

	private static final org.apache.log4j.Logger	LOG								= org.apache.log4j.Logger.getLogger(AttributePath.class);

	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "FUNCTION_DESCRIPTION", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									functionDescriptionString;

	@Transient
	private ObjectNode								functionDescription;

	@Transient
	private boolean									functionDescriptionInitialized	= false;

	@Transient
	private LinkedList<String>						parameters						= null;

	@Transient
	private ArrayNode								parametersJSON;

	@Transient
	private boolean									parametersInitialized			= false;

	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "PARAMETERS", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									parametersString				= null;

	/**
	 * The function type, e.g., function ({@link FunctionType#Function}) or transformation ({@link FunctionType#Transformation}).
	 */
	@XmlElement(name = "type")
	@Column(name = "FUNCTION_TYPE")
	@Enumerated(EnumType.STRING)
	protected final FunctionType					functionType;

	public Function() {

		functionType = FunctionType.Function;
	}

	public Function(final FunctionType functionTypeArg) {

		functionType = functionTypeArg;
	}

	@XmlElement(name = "parameters")
	public LinkedList<String> getParameters() {

		initParameters(false);

		return parameters;
	}

	@XmlElement(name = "function_description")
	public ObjectNode getFunctionDescription() {

		initFunctionDescription(false);

		return functionDescription;
	}

	public void setFunctionDescription(final ObjectNode functionDescriptionArg) {

		functionDescription = functionDescriptionArg;

		refreshFunctionDescriptionString();
	}

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

		if (!Function.class.isInstance(obj)) {

			return false;
		}

		return super.equals(obj);
	}

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

	private void refreshFunctionDescriptionString() {

		functionDescriptionString = functionDescription.toString();
	}

	private void initFunctionDescription(boolean fromScratch) {

		if (functionDescription == null && !functionDescriptionInitialized) {

			if (functionDescriptionString == null) {

				LOG.debug("function description JSON string is null");

				if (fromScratch) {

					functionDescription = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
				}

				functionDescriptionInitialized = true;

				return;
			}

			try {

				functionDescription = DMPPersistenceUtil.getJSON(functionDescriptionString);
			} catch (DMPException e) {

				LOG.debug("couldn't parse function description JSON string for function '" + getId() + "'");
			}

			functionDescriptionInitialized = true;
		}
	}
}
