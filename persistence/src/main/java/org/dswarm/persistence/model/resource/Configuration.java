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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.DMPException;
import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.representation.ResourceSetReferenceDeserializer;
import org.dswarm.persistence.model.representation.SetResourceReferenceSerializer;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A configuration contains information on how to process a given {@link Resource} into a {@link DataModel}, e.g., delimiter etc.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CONFIGURATION")
public class Configuration extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

	/**
	 * The related resources.
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "CONFIGURATIONS_RESOURCES", joinColumns = { @JoinColumn(name = "CONFIGURATION_UUID", referencedColumnName = "UUID") },
			inverseJoinColumns = { @JoinColumn(name = "RESOURCE_UUID", referencedColumnName = "UUID") })
	@JsonSerialize(using = SetResourceReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonDeserialize(using = ResourceSetReferenceDeserializer.class)
	@XmlIDREF
	@XmlList
	// @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Set<Resource> resources;

	/**
	 * A string that holds a serialised JSON object of configuration parameters.
	 */
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "PARAMETERS", columnDefinition = "BLOB")
	private byte[] parametersString;

	/**
	 * A JSON object of configuration parameters.
	 */
	@Transient
	private ObjectNode parameters;

	/**
	 * A flag that indicates, whether the configuration parameters are initialised or not.
	 */
	@Transient
	private boolean parametersInitialized;

	public Configuration(final String uuidArg) {

		super(uuidArg);
	}

	protected Configuration() {

	}

	/**
	 * Gets the configuration parameters.
	 *
	 * @return the configuration parameters
	 */
	@XmlElement(name = "parameters")
	public ObjectNode getParameters() {

		initParameters(false);

		return parameters;
	}

	/**
	 * Sets the configuration parameters.
	 *
	 * @param parameters new configuration parameters
	 */
	@XmlElement(name = "parameters")
	public void setParameters(final ObjectNode parameters) {

		this.parameters = parameters;

		refreshParametersString();
	}

	/**
	 * Adds a new configuration parameter.
	 *
	 * @param key   the key of the configuration parameter
	 * @param value the value of the configuration parameter
	 */
	public void addParameter(final String key, final JsonNode value) {

		if (parameters == null) {

			initParameters(true);
		}

		parameters.set(key, value);

		refreshParametersString();
	}

	/**
	 * Gets the configuration parameter for the given key.
	 *
	 * @param key a configuration parameter key
	 * @return the value of the matched configuration parameter or null
	 */
	public JsonNode getParameter(final String key) {

		initParameters(false);

		return parameters.get(key);
	}

	/**
	 * Gets the resources that are related to this configuration
	 *
	 * @return the resources that are related to this configuration
	 */
	public Set<Resource> getResources() {

		return resources;
	}

	/**
	 * Sets the resources of this configuration
	 *
	 * @param resourcesArg a new collection of resources
	 */
	public void setResources(final Set<Resource> resourcesArg) {

		if (resourcesArg == null && resources != null) {

			// remove configuration from resources, if configuration, will be prepared for removal

			final Set<Resource> resourcesToBeDeleted = Sets.newCopyOnWriteArraySet(resources);

			for (final Resource resource : resourcesToBeDeleted) {

				resource.removeConfiguration(this);
			}

			resources.clear();
		}

		if (resourcesArg != null) {

			if (!DMPPersistenceUtil.getResourceUtils().completeEquals(resources, resourcesArg)) {

				if (resources == null) {

					resources = Sets.newCopyOnWriteArraySet();
				}

				resources.clear();
				resources.addAll(resourcesArg);
			}

			for (final Resource resource : resourcesArg) {

				resource.addConfiguration(this);
			}
		}
	}

	/**
	 * Adds a new resource to the collection of resources of this configuration.<br>
	 * Created by: tgaengler
	 *
	 * @param resource a new export definition revision
	 */
	public void addResource(final Resource resource) {

		if (resource != null) {

			if (resources == null) {

				resources = Sets.newCopyOnWriteArraySet();
			}

			if (!resources.contains(resource)) {

				resources.add(resource);
				resource.addConfiguration(this);
			}
		}
	}

	/**
	 * Replaces an existing resource, i.e., the resource with the same identifier will be replaced.<br>
	 * Created by: tgaengler
	 *
	 * @param resource an existing, updated resource
	 */
	public void replaceResource(final Resource resource) {

		if (resource != null) {

			if (resources == null) {

				resources = Sets.newCopyOnWriteArraySet();
			}

			if (resources.contains(resource)) {

				resources.remove(resource);
			}

			resources.add(resource);

			resource.removeConfiguration(this);
			resource.addConfiguration(this);
		}
	}

	/**
	 * Removes an existing resource from the collection of resources of this configuration.<br>
	 * Created by: tgaengler
	 *
	 * @param resource an existing resource that should be removed
	 */
	public void removeResource(final Resource resource) {

		if (resources != null && resource != null && resources.contains(resource)) {

			resources.remove(resource);

			resource.removeConfiguration(this);
		}
	}

	/**
	 * Refreshs the string that holds the serialised JSON object of the configuration parameters. This method should be called
	 * after every manipulation of the configuration parameters (to keep the states consistent).
	 */
	private void refreshParametersString() {

		if (parameters != null) {

			parametersString = parameters.toString().getBytes(Charsets.UTF_8);
		}
	}

	/**
	 * Initialises the configuration parameters from the string that holds the serialised JSON object of the configuration
	 * parameters.
	 *
	 * @param fromScratch flag that indicates, whether the configuration parameters should be initialised from scratch or not
	 */
	private void initParameters(final boolean fromScratch) {

		if (parameters == null && !parametersInitialized) {

			if (parametersString == null) {

				Configuration.LOG.debug("parameters JSON string is null");

				if (fromScratch) {

					parameters = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
				}

				parametersInitialized = true;

				return;
			}

			try {

				parameters = DMPPersistenceUtil.getJSON(StringUtils.toEncodedString(parametersString, Charsets.UTF_8));
			} catch (final DMPException e) {

				Configuration.LOG.debug("couldn't parse parameters JSON string for configuration '" + getUuid() + "'");
			}

			parametersInitialized = true;
		}
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Configuration.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((Configuration) obj).getParameters(), getParameters())
				&& Objects.equal(((Configuration) obj).getResources(), getResources());
	}
}
