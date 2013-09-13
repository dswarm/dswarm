package de.avgl.dmp.persistence.model.resource;

import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.model.DMPJPAObject;

@XmlRootElement
@Entity
@Table(name = "RESOURCE")
public class Resource extends DMPJPAObject {

	/**
	 * 
	 */
	private static final long						serialVersionUID		= 1L;

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(Resource.class);

	@Column(name = "NAME")
	private String									name					= null;

	/**
	 * The type of the resource.
	 */
	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	private ResourceType							type;

	@Column(name = "DESCRIPTION")
	private String									description				= null;

	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "attributes", columnDefinition = "CLOB")
	private String									attributesString;

	@Transient
	private ObjectNode								attributes;

	@Transient
	private boolean									attributesInitialized	= false;

	/**
	 * All configurations of the resource.
	 */
	// TODO set correct casacade type
	@OneToMany(mappedBy = "resource", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Configuration>						configurations;

	public String getName() {

		return name;
	}

	public void setName(final String name) {

		this.name = name;
	}

	public ResourceType getType() {

		return type;
	}

	public void setType(final ResourceType type) {

		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {

		this.description = description;
	}

	public ObjectNode getAttributes() {

		if (attributes == null && attributesInitialized == false) {

			try {

				attributes = DMPUtil.getJSON(attributesString);
			} catch (DMPException e) {

				LOG.debug("couldn't parse attributes JSON string for resource '" + getId() + "'");
			}

			attributesInitialized = true;
		}

		return attributes;
	}

	public void setAttributes(final ObjectNode attributes) {

		this.attributes = attributes;

		refreshAttributesString();
	}

	public void addAttribute(final String key, final JsonNode value) {

		if (attributes == null) {

			attributes = new ObjectNode(DMPUtil.getJSONFactory());
		}

		attributes.set(key, value);

		refreshAttributesString();
	}

	public JsonNode getAttribute(final String key) {

		if (attributes == null) {

			LOG.debug("attributes JSON is null");

			return null;
		}

		return attributes.get(key);
	}

	/**
	 * Gets all configurations of the resource.
	 * 
	 * @return all configurations of the resource
	 */
	public Set<Configuration> getConfigurations() {

		return configurations;
	}

	/**
	 * Sets all configurations of the resource.
	 * 
	 * @param configurationsArg all configurations of the resource
	 */
	public void setConfigurations(final Set<Configuration> configurationsArg) {

		this.configurations = configurationsArg;
	}

	/**
	 * Adds a new configuration to the collection of configurations of this resource.<br>
	 * Created by: tgaengler
	 * 
	 * @param configuration a new export definition revision
	 */
	public void addConfiguration(final Configuration configuration) {

		if (configuration != null) {

			if (configurations == null) {

				configurations = Sets.newLinkedHashSet();
			}

			if (!configurations.contains(configuration)) {

				configurations.add(configuration);
			}

			if (configuration.getResource() == null) {

				configuration.setResource(this);
			}
		}
	}

	/**
	 * Replaces an existing configuration, i.e., the configuration with the same identifier will be replaced.<br>
	 * Created by: tgaengler
	 * 
	 * @param configuration an existing, updated configuration
	 */
	public void replaceConfiguration(final Configuration configuration) {

		if (configuration != null) {

			if (configurations == null) {

				configurations = Sets.newLinkedHashSet();
			}

			if (configurations.contains(configuration)) {

				configurations.remove(configuration);
			}

			configurations.add(configuration);

			if (configuration.getResource() == null) {

				configuration.setResource(this);
			}
		}
	}

	/**
	 * Removes an existing configuration from the collection of configurations of this export resource.<br>
	 * Created by: tgaengler
	 * 
	 * @param configuration an existing configuration that should be removed
	 */
	public void removeConfiguration(final Configuration configuration) {

		if (configurations != null && configuration != null && configurations.contains(configuration)) {

			configurations.remove(configuration);

			if (configuration.getResource() != null) {

				configuration.setResource(null);
			}
		}
	}

	private void refreshAttributesString() {

		attributesString = attributes.toString();
	}
}
