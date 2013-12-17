package de.avgl.dmp.persistence.model.resource;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
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
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.ApiModel;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@ApiModel("A data resource, e.g., an XML or CSV document, a SQL database, or an RDF graph. A data resource can consist of several records.")
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "RESOURCE")
public class Resource extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long						serialVersionUID	= 1L;

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(Resource.class);

	/**
	 * The type of the resource.
	 */
	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	private ResourceType							type;

	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "attributes", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									attributesString;

	@Transient
	private ObjectNode								attributes;

	@Transient
	private boolean									attributesInitialized;

	/**
	 * All configurations of the resource.
	 */
	// TODO set correct casacade type
	@ManyToMany(mappedBy = "resources", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	// @JsonSerialize(using = ConfigurationReferenceSerializer.class)
	// @JsonDeserialize(using = ConfigurationReferenceDeserializer.class)
	@XmlIDREF
	@XmlList
	// @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Set<Configuration>						configurations;

	public ResourceType getType() {

		return type;
	}

	public void setType(final ResourceType type) {

		this.type = type;
	}

	public ObjectNode getAttributes() {

		initAttributes(false);

		return attributes;
	}

	public void setAttributes(final ObjectNode attributes) {

		this.attributes = attributes;

		refreshAttributesString();
	}

	public void addAttribute(final String key, final String value) {

		if (attributes == null) {

			initAttributes(true);
		}

		attributes.put(key, value);

		refreshAttributesString();
	}

	public JsonNode getAttribute(final String key) {

		initAttributes(false);

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

		if (configurationsArg == null && configurations != null) {

			// remove resource from configurations, if resource, will be prepared for removal

			final Set<Configuration> configurationsToBeDeleted = Sets.newCopyOnWriteArraySet(configurations);

			for (final Configuration configuration : configurationsToBeDeleted) {

				configuration.removeResource(this);
			}

			configurations.clear();
		}

		if (configurationsArg != null) {

			if (!configurationsArg.equals(configurations)) {

				if (configurations == null) {

					configurations = Sets.newCopyOnWriteArraySet();
				}

				configurations.clear();
				configurations.addAll(configurationsArg);
			}

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

				configurations = Sets.newCopyOnWriteArraySet();
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

				configurations = Sets.newCopyOnWriteArraySet();
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

		if (attributes != null) {

			attributesString = attributes.toString();
		}
	}

	private void initAttributes(final boolean fromScratch) {

		if (attributes == null && !attributesInitialized) {

			if (attributesString == null) {

				LOG.debug("attributes JSON string is null");

				if (fromScratch) {

					attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
				}

				attributesInitialized = true;

				return;
			}

			try {

				attributes = DMPPersistenceUtil.getJSON(attributesString);
			} catch (final DMPException e) {

				LOG.debug("couldn't parse attributes JSON string for resource '" + getId() + "'");
			}

			attributesInitialized = true;
		}
	}

	@Override
	public boolean equals(final Object obj) {

		return Resource.class.isInstance(obj) && super.equals(obj);

	}
}
