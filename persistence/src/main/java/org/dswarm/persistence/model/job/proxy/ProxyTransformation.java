package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Transformation}s.
 *
 * @author tgaengler
 */
@XmlRootElement
public class ProxyTransformation extends ProxyBasicFunction<Transformation> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created transformation, i.e., no updated or already existing transformation.
	 *
	 * @param transformationArg a freshly created transformation
	 */
	public ProxyTransformation(final Transformation transformationArg) {

		super(transformationArg);
	}

	/**
	 * Creates a new proxy with the given real transformation and the type how the transformation was processed by the
	 * transformation persistence service, e.g., {@link RetrievalType.CREATED}.
	 *
	 * @param transformationArg a transformation that was processed by the transformation persistence service
	 * @param typeArg the type how this transformation was processed by the transformation persistence service
	 */
	public ProxyTransformation(final Transformation transformationArg, final RetrievalType typeArg) {

		super(transformationArg, typeArg);
	}
}
