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

import org.dswarm.persistence.model.BasicDMPJPAObject;
import org.dswarm.persistence.util.DMPPersistenceUtil;

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
	private static final long			serialVersionUID	= 1L;

	/**
	 * The attribute path instance type, e.g., mapping attribute path instance (
	 * {@link AttributePathInstanceType#MappingAttributePathInstance}).
	 */
	// -> note: separate attribute for entity type is not necessary since Jackson will include this
	// property automatically, when serialising the object
	// however, it needs to be enabled - otherwise on could not serialise a JSON string to a POJO object
	// @JsonIgnore
	@XmlElement(name = "type")
	@Column(name = "ATTRIBUTE_PATH_INSTANCE_TYPE")
	@Enumerated(EnumType.STRING)
	private AttributePathInstanceType	attributePathInstanceType;

	@XmlElement(name = "attribute_path")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "ATTRIBUTE_PATH")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private AttributePath				attributePath;

	protected AttributePathInstance() {

		// just for JPA
	}

	/**
	 * Creates a new attribute path instance with the given attribute path instance type.
	 *
	 * @param attributePathInstanceTypeArg the type of the attribute path instance
	 */
	protected AttributePathInstance(final AttributePathInstanceType attributePathInstanceTypeArg) {

		attributePathInstanceType = attributePathInstanceTypeArg;
	}

	protected AttributePathInstance(final String uuid, final AttributePathInstanceType attributePathInstanceTypeArg) {

		super(uuid);
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
	public boolean completeEquals(final Object obj) {

		return AttributePathInstance.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((AttributePathInstance) obj).getAttributePathInstanceType(), getAttributePathInstanceType())
				&& DMPPersistenceUtil.getAttributePathUtils().completeEquals(((AttributePathInstance) obj).getAttributePath(), getAttributePath());
	}
}
