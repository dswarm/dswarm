package de.avgl.dmp.persistence.model;

import javax.persistence.Id;
import javax.xml.bind.annotation.XmlID;

import de.avgl.dmp.persistence.model.job.DMPObject;

//@MappedSuperclass
// @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public abstract class DMPUUIDObject extends DMPObject<String> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
	@Id
	@XmlID
	private String	id;

	@Override
	public String getId() {

		return id;
	}
	
	public void setId(final String idArg) {
		
		id = idArg;
	}
}
