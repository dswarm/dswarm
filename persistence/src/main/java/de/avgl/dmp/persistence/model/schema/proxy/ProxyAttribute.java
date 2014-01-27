package de.avgl.dmp.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.RetrievalType;
import de.avgl.dmp.persistence.model.schema.Attribute;

/**
 * A proxy class for {@link Attribute}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyAttribute extends ProxyAdvancedDMPJPAObject<Attribute> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created attribute, i.e., no updated or already existing attribute.
	 * 
	 * @param attributeArg a freshly created attribute
	 */
	public ProxyAttribute(final Attribute attributeArg) {

		super(attributeArg);
	}

	/**
	 * Creates a new proxy with the given real attribute and the type how the attribute was processed by the attribute persistence service, e.g.,
	 * {@link RetrievalType.CREATED}.
	 * 
	 * @param attributeArg an attribute that was processed by the attribute persistence service
	 * @param typeArg the type how this attribute was processed by the attribute persistence service
	 */
	public ProxyAttribute(final Attribute attributeArg, final RetrievalType typeArg) {

		super(attributeArg, typeArg);
	}
}
