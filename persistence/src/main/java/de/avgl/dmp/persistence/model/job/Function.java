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
public class Function extends BasicDMPJPAObject {

	/**
	 * 
	 */
	private static final long						serialVersionUID		= 1L;

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(AttributePath.class);

	@Column(name = "DESCRIPTION", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									description				= null;

	@Transient
	private LinkedList<String>						parameters				= null;

	@Transient
	private ArrayNode								parametersJSON;

	@Transient
	private boolean									parametersInitialized	= false;

	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "ATTRIBUTE_PATH", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									parametersString		= null;

	public String getDescription() {

		return description;
	}

	public void setDescription(final String description) {

		this.description = description;
	}

	@XmlElement(name = "parameters")
	public LinkedList<String> getParameters() {

		initParameters(false);

		return parameters;
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
				}

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
}
