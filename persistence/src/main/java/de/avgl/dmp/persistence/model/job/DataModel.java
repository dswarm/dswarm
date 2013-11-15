package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;

/**
 * @author tgaengler
 */
@XmlRootElement
public class DataModel extends BasicDMPObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private String				description			= null;

	@XmlElement(name = "data_resource")
	private Resource			dataResource		= null;

	private Configuration		configuration		= null;

	private Schema				schema				= null;

	public DataModel(final String id) {

		super(id);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String descriptionArg) {
		
		description = descriptionArg;
	}

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
}
