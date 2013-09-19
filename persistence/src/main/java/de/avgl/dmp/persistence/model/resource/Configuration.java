package de.avgl.dmp.persistence.model.resource;

import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.model.utils.ResourceReferenceDeserializer;
import de.avgl.dmp.persistence.model.utils.ResourceReferenceSerializer;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

@XmlRootElement
@Entity
@Table(name = "CONFIGURATION")
public class Configuration extends DMPJPAObject {

	/**
	 * 
	 */
	private static final long						serialVersionUID		= 1L;

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(Configuration.class);

	@Column(name = "NAME")
	private String									name					= null;

	@Column(name = "DESCRIPTION")
	private String									description				= null;
	
	/**
	 * The related resources.
	 */
	@ManyToMany(mappedBy = "configurations", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonSerialize(using = ResourceReferenceSerializer.class)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonDeserialize(using = ResourceReferenceDeserializer.class)
	@XmlIDREF
	@XmlList
	private Set<Resource>	resources;

	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "parameters", columnDefinition = "CLOB")
	private String									parametersString;

	@Transient
	private ObjectNode								parameters;

	@Transient
	private boolean									parametersInitialized	= false;

	public String getName() {

		return name;
	}

	public void setName(final String name) {

		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {

		this.description = description;
	}

	public ObjectNode getParameters() {

		if (parameters == null && parametersInitialized == false) {

			try {

				parameters = DMPPersistenceUtil.getJSON(parametersString);
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

			parameters = new ObjectNode(DMPPersistenceUtil.getJSONFactory());
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

	public Set<Resource> getResources() {

		return resources;
	}

	public void setResources(final Set<Resource> resourcesArg) {

		if (resourcesArg == null && resources != null) {

			// remove configuration from resources, if configuration, will be prepared for removal

			for (final Resource resource : resources) {

				resource.removeConfiguration(this);
			}
		}

		resources = resourcesArg;

		if (resourcesArg != null) {

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

				resources = Sets.newLinkedHashSet();
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

				resources = Sets.newLinkedHashSet();
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
}
