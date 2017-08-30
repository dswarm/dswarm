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

/**
 * @author polowins based on MappingAttributePathInstance
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DiscriminatorValue("SchemaAttributePathInstance")
@Table(name = "SCHEMA_ATTRIBUTE_PATH_INSTANCE")
public class SchemaAttributePathInstance extends AttributePathInstance {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaAttributePathInstance.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The optional subschema of the schema attribute path instance. A sub-schema can
	 * further prescribe how the (complex) value of the last attribute of an attribute path
	 * is meant to be structured.
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "SUB_SCHEMA")
	@XmlElement(name = "sub_schema")
	private Schema subSchema;

	/**
	 * Indicates whether the attribute path should always have a value/values (or not)
	 */
	@XmlElement(name = "required")
	@Column(name = "REQUIRED")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean required;

	/**
	 * Indicates whether multiple values for the attribute path of this SAPI can occur (or not)
	 */
	@XmlElement(name = "multivalue")
	@Column(name = "MULTIVALUE")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean multivalue;

	/**
	 * Creates a new mapping attribute path instance.
	 */
	protected SchemaAttributePathInstance() {

		super(AttributePathInstanceType.SchemaAttributePathInstance);
	}

	public SchemaAttributePathInstance(final String uuid) {

		super(uuid, AttributePathInstanceType.SchemaAttributePathInstance);
	}

	/**
	 * @return the sub-schema of the mapping attribute path instance
	 * or null if no sub-schema was defined
	 */
	public Schema getSubSchema() {

		return subSchema;
	}

	/**
	 * Set a sub-schema for the attribute path instance to further prescribe
	 * how the (complex) value of the last attribute of this path is meant to be structured.
	 *
	 * @param subSchema - the Schema to be used as as sub-schema.
	 */
	public void setSubSchema(final Schema subSchema) {

		this.subSchema = subSchema;
	}

	public Boolean isRequired() {

		return required;
	}

	public void setRequired(final Boolean required) {

		this.required = required;
	}

	public Boolean isMultivalue() {

		return multivalue;
	}

	public void setMultivalue(final Boolean multivalue) {

		this.multivalue = multivalue;
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return SchemaAttributePathInstance.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((SchemaAttributePathInstance) obj).getSubSchema(), getSubSchema())
				&& Objects.equal(((SchemaAttributePathInstance) obj).isRequired(), isRequired())
				&& Objects.equal(((SchemaAttributePathInstance) obj).isMultivalue(), isMultivalue());
	}
}
