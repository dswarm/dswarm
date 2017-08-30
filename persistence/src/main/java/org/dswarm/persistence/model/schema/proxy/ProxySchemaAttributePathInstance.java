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

import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;

/**
 * A proxy class for {@link SchemaAttributePathInstance}s.
 * 
 * @author polowins - adapted from MappingSchemaAttributePathInstance
 */
@XmlRootElement
public class ProxySchemaAttributePathInstance extends ProxyAttributePathInstance<SchemaAttributePathInstance> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created schema attribute path instance, i.e., no updated or already
	 * existing schema attribute path instance.
	 * 
	 * @param schemaAttributePathInstanceArg a freshly created schema attribute path instance
	 */
	public ProxySchemaAttributePathInstance(final SchemaAttributePathInstance schemaAttributePathInstanceArg) {

		super(schemaAttributePathInstanceArg);
	}

	/**
	 * Creates a new proxy with the given real schema attribute path instance and the type how the schema attribute path
	 * instance was processed by the schema attribute path instance persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param schemaAttributePathInstanceArg a schema attribute path instance that was processed by the schema attribute path
	 *            instance persistence service
	 * @param typeArg the type how this schema attribute path instance was processed by the schema attribute path instance
	 *            persistence service
	 */
	public ProxySchemaAttributePathInstance(final SchemaAttributePathInstance schemaAttributePathInstanceArg, final RetrievalType typeArg) {

		super(schemaAttributePathInstanceArg, typeArg);
	}
}