package de.avgl.dmp.persistence.model.job;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPUUIDObject;

/**
 * @author tgaengler
 */
@XmlRootElement
//@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Table(name = "ATTRIBUTE_PATH")
public class AttributePath extends DMPUUIDObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * All attributes of the attribute path as ordered list
	 */
	//@ManyToMany(mappedBy = "attributePaths", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	private List<Attribute>		attributes			= null;

	/**
	 * All schemas that utilise this attribute path
	 */
	//@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	//@JoinTable(name = "SCHEMAS_ATTRIBUTE_PATHS", joinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "SCHEMA_ID", referencedColumnName = "ID") })
	private Set<Schema>			schemas				= null;

	public AttributePath() {

	}

	public AttributePath(final LinkedList<Attribute> attributesArg) {

		attributes = attributesArg;
	}

	public List<Attribute> getAttributes() {

		return attributes;
	}

	public void setAttributes(final List<Attribute> attributesArg) {

		attributes = attributesArg;

		if (attributesArg == null && attributes != null) {

			// remove attribute path from attribute, if attribute path will be prepared for removal

//			for (final Attribute attribute : attributes) {
//
//				attribute.removeAttributePath(this);
//			}
		}

		attributes = attributesArg;

//		if (attributesArg != null) {
//
//			for (final Attribute attribute : attributesArg) {
//
//				attribute.addAttributePath(this);
//			}
//		}
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

				attributes = Lists.newLinkedList();
			}

			// if (!attributes.contains(attributeArg)) {

			attributes.add(attributeArg);

//			final int attributeIndex = attributes.lastIndexOf(attributeArg);
//
//			attributeArg.addAttributePath(this, attributeIndex);
			// }
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

				attributes = Lists.newLinkedList();
			}

			if (!attributeArg.equals(attributes.get(attributeIndex))) {

				attributes.add(attributeArg);

//				final int attributeIndex2 = attributes.lastIndexOf(attributeArg);
//
//				attributeArg.addAttributePath(this, attributeIndex2);
			}
		}
	}

	/**
	 * Removes an existing attribute from this attribute path.<br>
	 * Created by: tgaengler
	 * 
	 * @param attribute an existing attribute that should be removed
	 */
	public void removeAttribute(final Attribute attribute) {

		if (attributes != null && attribute != null && attributes.contains(attribute)) {

			while (attributes.contains(attribute)) {

				attributes.remove(attribute);
			}

//			attribute.removeAttributePath(this);
		}
	}

	public Set<Schema> getSchemas() {

		return schemas;
	}

	public void setSchemas(final Set<Schema> schemasArg) {

		if (schemasArg == null && schemas != null) {

			// remove attribute path from schema, if attribute path, will be prepared for removal

			for (final Schema schema : schemas) {

				schema.removeAttributePath(this);
			}
		}

		schemas = schemasArg;

		if (schemasArg != null) {

			for (final Schema schema : schemasArg) {

				schema.addAttributePath(this);
			}
		}
	}

	/**
	 * Adds a new schema to the collection of schemas of this attribute path.<br>
	 * Created by: tgaengler
	 * 
	 * @param schema a new schema
	 */
	public void addSchema(final Schema schema) {

		if (schema != null) {

			if (schemas == null) {

				schemas = Sets.newLinkedHashSet();
			}

			if (!schemas.contains(schema)) {

				schemas.add(schema);
				schema.addAttributePath(this);
			}
		}
	}

	/**
	 * Removes an existing schema from the collection of schemas of this attribute path.<br>
	 * Created by: tgaengler
	 * 
	 * @param schema an existing schema that should be removed
	 */
	public void removeSchema(final Schema schema) {

		if (schemas != null && schema != null && schemas.contains(schema)) {

			schemas.remove(schema);

			schema.removeAttributePath(this);
		}
	}

	public String toAttributePath() {

		if (null == attributes) {

			return null;
		}

		if (attributes.isEmpty()) {

			return null;
		}

		final StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (final Attribute attribute : attributes) {

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
}
