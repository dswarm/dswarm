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
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Function}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyFunction extends ProxyBasicFunction<Function> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created function, i.e., no updated or already existing function.
	 * 
	 * @param functionArg a freshly created function
	 */
	public ProxyFunction(final Function functionArg) {

		super(functionArg);
	}

	/**
	 * Creates a new proxy with the given real function and the type how the function was processed by the function persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param functionArg a function that was processed by the function persistence service
	 * @param typeArg the type how this function was processed by the function persistence service
	 */
	public ProxyFunction(final Function functionArg, final RetrievalType typeArg) {

		super(functionArg, typeArg);
	}
}
