package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Filter}s.
 *
 * @author tgaengler
 */
@XmlRootElement
public class ProxyFilter extends ProxyBasicDMPJPAObject<Filter> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created filter, i.e., no updated or already existing filter.
	 *
	 * @param filterArg a freshly created filter
	 */
	public ProxyFilter(final Filter filterArg) {

		super(filterArg);
	}

	/**
	 * Creates a new proxy with the given real filter and the type how the filter was processed by the filter persistence service,
	 * e.g., {@link RetrievalType.CREATED}.
	 *
	 * @param filterArg a filter that was processed by the filter persistence service
	 * @param typeArg the type how this filter was processed by the filter persistence service
	 */
	public ProxyFilter(final Filter filterArg, final RetrievalType typeArg) {

		super(filterArg, typeArg);
	}
}
