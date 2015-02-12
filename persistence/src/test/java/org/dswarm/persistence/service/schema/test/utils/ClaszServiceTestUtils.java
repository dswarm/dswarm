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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public class ClaszServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<ClaszService, ProxyClasz, Clasz> {

	public static final Set<String> excludeClasses = Sets.newHashSet();

	public static final String BIBO_DOCUMENT = "http://purl.org/ontology/bibo/Document";

	static {

		ClaszServiceTestUtils.excludeClasses.add("http://purl.org/ontology/bibo/Document");
		ClaszServiceTestUtils.excludeClasses.add("http://vocab.ub.uni-leipzig.de/bibrm/ContractItem");
		ClaszServiceTestUtils.excludeClasses.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#datensatzType");
	}

	static {

		commonTermsMap.put(BIBO_DOCUMENT, new Tuple<>(BIBO_DOCUMENT, "document"));
	}

	public ClaszServiceTestUtils() {
		super(Clasz.class, ClaszService.class);
	}

	@Override
	public Clasz createObject(final String id, final String name) throws Exception {

		// TODO: think about this?
		final String uuid = UUIDService.getUUID(Clasz.class.getSimpleName());

		final Clasz clasz = new Clasz(uuid, id, name);

		return createAndCompareObject(clasz, clasz);
	}

	@Override
	public Clasz createAndPersistDefaultObject() throws Exception {

		return createObject(BIBO_DOCUMENT);
	}

	@Override public Clasz createDefaultObject() throws Exception {
		return null;
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
	public Clasz createObject(JsonNode objectDescription) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
