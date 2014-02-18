package de.avgl.dmp.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import de.avgl.dmp.persistence.model.proxy.RetrievalType;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;

/**
 * A proxy class for {@link MappingAttributePathInstance}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyMappingAttributePathInstance extends ProxyAttributePathInstance<MappingAttributePathInstance> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created mapping attribute path instance, i.e., no updated or already
	 * existing mapping attribute path instance.
	 * 
	 * @param mappingAttributePathInstanceArg a freshly created mapping attribute path instance
	 */
	public ProxyMappingAttributePathInstance(final MappingAttributePathInstance mappingAttributePathInstanceArg) {

		super(mappingAttributePathInstanceArg);
	}

	/**
	 * Creates a new proxy with the given real mapping attribute path instance and the type how the mapping attribute path
	 * instance was processed by the mapping attribute path instance persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param mappingAttributePathInstanceArg a mapping attribute path instance that was processed by the mapping attribute path
	 *            instance persistence service
	 * @param typeArg the type how this mapping attribute path instance was processed by the mapping attribute path instance
	 *            persistence service
	 */
	public ProxyMappingAttributePathInstance(final MappingAttributePathInstance mappingAttributePathInstanceArg, final RetrievalType typeArg) {

		super(mappingAttributePathInstanceArg, typeArg);
	}
}
