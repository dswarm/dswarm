package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Component}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyComponent extends ProxyExtendedBasicDMPJPAObject<Component> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created component, i.e., no updated or already existing component.
	 * 
	 * @param componentArg a freshly created component
	 */
	public ProxyComponent(final Component componentArg) {

		super(componentArg);
	}

	/**
	 * Creates a new proxy with the given real component and the type how the component was processed by the component persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param componentArg a component that was processed by the component persistence service
	 * @param typeArg the type how this component was processed by the component persistence service
	 */
	public ProxyComponent(final Component componentArg, final RetrievalType typeArg) {

		super(componentArg, typeArg);
	}
}
