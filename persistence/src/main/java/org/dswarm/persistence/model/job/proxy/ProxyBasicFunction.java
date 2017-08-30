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
package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * An abstract proxy class for functions.
 * 
 * @author tgaengler
 */
@XmlRootElement
public abstract class ProxyBasicFunction<POJOCLASS extends Function> extends ProxyExtendedBasicDMPJPAObject<POJOCLASS> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created function, i.e., no updated or already existing function.
	 * 
	 * @param basicFunctionArg a freshly created function
	 */
	public ProxyBasicFunction(final POJOCLASS basicFunctionArg) {

		super(basicFunctionArg);
	}

	/**
	 * Creates a new proxy with the given real function and the type how the function was processed by the function persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param basicFunctionArg a function that was processed by the function persistence service
	 * @param typeArg the type how this function was processed by the function persistence service
	 */
	public ProxyBasicFunction(final POJOCLASS basicFunctionArg, final RetrievalType typeArg) {

		super(basicFunctionArg, typeArg);
	}
}
