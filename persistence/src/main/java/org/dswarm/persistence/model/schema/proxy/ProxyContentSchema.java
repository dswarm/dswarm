package org.dswarm.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;

/**
 * A proxy class for {@link org.dswarm.persistence.model.schema.ContentSchema}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyContentSchema extends ProxyBasicDMPJPAObject<ContentSchema> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created content schema, i.e., no updated or already existing content schema.
	 * 
	 * @param contentSchemaArg a freshly created content schema
	 */
	public ProxyContentSchema(final ContentSchema contentSchemaArg) {

		super(contentSchemaArg);
	}

	/**
	 * Creates a new proxy with the given real content schema and the type how the content schema was processed by the content
	 * schema persistence service, e.g., {@link org.dswarm.persistence.model.proxy.RetrievalType.CREATED}.
	 * 
	 * @param contentSchemaArg a content schema that was processed by the content schema persistence service
	 * @param typeArg the type how this content schema was processed by the content schema persistence service
	 */
	public ProxyContentSchema(final ContentSchema contentSchemaArg, final RetrievalType typeArg) {

		super(contentSchemaArg, typeArg);
	}
}
