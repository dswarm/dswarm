package de.avgl.dmp.persistence.model.job;

import java.util.LinkedList;
import java.util.List;
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
import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matchers;

import ch.lambdaj.Lambda;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "ATTRIBUTE_PATH")
public class AttributePath extends DMPJPAObject {

	/**
	 * 
	 */
	private static final long						serialVersionUID				= 1L;

	private static final org.apache.log4j.Logger	LOG								= org.apache.log4j.Logger.getLogger(AttributePath.class);

	/**
	 * All attributes of the attribute path as ordered list
	 */
	// @ManyToMany(mappedBy = "attributePaths", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE,
	// CascadeType.PERSIST, CascadeType.REFRESH })
	@JsonIgnore
	@Access(AccessType.FIELD)
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "ATTRIBUTES_ATTRIBUTE_PATHS", joinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "ATTRIBUTE_ID", referencedColumnName = "ID") })
	private Set<Attribute>							attributes						= null;

	@Transient
	private LinkedList<Attribute>					orderedAttributes				= null;

	@Transient
	private ArrayNode								orderedAttributesJSON;

	@Transient
	private boolean									orderedAttributesInitialized	= false;

	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "ATTRIBUTE_PATH", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String									attributePath					= null;

	/**
	 * All schemas that utilise this attribute path
	 */
	// @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
	// CascadeType.REFRESH })
	// @JoinTable(name = "SCHEMAS_ATTRIBUTE_PATHS", joinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName =
	// "ID") }, inverseJoinColumns = { @JoinColumn(name = "SCHEMA_ID", referencedColumnName = "ID") })
	// private Set<Schema> schemas = null;

	public AttributePath() {

	}

	public AttributePath(final LinkedList<Attribute> attributesArg) {

		orderedAttributes = attributesArg;

		if (null != attributesArg) {

			attributes = Sets.newLinkedHashSet(orderedAttributes);

			initAttributePath(false);
		}
	}

	public Set<Attribute> getAttributes() {

		return attributes;
	}

	@XmlElement(name = "attributes")
	public LinkedList<Attribute> getAttributePath() {

		initAttributePath(false);

		return orderedAttributes;
	}

	public void setAttributePath(final LinkedList<Attribute> attributesArg) {

		if (attributesArg == null && orderedAttributes != null) {

			// remove attribute path from attribute, if attribute path will be prepared for removal

			// for (final Attribute attribute : attributes) {
			//
			// attribute.removeAttributePath(this);
			// }
			attributes = null;
		}

		orderedAttributes = attributesArg;

		if (attributesArg != null) {

			if (null == attributes) {

				attributes = Sets.newLinkedHashSet();
			}

			for (final Attribute attribute : attributesArg) {

				// attribute.addAttributePath(this);

				attributes.add(attribute);
			}
		}

		refreshAttributePathString();
	}

	/**
	 * Adds a new attribute to the end of this attribute path.<br>
	 * Created by: tgaengler
	 * 
	 * @param attributeArg a new attribute
	 */
	public void addAttribute(final Attribute attributeArg) {

		if (attributeArg != null) {

			if (attributes == null) {

				attributes = Sets.newLinkedHashSet();
			}

			if (orderedAttributes == null) {

				initAttributePath(false);

				if (orderedAttributes == null) {

					orderedAttributes = Lists.newLinkedList();
				}
			}

			// if (!attributes.contains(attributeArg)) {

			attributes.add(attributeArg);
			orderedAttributes.add(attributeArg);

			// final int attributeIndex = attributes.lastIndexOf(attributeArg);
			//
			// attributeArg.addAttributePath(this, attributeIndex);
			// }

			refreshAttributePathString();
		}
	}

	/**
	 * Adds a new attribute to the end of this attribute path.<br>
	 * Created by: tgaengler
	 * 
	 * @param attributeArg a new attribute
	 */
	protected void addAttribute(final Attribute attributeArg, final int attributeIndex) {

		if (attributeArg != null) {

			if (attributes == null) {

				attributes = Sets.newLinkedHashSet();
			}

			if (orderedAttributes == null) {

				orderedAttributes = Lists.newLinkedList();
			}

			if (!attributeArg.equals(orderedAttributes.get(attributeIndex))) {

				orderedAttributes.add(attributeIndex, attributeArg);
				attributes.add(attributeArg);

				// final int attributeIndex2 = attributes.lastIndexOf(attributeArg);
				//
				// attributeArg.addAttributePath(this, attributeIndex2);

				refreshAttributePathString();
			}
		}
	}

	/**
	 * Removes an existing attribute from this attribute path.<br>
	 * Created by: tgaengler
	 * 
	 * @param attribute an existing attribute that should be removed
	 * @param attributeIndex the position of the attribute in the attribute path
	 */
	public void removeAttribute(final Attribute attribute, final int attributeIndex) {

		if (attribute != null
				&& ((orderedAttributes != null && orderedAttributes.contains(attribute)) || (attributes != null && attributes.contains(attribute)))) {

			orderedAttributes.remove(attributeIndex);

			if (!orderedAttributes.contains(attribute)) {

				attributes.remove(attribute);
			}

			// attribute.removeAttributePath(this);

			refreshAttributePathString();
		}
	}

	// public Set<Schema> getSchemas() {
	//
	// return schemas;
	// }
	//
	// public void setSchemas(final Set<Schema> schemasArg) {
	//
	// if (schemasArg == null && schemas != null) {
	//
	// // remove attribute path from schema, if attribute path, will be prepared for removal
	//
	// for (final Schema schema : schemas) {
	//
	// schema.removeAttributePath(this);
	// }
	// }
	//
	// schemas = schemasArg;
	//
	// if (schemasArg != null) {
	//
	// for (final Schema schema : schemasArg) {
	//
	// schema.addAttributePath(this);
	// }
	// }
	// }
	//
	// /**
	// * Adds a new schema to the collection of schemas of this attribute path.<br>
	// * Created by: tgaengler
	// *
	// * @param schema a new schema
	// */
	// public void addSchema(final Schema schema) {
	//
	// if (schema != null) {
	//
	// if (schemas == null) {
	//
	// schemas = Sets.newLinkedHashSet();
	// }
	//
	// if (!schemas.contains(schema)) {
	//
	// schemas.add(schema);
	// schema.addAttributePath(this);
	// }
	// }
	// }
	//
	// /**
	// * Removes an existing schema from the collection of schemas of this attribute path.<br>
	// * Created by: tgaengler
	// *
	// * @param schema an existing schema that should be removed
	// */
	// public void removeSchema(final Schema schema) {
	//
	// if (schemas != null && schema != null && schemas.contains(schema)) {
	//
	// schemas.remove(schema);
	//
	// schema.removeAttributePath(this);
	// }
	// }

	public String toAttributePath() {

		if (null == getAttributePath()) {

			return null;
		}

		if (getAttributePath().isEmpty()) {

			return null;
		}

		final StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (final Attribute attribute : getAttributePath()) {

			if (!first) {

				sb.append(".");
			} else {

				first = false;
			}

			sb.append(attribute.getId());
		}

		return sb.toString();
	}

	@Override
	public boolean equals(final Object obj) {

		if (!AttributePath.class.isInstance(obj)) {

			return false;
		}

		return super.equals(obj);
	}

	private void refreshAttributePathString() {

		if (orderedAttributes != null) {

			orderedAttributesJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

			for (final Attribute attribute : orderedAttributes) {

				orderedAttributesJSON.add(attribute.getId());
			}
		}

		if (null != orderedAttributesJSON && orderedAttributesJSON.size() > 0) {

			attributePath = orderedAttributesJSON.toString();
		} else {

			attributePath = null;
		}
	}

	private void initAttributePath(final boolean fromScratch) {

		if (orderedAttributesJSON == null && !orderedAttributesInitialized) {

			if (attributePath == null) {

				AttributePath.LOG.debug("attributes path JSON is null for '" + getId() + "'");

				if (fromScratch) {

					orderedAttributesJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
					orderedAttributes = Lists.newLinkedList();

					orderedAttributesInitialized = true;
				}

				return;
			}

			try {

				orderedAttributes = Lists.newLinkedList();

				// parse attribute path string
				orderedAttributesJSON = DMPPersistenceUtil.getJSONArray(attributePath);

				if (null != orderedAttributesJSON) {

					for (final JsonNode attributeIdNode : orderedAttributesJSON) {

						final Attribute attribute = getAttribute(attributeIdNode.asText());

						if (null != attribute) {

							orderedAttributes.add(attribute);
						}
					}
				}
			} catch (final DMPException e) {

				AttributePath.LOG.debug("couldn't parse attribute path JSON for attribute path '" + getId() + "'");
			}

			orderedAttributesInitialized = true;
		}
	}

	private Attribute getAttribute(final String id) {

		if (null == id) {

			return null;
		}

		if (null == attributes) {

			return null;
		}

		if (attributes.isEmpty()) {

			return null;
		}

		final List<Attribute> attributesFiltered = Lambda.filter(Lambda.having(Lambda.on(Attribute.class).getId(), Matchers.equalTo(id)), attributes);

		if (attributesFiltered == null || attributesFiltered.isEmpty()) {

			return null;
		}

		return attributesFiltered.get(0);
	}
}
