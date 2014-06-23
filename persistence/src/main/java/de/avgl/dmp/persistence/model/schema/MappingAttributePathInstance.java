package de.avgl.dmp.persistence.model.schema;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Objects;

import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DiscriminatorValue("MappingAttributePathInstance")
@Table(name = "MAPPING_ATTRIBUTE_PATH_INSTANCE")
public class MappingAttributePathInstance extends AttributePathInstance {

	private static final Logger	LOG					= LoggerFactory.getLogger(MappingAttributePathInstance.class);

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The filter of this mapping attribute path instance.
	 */
	@XmlElement(name = "filter")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "FILTER")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Filter				filter;

	/**
	 * The (optional) ordinal of this mapping attribute path instance.
	 */
	@XmlElement(name = "ordinal")
	@Column(name = "ORDINAL")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer				ordinal;

	/**
	 * Creates a new mapping attribute path instance.
	 */
	public MappingAttributePathInstance() {

		super(AttributePathInstanceType.MappingAttributePathInstance);
	}

	/**
	 * Gets the filter of the mapping attribute path instance.
	 * 
	 * @return the filter of the mapping attribute path instance
	 */
	public Filter getFilter() {

		return filter;
	}

	/**
	 * Sets the filter of the mapping attribute path instance.
	 * 
	 * @param filterArg a new filter
	 */
	public void setFilter(final Filter filterArg) {

		filter = filterArg;
	}

	public Integer getOrdinal() {

		return ordinal;
	}

	public void setOrdinal(final Integer ordinalArg) {

		if (ordinalArg != null && ordinalArg.intValue() < 0) {

			throw new IllegalArgumentException("only positive integer values are allowed for ordinals");
		}

		ordinal = ordinalArg;
	}

	@Override
	public boolean equals(final Object obj) {

		return MappingAttributePathInstance.class.isInstance(obj) && super.equals(obj);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return MappingAttributePathInstance.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((MappingAttributePathInstance) obj).getOrdinal(), getOrdinal())
				&& DMPPersistenceUtil.getFilterUtils().completeEquals(((MappingAttributePathInstance) obj).getFilter(), getFilter());
	}
}
