package de.avgl.dmp.persistence.model.job;

import java.util.LinkedList;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "FUNCTION")
public class Function extends ExtendedBasicDMPJPAObject {

	/**
	 * 
	 */
	private static final long						serialVersionUID		= 1L;

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(AttributePath.class);

	@Column(name = "FUNCTIONDESCRIPTION")
	private String									functionDescription		= null;
	
	@Transient
	private LinkedList<String>						parameters				= null;

	@Transient
	private ArrayNode								parametersJSON;

	@Transient
	private boolean									parametersInitialized	= false;

	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "PARAMETERS", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									parametersString		= null;

	@XmlElement(name = "parameters")
	public LinkedList<String> getParameters() {

		initParameters(false);

		return parameters;
	}
	
	public void setFunctionDescription(final String functionDescription) {
		this.functionDescription = functionDescription;
	}
	
	public String getFunctionDescription() {
		return functionDescription;
	}

	public void setParameters(final LinkedList<String> parametersArg) {

		parameters = parametersArg;

		refreshParametersString();
	}

	public void addParameter(final String parameter) {

		if (parameter != null) {

			if (parameters == null) {

				initParameters(false);

				if (null == parameters) {

					parameters = Lists.newLinkedList();
				}
			}

			parameters.add(parameter);

			refreshParametersString();
		}
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

			if (parameters == null) {

				Function.LOG.debug("parameters JSON is null for '" + getId() + "'");

				if (fromScratch) {

					parametersJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
					parameters = Lists.newLinkedList();

					parametersInitialized = true;

					return;
				}
			}

			parameters = Lists.newLinkedList();

			if (parametersString != null) {

				try {

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
			}

			parametersInitialized = true;
		}
	}
}
