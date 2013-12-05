package de.avgl.dmp.persistence.model.resource;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.utils.DMPJPAObjectReferenceSerializer;

/**
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

	@XmlElement(name = "data_resource")
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "DATA_RESOURCE")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	@XmlIDREF
	private Resource			dataResource		= null;

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "CONFIGURATION")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	@XmlIDREF
	private Configuration		configuration		= null;

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "SCHEMA")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonSerialize(using = DMPJPAObjectReferenceSerializer.class)
	@XmlIDREF
	private Schema				schema				= null;

	public Resource getDataResource() {

		return dataResource;
	}

	public void setDataResource(final Resource dataResourceArg) {

		dataResource = dataResourceArg;
	}

	public Configuration getConfiguration() {

		return configuration;
	}

	public void setConfiguration(final Configuration configurationArg) {

		configuration = configurationArg;
	}

	public Schema getSchema() {

		return schema;
	}

	public void setSchema(final Schema schemaArg) {

		schema = schemaArg;
	}
	
	@Override
	public boolean equals(final Object obj) {

		if (!DataModel.class.isInstance(obj)) {

			return false;
		}

		return super.equals(obj);
	}
}
