package de.avgl.dmp.persistence.model.job;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.DMPUUIDObject;

/**
 * @author tgaengler
 */
@XmlRootElement
// @Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
// @Table(name = "SCHEMA")
public class Schema extends DMPUUIDObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	// @Column(name = "NAME")
	private String				name				= null;
	
	/**
	 * All attributes of the attribute path
	 */
	// @ManyToMany(mappedBy = "schemas", fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@XmlElement(name = "attribute_paths")
	private Set<AttributePath>			attributePaths		= null;
	
	@XmlElement(name = "record_class")
	private Clasz recordClass = null;

	public String getName() {
		
		return name;
	}

	public void setName(final String nameArg) {
		
		name = nameArg;
	}
	
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

//			for (final AttributePath attributePath : attributePaths) {
//
//				attributePath.removeSchema(this);
//			}
		}

		attributePaths = attributePathsArg;

//		if (attributePathsArg != null) {
//
//			for (final AttributePath attributePath : attributePathsArg) {
//
//				attributePath.addSchema(this);
//			}
//		}
	}

	public AttributePath getAttributePath(final String id) {

		if (id == null) {

			return null;
		}

		if (this.attributePaths == null || this.attributePaths.isEmpty()) {

			return null;
		}

		final List<AttributePath> attributePathsFiltered = filter(having(on(AttributePath.class).getId(), equalTo(id)), this.attributePaths);

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

				attributePaths = Sets.newLinkedHashSet();
			}

			if (!attributePaths.contains(attributePath)) {

				attributePaths.add(attributePath);
				//attributePath.addSchema(this);
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

			//attributePath.removeSchema(this);
		}
	}

	
	public Clasz getRecordClass() {
		
		return recordClass;
	}

	
	public void setRecordClass(final Clasz recordClassArg) {
		
		this.recordClass = recordClassArg;
	}
}
