package org.dswarm.persistence.model.resource.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Resource;

/**
 * A proxy class for {@link Resource}s.
 *
 * @author tgaengler
 */
@XmlRootElement
public class ProxyResource extends ProxyExtendedBasicDMPJPAObject<Resource> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created data resource, i.e., no updated or already existing data resource.
	 *
	 * @param resourceArg a freshly created data resource
	 */
	public ProxyResource(final Resource resourceArg) {

		super(resourceArg);
	}

	/**
	 * Creates a new proxy with the given real data resource and the type how the data resource was processed by the data resource
	 * persistence service, e.g., {@link RetrievalType.CREATED}.
	 *
	 * @param resourceArg a data resource that was processed by the data resource persistence service
	 * @param typeArg the type how this data resource was processed by the data resource persistence service
	 */
	public ProxyResource(final Resource resourceArg, final RetrievalType typeArg) {

		super(resourceArg, typeArg);
	}
}
