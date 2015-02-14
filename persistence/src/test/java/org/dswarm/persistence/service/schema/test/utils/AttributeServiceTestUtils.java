/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.fasterxml.jackson.databind.JsonNode;

import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public class AttributeServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<AttributeService, ProxyAttribute, Attribute> {

	public static final String DCTERMS_TITLE   = "http://purl.org/dc/terms/title";
	public static final String DCTERMS_HASPART = "http://purl.org/dc/terms/hasPart";
	public static final String DCTERMS_CREATED = "http://purl.org/dc/terms/created";
	public static final String RDF_VALUE       = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";
	public static final String MABXML_ID       = "http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#id";
	public static final String RDFS_LABEL      = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String DCTERMS_CREATOR = "http://purl.org/dc/terms/creator";
	public static final String FOAF_FIRSTNAME  = "http://xmlns.com/foaf/0.1/firstName";
	public static final String FOAF_FAMILYNAME = "http://xmlns.com/foaf/0.1/familyName";
	public static final String FOAF_NAME       = "http://xmlns.com/foaf/0.1/name";

	static {

		commonTermsMap.put(DCTERMS_TITLE, new Tuple<>(DCTERMS_TITLE, "title"));
		commonTermsMap.put(DCTERMS_HASPART, new Tuple<>(DCTERMS_HASPART, "hasPart"));
		commonTermsMap.put(DCTERMS_CREATED, new Tuple<>(DCTERMS_CREATED, "created"));
		commonTermsMap.put(RDF_VALUE, new Tuple<>(RDF_VALUE, "value"));
		commonTermsMap.put(MABXML_ID, new Tuple<>(MABXML_ID, "id"));
		commonTermsMap.put(RDFS_LABEL, new Tuple<>(RDFS_LABEL, "label"));
		commonTermsMap.put(DCTERMS_CREATOR, new Tuple<>(DCTERMS_CREATOR, "creator"));
		commonTermsMap.put(FOAF_FIRSTNAME, new Tuple<>(FOAF_FIRSTNAME, "first name"));
		commonTermsMap.put(FOAF_FAMILYNAME, new Tuple<>(FOAF_FAMILYNAME, "family name"));
		commonTermsMap.put(FOAF_NAME, new Tuple<>(FOAF_NAME, "name"));
	}

	public AttributeServiceTestUtils() {

		super(Attribute.class, AttributeService.class);
	}

	@Override
	public Attribute createObject(final String id, final String name) throws Exception {

		// TODO: think about this?
		final String uuid = UUIDService.getUUID(Attribute.class.getSimpleName());

		final Attribute attribute = new Attribute(uuid, id, name);

		return createAndCompareObject(attribute, attribute);
	}

	@Override
	public void reset() {

	}

	public Attribute getDctermsTitle() throws Exception {

		return createObject(DCTERMS_TITLE);
	}

	public Attribute getDctermsHaspart() throws Exception {

		return createObject(DCTERMS_HASPART);
	}

	public Attribute getDctermsCreated() throws Exception {

		return createObject(DCTERMS_CREATED);
	}

	public Attribute getRDFValue() throws Exception {

		return createObject(RDF_VALUE);
	}

	public Attribute getMABXMLID() throws Exception {

		return createObject(MABXML_ID);
	}

	public Attribute getRDFSLabel() throws Exception {

		return createObject(RDFS_LABEL);
	}

	public Attribute getDctermsCreator() throws Exception {

		return createObject(DCTERMS_CREATOR);
	}

	@Override
	public Attribute createObject(JsonNode objectDescription) throws Exception {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute createAndPersistDefaultObject() throws Exception {

		return getDctermsTitle();
	}

	@Override public Attribute createDefaultObject() throws Exception {
		return null;
	}

}
