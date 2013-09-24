package de.avgl.dmp.persistence.model.resource;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@XmlRootElement
@Entity
//@Cacheable(true)
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
	@Column(name = "attributes", columnDefinition = "VARCHAR(4000)", length=4000)
	private String									attributesString;

	@Transient
	private ObjectNode								attributes;

	@Transient
	private boolean									attributesInitialized	= false;

	/**
	 * All configurations of the resource.
	 */
	// TODO set correct casacade type
	@ManyToMany(mappedBy = "resources", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	// @JsonSerialize(using = ConfigurationReferenceSerializer.class)
	// @JsonDeserialize(using = ConfigurationReferenceDeserializer.class)
	@XmlIDREF
	@XmlList
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

				attributes = DMPPersistenceUtil.getJSON(attributesString);
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

			attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
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

		if (configurationsArg == null && configurations != null) {

			// remove resource from configurations, if resource, will be prepared for removal

			for (final Configuration configuration : configurations) {

				configuration.removeResource(this);
			}
		}

		configurations = configurationsArg;

		if (configurationsArg != null) {

			for (final Configuration configuration : configurationsArg) {

				configuration.addResource(this);
			}
		}
	}

	public Configuration getConfiguration(final Long id) {

		if (id == null) {

			return null;
		}

		if (this.configurations == null || this.configurations.isEmpty()) {

			return null;
		}

		final List<Configuration> configurationsFiltered = filter(having(on(Configuration.class).getId(), equalTo(id)), this.configurations);

		if (configurationsFiltered == null || configurationsFiltered.isEmpty()) {

			return null;
		}

		return configurationsFiltered.get(0);
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
				configuration.addResource(this);
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

			configuration.removeResource(this);
			configuration.addResource(this);
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

			configuration.removeResource(this);
		}
	}

	private void refreshAttributesString() {

		attributesString = attributes.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		
		if (!Resource.class.isInstance(obj)) {

			return false;
		}
		
		return super.equals(obj);
	}
}
