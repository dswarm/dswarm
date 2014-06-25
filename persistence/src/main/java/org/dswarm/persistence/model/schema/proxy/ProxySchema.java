package org.dswarm.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.Schema;

/**
 * A proxy class for {@link Schema}s.
 *
 * @author tgaengler
 */
@XmlRootElement
public class ProxySchema extends ProxyBasicDMPJPAObject<Schema> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created schema, i.e., no updated or already existing schema.
	 *
	 * @param schemaArg a freshly created schema
	 */
	public ProxySchema(final Schema schemaArg) {

		super(schemaArg);
	}

	/**
	 * Creates a new proxy with the given real schema and the type how the schema was processed by the schema persistence service,
	 * e.g., {@link RetrievalType.CREATED}.
	 *
	 * @param schemaArg a schema that was processed by the schema persistence service
	 * @param typeArg the type how this schema was processed by the schema persistence service
	 */
	public ProxySchema(final Schema schemaArg, final RetrievalType typeArg) {

		super(schemaArg, typeArg);
	}
}
