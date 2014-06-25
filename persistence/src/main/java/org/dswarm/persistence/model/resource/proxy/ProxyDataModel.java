package org.dswarm.persistence.model.resource.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * A proxy class for {@link DataModel}s.
 *
 * @author tgaengler
 */
@XmlRootElement
public class ProxyDataModel extends ProxyExtendedBasicDMPJPAObject<DataModel> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created data model, i.e., no updated or already existing data model.
	 *
	 * @param dataModelArg a freshly created data model
	 */
	public ProxyDataModel(final DataModel dataModelArg) {

		super(dataModelArg);
	}

	/**
	 * Creates a new proxy with the given real data model and the type how the data model was processed by the data model
	 * persistence service, e.g., {@link RetrievalType.CREATED}.
	 *
	 * @param dataModelArg a data model that was processed by the data model persistence service
	 * @param typeArg the type how this data model was processed by the data model persistence service
	 */
	public ProxyDataModel(final DataModel dataModelArg, final RetrievalType typeArg) {

		super(dataModelArg, typeArg);
	}
}
