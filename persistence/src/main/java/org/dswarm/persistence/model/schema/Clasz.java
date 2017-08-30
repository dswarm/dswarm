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
package org.dswarm.persistence.model.schema;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.AdvancedDMPJPAObject;

/**
 * A class is a type. In a graph a node or edge can have a type, e.g., foaf:Document.
 *
 * @author tgaengler
 */
@XmlRootElement
@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CLASS")
public class Clasz extends AdvancedDMPJPAObject {

	private static final Logger LOG = LoggerFactory.getLogger(Clasz.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates new class with no identifier.
	 */
	protected Clasz() {

		//super(null);
	}

	/**
	 * Creates a new class with the given identifier.
	 *
	 * @param uri a class identifier
	 */
	public Clasz(final String uuid) {

		super(uuid);
	}

	public Clasz(final String uuid, final String uri) {

		super(uuid, uri);
	}

	/**
	 * Creates a new class with the given identifier and name-
	 *
	 * @param uri  a class identifier
	 * @param name a class name
	 */
	public Clasz(final String uuid, final String uri, final String name) {

		super(uuid, uri, name);
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return Clasz.class.isInstance(obj) && super.completeEquals(obj);
	}

}
