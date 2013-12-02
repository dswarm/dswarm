package de.avgl.dmp.persistence.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlID;

import de.avgl.dmp.persistence.model.job.DMPObject;

@MappedSuperclass
// @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public abstract class DMPJPAObject extends DMPObject<Long> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
	@Id
	@XmlID
	@Access(AccessType.FIELD)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long	id;

	@Override
	public Long getId() {

		return id;
	}
}
