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
package org.dswarm.persistence.model.resource;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import ch.lambdaj.Lambda;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.ApiModel;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.DMPException;
import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.representation.ConfigurationSetReferenceDeserializer;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A data resource describes attributes of a specific amount of data. A data resource can be, e.g., an XML or CSV document, a SQL
 * database, or an RDF graph. A data resource can consist of several records.
 *
 * @author tgaengler
 */
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
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(Resource.class);

	/**
	 * The type of the resource, e.g., file.
	 */
	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	private ResourceType type;

	/**
	 * A string that holds the serialised JSON object for attributes.
	 */
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "ATTRIBUTES", columnDefinition = "BLOB")
	private byte[] attributesString;

	/**
	 * A JSON object for attributes.
	 */
	@Transient
	private ObjectNode attributes;

	/**
	 * A flag that indicates, whether the attributes are initialised or not.
	 */
	@Transient
	private boolean attributesInitialized;

	/**
	 * All configurations of the resource.
	 */
	// TODO set correct casacade type
	@ManyToMany(mappedBy = "resources", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	// @JsonSerialize(using = ConfigurationReferenceSerializer.class)
	@JsonDeserialize(using = ConfigurationSetReferenceDeserializer.class)
	@XmlIDREF
	@XmlList
	// @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Set<Configuration> configurations;

	public Resource(final String uuidArg) {

		super(uuidArg);
	}

	protected Resource() {

	}

	/**
	 * Gets the resource type.
	 *
	 * @return the resource type
	 */
	public ResourceType getType() {

		return type;
	}

	/**
	 * Sets the resource type
	 *
	 * @param type a new resource type
	 */
	public void setType(final ResourceType type) {

		this.type = type;
	}

	/**
	 * Gets all attributes of the data resource.
	 *
	 * @return all attributes of the data resource
	 */
	@XmlElement(name = "resource_attributes")
	public ObjectNode getAttributes() {

		initAttributes(false);

		return attributes;
	}

	/**
	 * Sets the attributes of the data resource.
	 *
	 * @param attributes new attributes
	 */
	@XmlElement(name = "resource_attributes")
	public void setAttributes(final ObjectNode attributes) {

		this.attributes = attributes;

		refreshAttributesString();
	}

	/**
	 * Adds a new attribute to the attributes of the data resource.
	 *
	 * @param key   the key of the attribute
	 * @param value the value of the attribute
	 */
	public void addAttribute(final String key, final String value) {

		if (attributes == null) {

			initAttributes(true);
		}

		attributes.put(key, value);

		refreshAttributesString();
	}

	/**
	 * Gets a specific attribute by the given attribute key.
	 *
	 * @param key an attribute key
	 * @return the value of the matched attribute or null.
	 */
	public JsonNode getAttribute(final String key) {

		initAttributes(false);

		if (attributes == null) {

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

		if (configurationsArg == null && configurations != null) {

			// remove resource from configurations, if resource, will be prepared for removal

			final Set<Configuration> configurationsToBeDeleted = Sets.newCopyOnWriteArraySet(configurations);

			for (final Configuration configuration : configurationsToBeDeleted) {

				configuration.removeResource(this);
			}

			configurations.clear();
		}

		if (configurationsArg != null) {

			if (!DMPPersistenceUtil.getConfigurationUtils().completeEquals(configurations, configurationsArg)) {

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

	/**
	 * Gets the configuration by the given identifier.
	 *
	 * @param uuid a configuration identifier
	 * @return the matched configuration or null
	 */
	public Configuration getConfiguration(final String uuid) {

		if (uuid == null) {

			return null;
		}

		if (configurations == null || configurations.isEmpty()) {

			return null;
		}

		final List<Configuration> configurationsFiltered = Lambda
				.filter(Lambda.having(Lambda.on(Configuration.class).getUuid(), Matchers.equalTo(uuid)),
						configurations);

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

	/**
	 * Refreshs the string that holds the serialised JSON object of the attributes. This method should be called after every
	 * manipulation of the attributes (to keep the states consistent).
	 */
	private void refreshAttributesString() {

		if (attributes != null) {

			attributesString = attributes.toString().getBytes(Charsets.UTF_8);
		}
	}

	/**
	 * Initialises the attributes from the string that holds the serialised JSON object of the attributes.
	 *
	 * @param fromScratch flag that indicates, whether the attributes should be initialised from scratch or not
	 */
	private void initAttributes(final boolean fromScratch) {

		if (attributes == null && !attributesInitialized) {

			if (attributesString == null) {

				Resource.LOG.debug("attributes JSON string is null");

				if (fromScratch) {

					attributes = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
				}

				attributesInitialized = true;

				return;
			}

			try {

				attributes = DMPPersistenceUtil.getJSON(StringUtils.toEncodedString(attributesString, Charsets.UTF_8));
			} catch (final DMPException e) {

				Resource.LOG.debug("couldn't parse attributes JSON string for resource '" + getUuid() + "'");
			}

			attributesInitialized = true;
		}
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Resource.class.isInstance(obj) && super.completeEquals(obj) && Objects.equal(((Resource) obj).getType(), getType())
				&& Objects.equal(((Resource) obj).getAttributes(), getAttributes())
				&& Objects.equal(((Resource) obj).getConfigurations(), getConfigurations());
	}
}
