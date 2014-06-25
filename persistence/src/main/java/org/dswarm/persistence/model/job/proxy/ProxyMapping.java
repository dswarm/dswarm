package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Mapping}s.
 *
 * @author tgaengler
 */
@XmlRootElement
public class ProxyMapping extends ProxyBasicDMPJPAObject<Mapping> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created mapping, i.e., no updated or already existing mapping.
	 *
	 * @param mappingArg a freshly created mapping
	 */
	public ProxyMapping(final Mapping mappingArg) {

		super(mappingArg);
	}

	/**
	 * Creates a new proxy with the given real mapping and the type how the mapping was processed by the mapping persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 *
	 * @param mappingArg a mapping that was processed by the mapping persistence service
	 * @param typeArg the type how this mapping was processed by the mapping persistence service
	 */
	public ProxyMapping(final Mapping mappingArg, final RetrievalType typeArg) {

		super(mappingArg, typeArg);
	}
}
