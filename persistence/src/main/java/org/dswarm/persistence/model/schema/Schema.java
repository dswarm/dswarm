package org.dswarm.persistence.model.schema;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matchers;

import ch.lambdaj.Lambda;

import com.google.common.collect.Sets;

import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A data schema is a collection of {@link AttributePath}s and a record class ({@link Clasz}).
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

	/**
	 * All attributes paths of the schema.
	 */
	// @ManyToMany(mappedBy = "schemas", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE,
	// CascadeType.PERSIST, CascadeType.REFRESH })
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinTable(name = "SCHEMAS_ATTRIBUTE_PATHS", joinColumns = { @JoinColumn(name = "SCHEMA_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName = "ID") })
	@XmlElement(name = "attribute_paths")
	private Set<AttributePath>	attributePaths;

	/**
	 * The record class of the schema.
	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "RECORD_CLASS")
	@XmlElement(name = "record_class")
	private Clasz				recordClass;

	/**
	 * Gets all attribute paths of the schema.
	 *
	 * @return all attribute paths of the schema
	 */
	public Set<AttributePath> getAttributePaths() {

		return attributePaths;
	}

	/**
	 * Sets all attribute paths of the schema.
	 *
	 * @param attributePathsArg all attribute paths of the schema
	 */
	public void setAttributePaths(final Set<AttributePath> attributePathsArg) {

		if (attributePathsArg == null && attributePaths != null) {

			// remove schema from attribute paths, if schema will be prepared for removal

			// for (final AttributePath attributePath : attributePaths) {
			//
			// attributePath.removeSchema(this);
			// }

			attributePaths.clear();
		}

		// attributePaths = attributePathsArg;

		if (attributePathsArg != null) {

			if (attributePaths == null) {

				attributePaths = Sets.newCopyOnWriteArraySet();
			}

			if (!DMPPersistenceUtil.getAttributePathUtils().completeEquals(attributePaths, attributePathsArg)) {

				attributePaths.clear();
				attributePaths.addAll(attributePathsArg);
			}
			//
			// for (final AttributePath attributePath : attributePathsArg) {
			//
			// attributePath.addSchema(this);
			// }
		}
	}

	/**
	 * Gets the attribute path for the given attribute path identifier.
	 *
	 * @param id an attribute path identifier
	 * @return that matched attribute path or null
	 */
	public AttributePath getAttributePath(final Long id) {

		if (id == null) {

			return null;
		}

		if (attributePaths == null || attributePaths.isEmpty()) {

			return null;
		}

		final List<AttributePath> attributePathsFiltered = Lambda.filter(Lambda.having(Lambda.on(AttributePath.class).getId(), Matchers.equalTo(id)),
				attributePaths);

		if (attributePathsFiltered == null || attributePathsFiltered.isEmpty()) {

			return null;
		}

		return attributePathsFiltered.get(0);
	}

	/**
	 * Adds a new attribute path to the collection of attribute paths of this schema.<br>
	 * Created by: tgaengler
	 *
	 * @param attributePath a new attribute path
	 */
	public void addAttributePath(final AttributePath attributePath) {

		if (attributePath != null) {

			if (attributePaths == null) {

				attributePaths = Sets.newCopyOnWriteArraySet();
			}

			if (!attributePaths.contains(attributePath)) {

				attributePaths.add(attributePath);
				// attributePath.addSchema(this);
			}
		}
	}

	/**
	 * Removes an existing attribute path from the collection of attribute paths of this export schema.<br>
	 * Created by: tgaengler
	 *
	 * @param attributePath an existing attribute path that should be removed
	 */
	public void removeAttributePath(final AttributePath attributePath) {

		if (attributePaths != null && attributePath != null && attributePaths.contains(attributePath)) {

			attributePaths.remove(attributePath);

			// attributePath.removeSchema(this);
		}
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

	@Override
	public boolean equals(final Object obj) {

		return Schema.class.isInstance(obj) && super.equals(obj);

	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Schema.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((Schema) obj).getAttributePaths(), getAttributePaths())
				&& DMPPersistenceUtil.getClaszUtils().completeEquals(((Schema) obj).getRecordClass(), getRecordClass());
	}
}
