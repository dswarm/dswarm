package org.dswarm.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.AttributePath;

/**
 * A proxy class for {@link AttributePath}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyAttributePath extends ProxyDMPJPAObject<AttributePath> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created attribute path, i.e., no updated or already existing attribute path.
	 * 
	 * @param attributePathArg a freshly created attribute path
	 */
	public ProxyAttributePath(final AttributePath attributePathArg) {

		super(attributePathArg);
	}

	/**
	 * Creates a new proxy with the given real attribute path and the type how the attribute path was processed by the attribute
	 * path persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param attributePathArg a attribute path that was processed by the attribute path persistence service
	 * @param typeArg the type how this attribute path was processed by the attribute path persistence service
	 */
	public ProxyAttributePath(final AttributePath attributePathArg, final RetrievalType typeArg) {

		super(attributePathArg, typeArg);
	}
}
