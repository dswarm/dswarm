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
package org.dswarm.persistence.model.resource;

import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * A (input) data model consists of a {@link Resource} and a {@link Configuration} that has been applied to the data resource to
 * produce the data model. Thereby, a schema that describes the relationships between the data was derived or manually set.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "DATA_MODEL")
public class DataModel extends ExtendedBasicDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The data resource (origin or raw data).
	 */
	@XmlElement(name = "data_resource")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "DATA_RESOURCE")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private Resource			dataResource;

	/**
	 * The related configuration.
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "CONFIGURATION")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private Configuration		configuration;

	/**
	 * The data schema.
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "DATA_SCHEMA")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	// @JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	// @XmlIDREF
	private Schema				schema;

	/**
	 * The flag that indicated, whether the data model is deprecated atm, or not
	 */
	@Access(AccessType.FIELD)
	@Column(name = "DEPRECATED", columnDefinition = "TINYINT(1)")
	private boolean deprecated;

	public DataModel(final String uuidArg) {

		super(uuidArg);
	}

	protected DataModel() {

	}

	/**
	 * Gets the data resource.
	 *
	 * @return the data resource
	 */
	public Resource getDataResource() {

		return dataResource;
	}

	/**
	 * Sets the data resource.
	 *
	 * @param dataResourceArg a new data resource
	 */
	public void setDataResource(final Resource dataResourceArg) {

		dataResource = dataResourceArg;
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	public Configuration getConfiguration() {

		return configuration;
	}

	/**
	 * Sets the configuration.
	 *
	 * @param configurationArg a new configuration
	 */
	public void setConfiguration(final Configuration configurationArg) {

		configuration = configurationArg;
	}

	/**
	 * Gets the data schema.
	 *
	 * @return the data schema
	 */
	public Schema getSchema() {

		return schema;
	}

	/**
	 * Sets the data schema
	 *
	 * @param schemaArg a new data schema
	 */
	public void setSchema(final Schema schemaArg) {

		schema = schemaArg;
	}

	public boolean isDeprecated() {

		return deprecated;
	}

	public void setDeprecated(final boolean deprecatedArg) {

		this.deprecated = deprecatedArg;
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return DataModel.class.isInstance(obj) && super.completeEquals(obj)
				&& DMPPersistenceUtil.getResourceUtils().completeEquals(((DataModel) obj).getDataResource(), getDataResource())
				&& DMPPersistenceUtil.getConfigurationUtils().completeEquals(((DataModel) obj).getConfiguration(), getConfiguration())
				&& DMPPersistenceUtil.getSchemaUtils().completeEquals(((DataModel) obj).getSchema(), getSchema())
				&& Objects.equals(((DataModel) obj).isDeprecated(), isDeprecated());
	}
}
