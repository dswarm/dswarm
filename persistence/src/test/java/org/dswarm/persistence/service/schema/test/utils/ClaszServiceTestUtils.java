/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.schema.test.utils;

import java.util.Set;

import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

public class ClaszServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<ClaszService, ProxyClasz, Clasz> {

	public static final Set<String>	excludeClasses	= Sets.newHashSet();

	static {

		ClaszServiceTestUtils.excludeClasses.add("http://purl.org/ontology/bibo/Document");
		ClaszServiceTestUtils.excludeClasses.add("http://vocab.ub.uni-leipzig.de/bibrm/ContractItem");
		ClaszServiceTestUtils.excludeClasses.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#datensatzType");
	}

	
	/**
	 * "http://purl.org/ontology/bibo/Document", "document"
	 */
	public static final int CLASS__DOCUMENT = 0;
	
	
	public ClaszServiceTestUtils() {
		super(Clasz.class, ClaszService.class);
	}

	public Clasz createClass(final String id, final String name) throws Exception {

		final Clasz clasz = new Clasz(id, name);
		final Clasz updatedClasz = createObject(clasz, clasz);

		return updatedClasz;
	}

	
	
	/**
	 * Convenience method to create default or known classes.
	 * @param identifier ClaszServiceTestUtils.CLASS__...
	 * @return
	 * @throws Exception
	 */
	public Clasz createClass( int identifier ) throws Exception {
		
		switch( identifier ) {
			case CLASS__DOCUMENT:
				return createClass( "http://purl.org/ontology/bibo/Document", "document" );
			default:
				throw new IllegalArgumentException( "Identifier '" + identifier + "' not found!" );
		}
	}
	
	
	
	@Override
	public void deleteObject(final Clasz object) {

		if (object == null) {

			return;
		}

		if (object.getUri() == null) {

			return;
		}

		if (ClaszServiceTestUtils.excludeClasses.contains(object.getUri())) {

			// don't delete classes that should be kept

			return;
		}

		super.deleteObject(object);
	}

	@Override
	public void reset() {

	}

	@Override
	public Clasz getObject( JsonNode objectDescription ) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
