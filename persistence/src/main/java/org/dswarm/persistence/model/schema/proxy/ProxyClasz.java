package org.dswarm.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.Clasz;

/**
 * A proxy class for {@link Clasz}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyClasz extends ProxyAdvancedDMPJPAObject<Clasz> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created class, i.e., no updated or already existing class.
	 * 
	 * @param classArg a freshly created class
	 */
	public ProxyClasz(final Clasz classArg) {

		super(classArg);
	}

	/**
	 * Creates a new proxy with the given real class and the type how the class was processed by the class persistence service,
	 * e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param classArg a class that was processed by the class persistence service
	 * @param typeArg the type how this class was processed by the class persistence service
	 */
	public ProxyClasz(final Clasz classArg, final RetrievalType typeArg) {

		super(classArg, typeArg);
	}
}
