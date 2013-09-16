package de.avgl.dmp.persistence.model.resource;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.model.DMPJPAObject;

@Entity
@Table(name = "CONFIGURATION")
public class Configuration extends DMPJPAObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(Configuration.class);

	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "parameters", columnDefinition = "CLOB")
	private String									parametersString;

	@Transient
	private ObjectNode								parameters;

	@Transient
	private boolean									parametersInitialized	= false;

	/**
	 * The related resource.
	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "RESOURCE")
	private Resource								resource;

	public ObjectNode getParameters() {

		if (parameters == null && parametersInitialized == false) {

			try {

				parameters = DMPUtil.getJSON(parametersString);
			} catch (DMPException e) {

				LOG.debug("couldn't parse parameters JSON string for resource '" + getId() + "'");
			}

			parametersInitialized = true;
		}

		return parameters;
	}

	public void setParameters(final ObjectNode parameters) {

		this.parameters = parameters;

		refreshParametersString();
	}

	public void addParameter(final String key, final JsonNode value) {

		if (parameters == null) {

			parameters = new ObjectNode(DMPUtil.getJSONFactory());
		}

		parameters.set(key, value);

		refreshParametersString();
	}

	public JsonNode getParameter(final String key) {

		if (parameters == null) {

			LOG.debug("attributes JSON is null");

			return null;
		}

		return parameters.get(key);
	}

	public Resource getResource() {

		return resource;
	}

	public void setResource(final Resource resourceArg) {

		if (resourceArg == null && resource != null) {

			// remove configuration from resource, if configuration, will be prepared for removal

			resource.removeConfiguration(this);
		}

		resource = resourceArg;

		if (resourceArg != null) {

			resource.addConfiguration(this);
		}
	}

	private void refreshParametersString() {

		parametersString = parameters.toString();
	}
}
