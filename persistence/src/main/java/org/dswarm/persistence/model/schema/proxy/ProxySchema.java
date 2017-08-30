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
