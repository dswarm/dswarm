package de.avgl.dmp.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.proxy.ProxyBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.RetrievalType;
import de.avgl.dmp.persistence.model.schema.AttributePathInstance;

/**
 * An abstract proxy class for {@link AttributePathInstance}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public abstract class ProxyAttributePathInstance<POJOCLASS extends AttributePathInstance> extends ProxyBasicDMPJPAObject<POJOCLASS> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created attribute path instance, i.e., no updated or already existing
	 * attribute path instance.
	 * 
	 * @param attributePathInstanceArg a freshly created attribute path instance
	 */
	public ProxyAttributePathInstance(final POJOCLASS attributePathInstanceArg) {

		super(attributePathInstanceArg);
	}

	/**
	 * Creates a new proxy with the given real attribute path instance and the type how the attribute path instance was processed
	 * by the attribute path instance persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param attributePathInstanceArg a attribute path instance that was processed by the attribute path instance persistence
	 *            service
	 * @param typeArg the type how this attribute path instance was processed by the attribute path instance persistence service
	 */
	public ProxyAttributePathInstance(final POJOCLASS attributePathInstanceArg, final RetrievalType typeArg) {

		super(attributePathInstanceArg, typeArg);
	}
}
