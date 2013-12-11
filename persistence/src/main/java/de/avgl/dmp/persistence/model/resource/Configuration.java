package de.avgl.dmp.persistence.model.resource;

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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.utils.ResourceReferenceDeserializer;
import de.avgl.dmp.persistence.model.utils.SetResourceReferenceSerializer;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CONFIGURATION")
public class Configuration extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long						serialVersionUID		= 1L;

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(Configuration.class);

	/**
	 * The related resources.
	 */
	@ManyToMany(fetch = FetchType.EAGER, cascade = { /* CascadeType.DETACH, CascadeType.MERGE, */CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "RESOURCES_CONFIGURATIONS", joinColumns = { @JoinColumn(name = "CONFIGURATION_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "RESOURCE_ID", referencedColumnName = "ID") })
	@JsonSerialize(using = SetResourceReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonDeserialize(using = ResourceReferenceDeserializer.class)
	@XmlIDREF
	@XmlList
	// @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	private Set<Resource>							resources;

	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "parameters", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									parametersString;

	@Transient
	private ObjectNode								parameters;

	@Transient
	private boolean									parametersInitialized;

	public ObjectNode getParameters() {

		initParameters(false);

		return parameters;
	}

	public void setParameters(final ObjectNode parameters) {

		this.parameters = parameters;

		refreshParametersString();
	}

	public void addParameter(final String key, final JsonNode value) {

		if (parameters == null) {

			initParameters(true);
		}

		parameters.set(key, value);

		refreshParametersString();
	}

	public JsonNode getParameter(final String key) {

		initParameters(false);

		return parameters.get(key);
	}

	public Set<Resource> getResources() {

		return resources;
	}

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

			if (!resourcesArg.equals(resources)) {

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

	private void refreshParametersString() {

		parametersString = parameters.toString();
	}

	private void initParameters(final boolean fromScratch) {

		if (parameters == null && !parametersInitialized) {

			if (parametersString == null) {

				LOG.debug("parameters JSON string is null");

				if (fromScratch) {

					parameters = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
				}

				parametersInitialized = true;

				return;
			}

			try {

				parameters = DMPPersistenceUtil.getJSON(parametersString);
			} catch (final DMPException e) {

				LOG.debug("couldn't parse parameters JSON string for configuration '" + getId() + "'");
			}

			parametersInitialized = true;
		}
	}

	@Override
	public boolean equals(final Object obj) {

		return Configuration.class.isInstance(obj) && super.equals(obj);

	}
}
