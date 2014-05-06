package de.avgl.dmp.persistence.model.proxy;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Objects;

import de.avgl.dmp.persistence.model.DMPObject;

/**
 * The most abstract proxy POJO class, i.e., this class is intended for inheritance. It only provides a getter for the identifier
 * of the real object and basic #hashCode and #equals implementations (by identifier).<br/>
 * Note: these proxy object should only be utilised when information is retrieved via persistence services
 * 
 * @author tgaengler
 * @param <POJOCLASS> the proxied object class
 * @param <IDTYPE> the identifier type of the proxied object
 */
@XmlRootElement
public abstract class ProxyDMPObject<POJOCLASS extends DMPObject<IDTYPE>, IDTYPE> implements Serializable {

	protected final POJOCLASS	dmpObject;

	private final RetrievalType	type;

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created object, i.e., no updated or already existing object.
	 * 
	 * @param dmpObjectArg a freshly created object
	 */
	public ProxyDMPObject(final POJOCLASS dmpObjectArg) {

		dmpObject = dmpObjectArg;
		type = RetrievalType.CREATED;
	}

	/**
	 * Creates a new proxy with the given real object and the type how the object was processed by the persistence service, e.g.,
	 * {@link RetrievalType.CREATED}.
	 * 
	 * @param dmpObjectArg an object that was processed by a persistence service
	 * @param typeArg the type how this object was processed by the persistence service
	 */
	public ProxyDMPObject(final POJOCLASS dmpObjectArg, final RetrievalType typeArg) {

		dmpObject = dmpObjectArg;
		type = typeArg;
	}

	/**
	 * Gets the real object that was proxied by this object.
	 * 
	 * @return the real object that was proxied by this object
	 */
	public final POJOCLASS getObject() {

		return dmpObject;
	}

	/**
	 * Gets the type how the object was processed by the persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @return the type how the object was processed by the persistence service
	 */
	public RetrievalType getType() {

		return type;
	}

	/**
	 * Gets the identifier of the proxied object.
	 * 
	 * @return the identifier of the proxied object as the implemented identifier type of the real object
	 */
	public abstract IDTYPE getId();

	@Override
	public int hashCode() {

		if (dmpObject == null) {

			// TODO: [@tgaengler] is this correct/ok?

			return 0;
		}

		return Objects.hashCode(dmpObject.getId());
	}

	@Override
	public boolean equals(final Object obj) {

		return ProxyDMPObject.class.isInstance(obj) && Objects.equal(((ProxyDMPObject<?, ?>) obj).getId(), getId());

	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
