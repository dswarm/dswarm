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

import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Transformation}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyTransformation extends ProxyBasicFunction<Transformation> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created transformation, i.e., no updated or already existing transformation.
	 * 
	 * @param transformationArg a freshly created transformation
	 */
	public ProxyTransformation(final Transformation transformationArg) {

		super(transformationArg);
	}

	/**
	 * Creates a new proxy with the given real transformation and the type how the transformation was processed by the
	 * transformation persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param transformationArg a transformation that was processed by the transformation persistence service
	 * @param typeArg the type how this transformation was processed by the transformation persistence service
	 */
	public ProxyTransformation(final Transformation transformationArg, final RetrievalType typeArg) {

		super(transformationArg, typeArg);
	}
}
