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
import java.util.Map;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.DMPException;
import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A data schema is a collection of {@link SchemaAttributePathInstance}s and a record class ({@link Clasz}) and optionally it contains a content
 * schema ({@link ContentSchema}). An {@link AttributePath} can only occur once in a schema, i.e., the ordered list of {@link SchemaAttributePathInstance}s must be an ordered set that also ensures that there are not two (ore more) {@link SchemaAttributePathInstance}s included that refer to the same {@link AttributePath}.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "DATA_SCHEMA")
public class Schema extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(Schema.class);

	/**
	 * The base URI of the schema
	 */
	@Column(name = "BASE_URI")
	@XmlElement(name = "base_uri")
	private String baseURI;

	/**
	 * All attribute path (instances) of the schema.
	 */
	// @ManyToMany(mappedBy = "schemas", fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE,
	// CascadeType.PERSIST, CascadeType.REFRESH })
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "SCHEMAS_SCHEMA_ATTRIBUTE_PATH_INSTANCES", joinColumns = { @JoinColumn(name = "SCHEMA_UUID", referencedColumnName = "UUID") },
			inverseJoinColumns = { @JoinColumn(name = "SCHEMA_ATTRIBUTE_PATH_INSTANCE_UUID", referencedColumnName = "UUID") })
	@JsonIgnore
	private Set<SchemaAttributePathInstance> attributePaths;

	/**
	 * All attribute path (instances) of the schema in their correct order. To guarantee the attribute path uniqueness constraint we utilise a map here. Whereby, the key is the stringified attribute path.
	 */
	@Transient
	private Map<String, SchemaAttributePathInstance> orderedAttributePaths;

	/**
	 * All attribute path (instances) of the schema in their correct order as a Json array.
	 */
	@Transient
	private ArrayNode orderedAttributePathsJson;

	/**
	 * true if the attribute paths were already initialized
	 */
	@Transient
	private boolean isOrderedAttributePathsInitialized;

	/**
	 * A Json string of all attribute path (instances) of this schema.
	 * This is a serialization of {@link #orderedAttributePathsJson}
	 */
	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "SCHEMA_ATTRIBUTE_PATH_INSTANCES", columnDefinition = "LONGBLOB")
	private byte[] attributePathsJsonString;

	/**
	 * The record class of the schema.
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "RECORD_CLASS")
	@XmlElement(name = "record_class")
	private Clasz recordClass;

	/**
	 * The content schema of the schema.
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "CONTENT_SCHEMA")
	@XmlElement(name = "content_schema")
	private ContentSchema contentSchema;

	protected Schema() {

	}

	public Schema(final String uuid) {

		super(uuid);
	}

	/**
	 * Gets the base URI of the schema.
	 *
	 * @return the base URI of the schema
	 */
	public String getBaseURI() {

		return baseURI;
	}

	/**
	 * Sets the base URI of the schema.
	 *
	 * @param baseURI the base URI of the schema
	 */
	public void setBaseURI(final String baseURI) {

		this.baseURI = baseURI;
	}

	/**
	 * @return an ordered list of all attribute path (instances).
	 */
	@XmlElement(name = "attribute_paths")
	public Collection<SchemaAttributePathInstance> getAttributePaths() {
		tryInitializeOrderedAttributePaths();
		return orderedAttributePaths.values();
	}

	/**
	 * Sets all attribute path (instances) of the schema.
	 *
	 * @param attributePathsArg all attribute paths instances of the schema (should be an ordered list, where each attribute path should only occur once in list)
	 */
	@XmlElement(name = "attribute_paths")
	public void setAttributePaths(final Collection<SchemaAttributePathInstance> attributePathsArg) {
		if (attributePathsArg == null) {
			removeAllAttributePaths();
		} else {
			setAllAttributePaths(attributePathsArg);
		}
	}

	/**
	 * Gets all attribute path (instances) of the schema.
	 *
	 * @return all attribute paths instances of the schema
	 */
	@JsonIgnore
	public Set<SchemaAttributePathInstance> getUniqueAttributePaths() {
		return attributePaths;
	}

	/**
	 * Gets the attribute path (instance) for the given schema attribute path instance identifier.
	 *
	 * @param uuid a schema attribute path instance identifier
	 * @return that matched attribute path (instance) or null
	 */
	public SchemaAttributePathInstance getAttributePath(final String uuid) {
		Preconditions.checkNotNull(uuid);

		ensureAttributePaths();

		if (attributePaths != null) {
			for (final SchemaAttributePathInstance attributePath : attributePaths) {
				if (attributePath.getUuid().equals(uuid)) {
					return attributePath;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the attribute path (instance) for the given schema attribute path uri path.
	 *
	 * @param uriPath a schema attribute path uri path
	 * @return that matched attribute path (instance) or null
	 */
	public SchemaAttributePathInstance getAttributePathByURIPath(final String uriPath) {
		Preconditions.checkNotNull(uriPath);

		ensureAttributePaths();
		ensureInitializedOrderedAttributePaths();

		if (orderedAttributePaths != null) {

			return orderedAttributePaths.get(uriPath);
		}

		return null;
	}

	/**
	 * Adds a new attribute path (instance) to the collection of attribute path (instances) of this schema.<br>
	 *
	 * @param attributePath a new attribute path
	 */
	public void addAttributePath(final SchemaAttributePathInstance attributePath) {

		Preconditions.checkNotNull(attributePath);

		ensureAttributePaths();
		ensureInitializedOrderedAttributePaths();

		// TODO check if equals method works for SAPIs, otherwise the usage of contains may fail here

		// second check is for attribute path uniqueness constraint
		if (!attributePaths.contains(attributePath) && !orderedAttributePaths.containsKey(attributePath.getAttributePath().toAttributePath())) {

			attributePaths.add(attributePath);
			orderedAttributePaths.put(attributePath.getAttributePath().toAttributePath(), attributePath);

			refreshAttributePathsString();
		}
	}

	//note: index specific insert is not really needed right now
	//	/**
	//	 * Adds a new attribute path (instance) at the given index, overwriting any existing attribute path (instance).
	//	 *
	//	 * @param attributePath the attribute path instance to add
	//	 * @param atIndex        the index at which to add
	//	 */
	//	public void addAttributePath(final SchemaAttributePathInstance attributePath, final int atIndex) {
	//		Preconditions.checkNotNull(attributePath);
	//		Preconditions.checkArgument(atIndex >= 0, "insertion index must be positive");
	//		Preconditions
	//				.checkArgument(atIndex <= orderedAttributePaths.size(), "insertion index must not be greater than %s", orderedAttributePaths.size());
	//
	//		ensureAttributePaths();
	//		ensureInitializedOrderedAttributePaths();
	//
	//		if (!attributePath.equals(orderedAttributePaths.get(atIndex))) {
	//			orderedAttributePaths.add(atIndex, attributePath);
	//			attributePaths.add(attributePath);
	//			refreshAttributePathsString();
	//		}
	//	}

	//	 note removal is not really needed right now

	/**
	 * Removes an existing attribute path (instance) from the collection of attribute path (instances) of this export schema.<br>
	 * Created by: tgaengler
	 *
	 * @param attributePath an existing attribute path instance that should be removed
	 */
	public void removeAttributePath(final SchemaAttributePathInstance attributePath) {
		if (attributePath != null && attributePaths != null) {
			final boolean isRemoved = attributePaths.remove(attributePath);
			if (isRemoved && orderedAttributePaths != null) {
				orderedAttributePaths.remove(attributePath.getAttributePath().toAttributePath());
			}
		}
	}

	// note: removal is not really needed right now
	//	/**
	//	 * Removes an attribute path (instance) if it occurs at a specific index.
	//	 *
	//	 * @param attributePath the attribute path instance to remove
	//	 * @param atIndex       the index from which to remove
	//	 * @return true if the attribute path instance could be removed, false otherwise.
	//	 */
	//	public boolean removeAttributePath(final AttributePath attributePath, final int atIndex) {
	//		Preconditions.checkNotNull(attributePath);
	//		Preconditions.checkArgument(atIndex >= 0, "deletion index must be positive");
	//		Preconditions.checkArgument(atIndex < orderedAttributePaths.size(), "deletion index must be less than {}", orderedAttributePaths.size());
	//
	//		if (orderedAttributePaths != null) {
	//			if (orderedAttributePaths.get(atIndex).equals(attributePath)) {
	//				orderedAttributePaths.remove(atIndex);
	//				if (attributePaths != null) {
	//					attributePaths.remove(attributePath);
	//				}
	//				return true;
	//			}
	//		} else {
	//			if (attributePaths != null) {
	//				return attributePaths.remove(attributePath);
	//			}
	//		}
	//		return false;
	//	}

	/**
	 * Gets the record class of the schema.
	 *
	 * @return the record class of the schema
	 */
	public Clasz getRecordClass() {

		return recordClass;
	}

	/**
	 * Sets the record class of the schema.
	 *
	 * @param recordClassArg a new record class
	 */
	public void setRecordClass(final Clasz recordClassArg) {

		recordClass = recordClassArg;
	}

	/**
	 * Gets the content schema of the schema.
	 *
	 * @return the content schema of the schema
	 */
	public ContentSchema getContentSchema() {

		return contentSchema;
	}

	/**
	 * Sets the content schema of the schema.
	 *
	 * @param contentSchemaArg a new content schema
	 */
	public void setContentSchema(final ContentSchema contentSchemaArg) {

		contentSchema = contentSchemaArg;
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Schema.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((Schema) obj).getBaseURI(), getBaseURI())
				&& DMPPersistenceUtil.getSchemaAttributePathInstanceUtils()
				.completeEquals(((Schema) obj).getUniqueAttributePaths(), getUniqueAttributePaths())
				// note: we need also to compare the ordered list of schema attribute path instances here
				&& DMPPersistenceUtil.getSchemaAttributePathInstanceUtils().completeEquals(((Schema) obj).getAttributePaths(), getAttributePaths())
				&& DMPPersistenceUtil.getClaszUtils().completeEquals(((Schema) obj).getRecordClass(), getRecordClass())
				&& DMPPersistenceUtil.getContentSchemaUtils().completeEquals(((Schema) obj).getContentSchema(), getContentSchema());
	}

	private void ensureAttributePaths() {
		if (attributePaths == null) {
			attributePaths = Sets.newCopyOnWriteArraySet();
		}
	}

	private void ensureOrderedAttributePaths() {
		if (orderedAttributePaths == null) {
			orderedAttributePaths = Maps.newLinkedHashMap();
		}
	}

	@JsonIgnore
	private void setAllAttributePaths(final Collection<SchemaAttributePathInstance> attributePathsArg) {
		ensureOrderedAttributePaths();

		if (!DMPPersistenceUtil.getSchemaAttributePathInstanceUtils().completeEquals(orderedAttributePaths.values(), attributePathsArg)) {
			ensureAttributePaths();

			attributePaths.clear();
			orderedAttributePaths.clear();

			for (final SchemaAttributePathInstance newAttributePath : attributePathsArg) {

				final String attributePathString = newAttributePath.getAttributePath().toAttributePath();

				if (!orderedAttributePaths.containsKey(attributePathString)) {

					orderedAttributePaths.put(attributePathString, newAttributePath);
					attributePaths.add(newAttributePath);
				}
			}
		}

		refreshAttributePathsString();
	}

	private void removeAllAttributePaths() {
		if (orderedAttributePaths != null) {
			if (attributePaths != null) {
				attributePaths.clear();
			}
			orderedAttributePaths.clear();
			refreshAttributePathsString();
		}
	}

	private void ensureInitializedOrderedAttributePaths() {
		if (!isOrderedAttributePathsInitialized) {
			initializedAttributePaths(true);
			ensureOrderedAttributePaths();
		}
	}

	private void tryInitializeOrderedAttributePaths() {
		initializedAttributePaths(false);
		ensureOrderedAttributePaths();
	}

	private void initializedAttributePaths(final boolean fromScratch) {
		if (orderedAttributePathsJson == null && !isOrderedAttributePathsInitialized) {

			if (attributePathsJsonString == null) {
				Schema.LOG.debug("attribute paths JSON is null for {}", getUuid());

				if (fromScratch) {
					orderedAttributePathsJson = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
					orderedAttributePaths = Maps.newLinkedHashMap();

					isOrderedAttributePathsInitialized = true;
				}

				return;
			}

			try {
				orderedAttributePaths = Maps.newLinkedHashMap();
				orderedAttributePathsJson = DMPPersistenceUtil.getJSONArray(StringUtils.toEncodedString(attributePathsJsonString, Charsets.UTF_8));

				if (orderedAttributePathsJson != null) {

					for (final JsonNode attributePathIdNode : orderedAttributePathsJson) {
						final SchemaAttributePathInstance attributePath = getAttributePath(attributePathIdNode.asText());
						if (attributePath != null && !orderedAttributePaths.containsKey(attributePath.getAttributePath().toAttributePath())) {
							orderedAttributePaths.put(attributePath.getAttributePath().toAttributePath(), attributePath);
						}
					}
				}
			} catch (final DMPException e) {
				Schema.LOG.debug("couldn't parse attribute path JSON for attribute path '" + getUuid() + "'", e);
			}
			isOrderedAttributePathsInitialized = true;
		}
	}

	private void refreshAttributePathsString() {
		if (orderedAttributePaths != null) {
			orderedAttributePathsJson = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
			for (final SchemaAttributePathInstance attributePath : orderedAttributePaths.values()) {
				orderedAttributePathsJson.add(attributePath.getUuid());
			}
		}

		if (orderedAttributePathsJson != null && orderedAttributePathsJson.size() > 0) {
			attributePathsJsonString = orderedAttributePathsJson.toString().getBytes(Charsets.UTF_8);
		} else {
			attributePathsJsonString = null;
		}
	}
}
