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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.util.DMPPersistenceUtil;

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
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
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
	protected MappingAttributePathInstance() {

		super(AttributePathInstanceType.MappingAttributePathInstance);
	}

	public MappingAttributePathInstance(final String uuid) {

		super(uuid, AttributePathInstanceType.MappingAttributePathInstance);
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
	public boolean completeEquals(final Object obj) {

		return MappingAttributePathInstance.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((MappingAttributePathInstance) obj).getOrdinal(), getOrdinal())
				&& DMPPersistenceUtil.getFilterUtils().completeEquals(((MappingAttributePathInstance) obj).getFilter(), getFilter());
	}
}
