/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
