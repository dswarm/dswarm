package de.avgl.dmp.persistence.model.schema;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Objects;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @Type(value = MappingAttributePathInstance.class, name = "MappingAttributePathInstance") })
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ATTRIBUTE_PATH_INSTANCE_TYPE", discriminatorType = DiscriminatorType.STRING)
@Table(name = "ATTRIBUTE_PATH_INSTANCE")
public abstract class AttributePathInstance extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long						serialVersionUID	= 1L;

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(AttributePathInstance.class);

	/**
	 * The attribute path instance type, e.g., mapping attribute path instance (
	 * {@link AttributePathInstanceType#MappingAttributePathInstance}).
	 */
	@XmlElement(name = "type")
	// -> note: separate attribute for entity type is not necessary since Jackson will include this
	// property automatically, when serialising the object
	// however, it needs to be enabled - otherwise on could not serialise a JSON string to a POJO object
	// @JsonIgnore
	@Column(name = "ATTRIBUTE_PATH_INSTANCE_TYPE")
	@Enumerated(EnumType.STRING)
	private AttributePathInstanceType				attributePathInstanceType;

	@XmlElement(name = "attribute_path")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "ATTRIBUTE_PATH")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private AttributePath							attributePath;

	public AttributePathInstance() {

		// just for JPA
	}

	/**
	 * Creates a new attribute path instance with the given attribute path instance type.
	 * 
	 * @param attributePathInstanceTypeArg the type of the attribute path instance
	 */
	public AttributePathInstance(final AttributePathInstanceType attributePathInstanceTypeArg) {

		attributePathInstanceType = attributePathInstanceTypeArg;
	}

	public AttributePath getAttributePath() {

		return attributePath;
	}

	public void setAttributePath(final AttributePath attributePathArg) {

		attributePath = attributePathArg;
	}

	public AttributePathInstanceType getAttributePathInstanceType() {

		return attributePathInstanceType;
	}

	@Override
	public boolean equals(final Object obj) {

		return AttributePathInstance.class.isInstance(obj) && super.equals(obj);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return AttributePathInstance.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((AttributePathInstance) obj).getAttributePathInstanceType(), getAttributePathInstanceType())
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((AttributePathInstance) obj).getAttributePath(), getAttributePath());
	}
}
