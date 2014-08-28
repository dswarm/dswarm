package org.dswarm.persistence.model.schema;

import java.util.Collection;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.DMPException;
import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A data schema is a collection of {@link AttributePath}s and a record class ({@link Clasz}) and optionally it contains a content
 * schema ({@link ContentSchema}).
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
	private static final long	serialVersionUID	= 1L;

	private static final Logger LOG								= LoggerFactory.getLogger(Schema.class);

	/**
	 * All attributes paths of the schema.
	 */
	// @ManyToMany(mappedBy = "schemas", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE,
	// CascadeType.PERSIST, CascadeType.REFRESH })
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "SCHEMAS_ATTRIBUTE_PATHS", joinColumns = { @JoinColumn(name = "SCHEMA_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName = "ID") })
	@JsonIgnore
	private Set<AttributePath>	attributePaths;

	/**
	 * All attribute paths ff the schema in their correct order.
	 */
	@Transient
	private List<AttributePath>		orderedAttributePaths;

	/**
	 * All attribute paths of the schema in their correct order as a Json array.
	 */
	@Transient
	private ArrayNode				orderedAttributePathsJson;

	/**
	 * true if the attribute paths were already initialized
	 */
	@Transient
	private boolean					isOrderedAttributePathsInitialized;

	/**
	 * A Json string of all attribute paths of this schema.
	 * This is a serialization of {@link #orderedAttributePathsJson}
	 */
	@JsonIgnore
	@Lob
	@Access(AccessType.FIELD)
	@Column(name = "ATTRIBUTE_PATHS", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String					attributePathsJsonString;

	/**
	 * The record class of the schema.
	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "RECORD_CLASS")
	@XmlElement(name = "record_class")
	private Clasz				recordClass;

	/**
	 * The content schema of the schema.
	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "CONTENT_SCHEMA")
	@XmlElement(name = "content_schema")
	private ContentSchema		contentSchema;


	public Schema() {
	}

	/**
	 * @return an ordered list of all attribute paths.
	 */
	@XmlElement(name = "attribute_paths")
	public List<AttributePath> getAttributePaths() {
		tryInitializeOrderedAttributePaths();
		return orderedAttributePaths;
	}

	/**
	 * Sets all attribute paths of the schema.
	 *
	 * @param attributePathsArg all attribute paths of the schema
	 */
	@XmlElement(name = "attribute_paths")
	public void setAttributePaths(final Collection<AttributePath> attributePathsArg) {
		if (attributePathsArg == null) {
			removeAllAttributePaths();
		} else {
			setAllAttributePaths(attributePathsArg);
		}
	}

	/**
	 * Gets all attribute paths of the schema.
	 *
	 * @return all attribute paths of the schema
	 */
	@JsonIgnore
	public Set<AttributePath> getUniqueAttributePaths() {
		return attributePaths;
	}

	/**
	 * Gets the attribute path for the given attribute path identifier.
	 *
	 * @param id an attribute path identifier
	 * @return that matched attribute path or null
	 */
	public AttributePath getAttributePath(final Long id) {
		Preconditions.checkNotNull(id);

		if (attributePaths != null) {
			for (final AttributePath attributePath : attributePaths) {
				if (attributePath.getId().equals(id)) {
					return attributePath;
				}
			}
		}

		return null;
	}

	/**
	 * Adds a new attribute path to the collection of attribute paths of this schema.<br>
	 * Created by: tgaengler
	 *
	 * @param attributePath a new attribute path
	 */
	public void addAttributePath(final AttributePath attributePath) {
		Preconditions.checkNotNull(attributePath);

		ensureAttributePaths();
		ensureInitializedOrderedAttributePaths();

		if (!attributePaths.contains(attributePath)) {

			attributePaths.add(attributePath);
			orderedAttributePaths.add(attributePath);

			refreshAttributePathsString();
		}
	}

	/**
	 * Adds a new attribute path at the given index, overwriting any existing attribute path.
	 *
	 * @param attributePath the attribute path to add
	 * @param atIndex		the index at which to add
	 */
	public void addAttributePath(final AttributePath attributePath, final int atIndex) {
		Preconditions.checkNotNull(attributePath);
		Preconditions.checkArgument(atIndex >= 0, "insertion index must be positive");
		Preconditions.checkArgument(atIndex <= orderedAttributePaths.size(), "insertion index must not be greater than %s", orderedAttributePaths.size());

		ensureAttributePaths();
		ensureInitializedOrderedAttributePaths();

		if (!attributePath.equals(orderedAttributePaths.get(atIndex))) {
			orderedAttributePaths.add(atIndex, attributePath);
			attributePaths.add(attributePath);
			refreshAttributePathsString();
		}
	}

	/**
	 * Removes an existing attribute path from the collection of attribute paths of this export schema.<br>
	 * Created by: tgaengler
	 *
	 * @param attributePath an existing attribute path that should be removed
	 */
	public void removeAttributePath(final AttributePath attributePath) {
		if (attributePath != null && attributePaths != null) {
			final boolean isRemoved = attributePaths.remove(attributePath);
			if (isRemoved && orderedAttributePaths != null) {
				orderedAttributePaths.remove(attributePath);
			}
		}
	}

	/**
	 * Removes an attribute path if it occurs at a specific index.
	 *
	 * @param attributePath the attribute path to remove
	 * @param atIndex       the index from which to remove
	 * @return true if the attribute path could be removed, false otherwise.
	 */
	public boolean removeAttributePath(final AttributePath attributePath, final int atIndex) {
		Preconditions.checkNotNull(attributePath);
		Preconditions.checkArgument(atIndex >= 0, "deletion index must be positive");
		Preconditions.checkArgument(atIndex < orderedAttributePaths.size(), "deletion index must be less than {}", orderedAttributePaths.size());

		if (orderedAttributePaths != null) {
			if (orderedAttributePaths.get(atIndex).equals(attributePath)) {
				orderedAttributePaths.remove(atIndex);
				if (attributePaths != null) {
					attributePaths.remove(attributePath);
				}
				return true;
			}
		} else {
			if (attributePaths != null) {
				return attributePaths.remove(attributePath);
			}
		}
		return false;
	}

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
	public boolean equals(final Object obj) {

		return Schema.class.isInstance(obj) && super.equals(obj);

	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Schema.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((Schema) obj).getUniqueAttributePaths(), getUniqueAttributePaths())
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
			orderedAttributePaths = Lists.newLinkedList();
		}
	}

	@JsonIgnore
	private void setAllAttributePaths(final Collection<AttributePath> attributePathsArg) {
		ensureOrderedAttributePaths();

		if (!DMPPersistenceUtil.getAttributePathUtils().completeEquals(orderedAttributePaths, attributePathsArg)) {
			ensureAttributePaths();

			attributePaths.clear();
			orderedAttributePaths.clear();

			for (final AttributePath newAttributePath : attributePathsArg) {
				orderedAttributePaths.add(newAttributePath);
				attributePaths.add(newAttributePath);
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
		if (orderedAttributePaths == null) {
			final Optional<List<AttributePath>> paths = initializedAttributePaths(true);
			orderedAttributePaths = paths.or(Lists.<AttributePath>newLinkedList());
		}
	}

	private void tryInitializeOrderedAttributePaths() {
		final Optional<List<AttributePath>> paths = initializedAttributePaths(false);
		orderedAttributePaths = paths.orNull();
	}

	private Optional<List<AttributePath>> initializedAttributePaths(final boolean fromScratch) {
		if (orderedAttributePathsJson == null && !isOrderedAttributePathsInitialized) {

			if (attributePathsJsonString == null) {
				Schema.LOG.debug("attribute paths JSON is null for {}", getId());

				if (fromScratch) {
					orderedAttributePathsJson = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
					orderedAttributePaths = Lists.newLinkedList();

					isOrderedAttributePathsInitialized = true;
				}

				return Optional.fromNullable(orderedAttributePaths);
			}

			try {
				orderedAttributePaths = Lists.newLinkedList();
				orderedAttributePathsJson = DMPPersistenceUtil.getJSONArray(attributePathsJsonString);

				if (orderedAttributePathsJson != null) {

					for (final JsonNode attributePathIdNode : orderedAttributePathsJson) {
						final AttributePath attributePath = getAttributePath(attributePathIdNode.longValue());
						if (attributePath != null) {
							orderedAttributePaths.add(attributePath);
						}
					}
				}
			} catch (final DMPException e) {
				Schema.LOG.debug("couldn't parse attribute path JSON for attribute path '" + getId() + "'", e);
			}
			isOrderedAttributePathsInitialized = true;
		}

		return Optional.fromNullable(orderedAttributePaths);
	}

	private void refreshAttributePathsString() {
		if (orderedAttributePaths != null) {
			orderedAttributePathsJson = new ArrayNode(DMPPersistenceUtil.getJSONFactory());
			for (final AttributePath attributePath : orderedAttributePaths) {
				orderedAttributePathsJson.add(attributePath.getId());
			}
		}

		if (orderedAttributePathsJson != null && orderedAttributePathsJson.size() > 0) {
			attributePathsJsonString = orderedAttributePathsJson.toString();
		} else {
			attributePathsJsonString = null;
		}
	}
}