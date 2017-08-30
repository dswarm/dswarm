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
package org.dswarm.persistence.model.schema;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
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

import ch.lambdaj.Lambda;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.DMPException;
import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.schema.utils.AttributePathUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * An attribute path is an ordered list of {@link Attribute}s.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "ATTRIBUTE_PATH")
@Cacheable(false)
public class AttributePath extends DMPObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(AttributePath.class);

	/**
	 * All utilised attributes of this attribute path.
	 */
	// @ManyToMany(mappedBy = "attributePaths", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE,
	// CascadeType.PERSIST, CascadeType.REFRESH })
	@JsonIgnore
	@Access(AccessType.FIELD)
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "ATTRIBUTE_PATHS_ATTRIBUTES", joinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_UUID", referencedColumnName = "UUID") },
			inverseJoinColumns = { @JoinColumn(name = "ATTRIBUTE_UUID", referencedColumnName = "UUID") })
	private Set<Attribute> attributes;

	/**
	 * all attributes of this attribute path as ordered list.
	 */
	@Transient
	private List<Attribute> orderedAttributes;

	/**
	 * A JSON object of the ordered list of attributes.
	 */
	@Transient
	private ArrayNode orderedAttributesJSON;

	/**
	 * A flag that indicates, whether the attributes are initialised or not.
	 */
	@Transient
	private boolean orderedAttributesInitialized;

	/**
	 * A string that holds the serialised JSON object of the attribute path (ordered list of attributes).
	 */
	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "ATTRIBUTE_PATH", columnDefinition = "BLOB")
	private byte[] attributePath;

	/**
	 * All schemas that utilise this attribute path
	 */
	// @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
	// CascadeType.REFRESH })
	// @JoinTable(name = "SCHEMAS_ATTRIBUTE_PATHS", joinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName =
	// "ID") }, inverseJoinColumns = { @JoinColumn(name = "SCHEMA_ID", referencedColumnName = "ID") })
	// private Set<Schema> schemas = null;

	/**
	 * Creates a new attribute path.
	 */
	protected AttributePath() {

	}

	public AttributePath(final String uuid) {

		// TODO: how should be implement the uniqueness constraint (URI) properly now?

		super(uuid);
	}

	/**
	 * Creates a new attribute path with the given ordered list of attributes.
	 *
	 * @param attributesArg an ordered list of attributes
	 */
	public AttributePath(final Collection<Attribute> attributesArg) {

		// orderedAttributes = attributesArg;
		//
		// if (null != orderedAttributes) {
		//
		// attributes = Sets.newLinkedHashSet(orderedAttributes);
		//
		// initAttributePath(false);
		// }

		setAttributePath(attributesArg);
	}

	/**
	 * Gets the utilised attributes of this attribute path.<br/>
	 * note: this is not the ordered list of attributes, i.e., the attribute path; these are only the utilised attributes, i.e.,
	 * an attribute can occur multiple times in an attribute path.
	 *
	 * @return the utilised attributes of this attribute path
	 */
	public Set<Attribute> getAttributes() {

		return attributes;
	}

	/**
	 * Gets the attribute path, i.e., the ordered list of attributes.
	 *
	 * @return the attribute path
	 */
	@XmlElement(name = "attributes")
	public List<Attribute> getAttributePath() {

		initAttributePath(false);

		return orderedAttributes;
	}

	/**
	 * Gets the attribute path as JSON object (a list of Attributes)
	 *
	 * @return the attribute path as JSON object
	 */
	@JsonIgnore
	public String getAttributePathAsJSONObjectString() {

		initAttributePath(false);

		refreshAttributePathString();

		return StringUtils.toEncodedString(attributePath, Charsets.UTF_8);
	}

	/**
	 * Sets the attribute path (ordered list of attributes).
	 *
	 * @param attributesArg a new attribute path (ordered list of attributes)
	 */
	@XmlElement(name = "attributes")
	public void setAttributePath(final Collection<Attribute> attributesArg) {

		if (attributesArg == null && orderedAttributes != null) {

			// remove attribute path from attribute, if attribute path will be prepared for removal

			// for (final Attribute attribute : attributes) {
			//
			// attribute.removeAttributePath(this);
			// }

			if (attributes != null) {

				attributes.clear();
			}

			orderedAttributes.clear();
		}

		// orderedAttributes = attributesArg;

		if (attributesArg != null) {

			if (orderedAttributes == null) {

				orderedAttributes = Lists.newLinkedList();
			}

			// if (!orderedAttributes.equals(attributesArg)) {
			if (!DMPPersistenceUtil.getAttributeUtils().completeEquals(orderedAttributes, attributesArg)) {

				orderedAttributes.clear();
				orderedAttributes.addAll(attributesArg);

				if (null == attributes) {

					attributes = Sets.newConcurrentHashSet();
				}

				attributes.clear();

				for (final Attribute attribute : orderedAttributes) {

					// attribute.addAttributePath(this);

					attributes.add(attribute);
				}
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

				attributes = Sets.newConcurrentHashSet();
			}

			if (orderedAttributes == null) {

				initAttributePath(true);

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

				attributes = Sets.newCopyOnWriteArraySet();
			}

			if (orderedAttributes == null) {

				initAttributePath(true);

				if (orderedAttributes == null) {

					orderedAttributes = Lists.newLinkedList();
				}
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
	 * @param attribute      an existing attribute that should be removed
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

	/**
	 * Builds the ordered list of (identifiers of) attributes as one string, separated by a delimiter character (currently a dot)
	 *
	 * @return ordered list of (identifiers of) attributes as one string
	 */
	public String toAttributePath() {

		final List<Attribute> attributePath = getAttributePath();

		return AttributePathUtils.generateAttributePath(attributePath);
	}

	/**
	 * Refreshs the string that holds the serialised JSON object of the attribute path (ordered list of attributes). This method
	 * should be called after every manipulation of the attribute path (to keep the states consistent).
	 */
	private void refreshAttributePathString() {

		if (orderedAttributes != null) {

			orderedAttributesJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

			for (final Attribute attribute : orderedAttributes) {

				orderedAttributesJSON.add(attribute.getUuid());
			}
		}

		if (null != orderedAttributesJSON && orderedAttributesJSON.size() > 0) {

			attributePath = orderedAttributesJSON.toString().getBytes(Charsets.UTF_8);
		} else {

			attributePath = null;
		}
	}

	/**
	 * Initialises the attribute path, collection of attributes and JSON object from the string that holds the serialised JSON
	 * object of the attribute path.
	 *
	 * @param fromScratch flag that indicates, whether the attribute path should be initialised from scratch or not
	 */
	private void initAttributePath(final boolean fromScratch) {

		if (orderedAttributesJSON == null && !orderedAttributesInitialized) {

			if (attributePath == null) {

				AttributePath.LOG.debug("attributes path JSON is null for '" + getUuid() + "'");

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
				orderedAttributesJSON = DMPPersistenceUtil.getJSONArray(StringUtils.toEncodedString(attributePath, Charsets.UTF_8));

				if (null != orderedAttributesJSON) {

					for (final JsonNode attributeIdNode : orderedAttributesJSON) {

						final Attribute attribute = getAttribute(attributeIdNode.asText());

						if (null != attribute) {

							orderedAttributes.add(attribute);
						}
					}
				}
			} catch (final DMPException e) {

				AttributePath.LOG.debug("couldn't parse attribute path JSON for attribute path '" + getUuid() + "'");
			}

			orderedAttributesInitialized = true;
		}
	}

	/**
	 * Gets the attribute for a given attribute identifier.
	 *
	 * @param uuid an attribute identifier
	 * @return the matched attribute or null
	 */
	public Attribute getAttribute(final String uuid) {

		if (null == uuid) {

			return null;
		}

		if (null == attributes) {

			return null;
		}

		if (attributes.isEmpty()) {

			return null;
		}

		final List<Attribute> attributesFiltered = Lambda
				.filter(Lambda.having(Lambda.on(Attribute.class).getUuid(), Matchers.equalTo(uuid)), attributes);

		if (attributesFiltered == null || attributesFiltered.isEmpty()) {

			return null;
		}

		return attributesFiltered.get(0);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return AttributePath.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getAttributeUtils().completeEquals(((AttributePath) obj).getAttributes(), getAttributes())
				&& DMPPersistenceUtil.getAttributeUtils().completeEquals(((AttributePath) obj).getAttributePath(), getAttributePath());
	}
}
