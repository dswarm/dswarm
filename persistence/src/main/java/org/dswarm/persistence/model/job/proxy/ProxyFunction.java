package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Function}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyFunction extends ProxyBasicFunction<Function> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created function, i.e., no updated or already existing function.
	 * 
	 * @param functionArg a freshly created function
	 */
	public ProxyFunction(final Function functionArg) {

		super(functionArg);
	}

	/**
	 * Creates a new proxy with the given real function and the type how the function was processed by the function persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param functionArg a function that was processed by the function persistence service
	 * @param typeArg the type how this function was processed by the function persistence service
	 */
	public ProxyFunction(final Function functionArg, final RetrievalType typeArg) {

		super(functionArg, typeArg);
	}
}
