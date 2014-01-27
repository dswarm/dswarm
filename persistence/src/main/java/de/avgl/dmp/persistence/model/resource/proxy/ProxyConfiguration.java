package de.avgl.dmp.persistence.model.resource.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.RetrievalType;
import de.avgl.dmp.persistence.model.resource.Configuration;

/**
 * A proxy class for {@link Configuration}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyConfiguration extends ProxyExtendedBasicDMPJPAObject<Configuration> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created configuration, i.e., no updated or already existing configuration.
	 * 
	 * @param configurationArg a freshly created configuration
	 */
	public ProxyConfiguration(final Configuration configurationArg) {

		super(configurationArg);
	}

	/**
	 * Creates a new proxy with the given real configuration and the type how the configuration was processed by the configuration
	 * persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param configurationArg a configuration that was processed by the configuration persistence service
	 * @param typeArg the type how this configuration was processed by the configuration persistence service
	 */
	public ProxyConfiguration(final Configuration configurationArg, final RetrievalType typeArg) {

		super(configurationArg, typeArg);
	}
}
