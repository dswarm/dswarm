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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public class AttributeServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<AttributeService, ProxyAttribute, Attribute> {

	public static final String DCTERMS_TITLE = "http://purl.org/dc/terms/title";
	public static final String DCTERMS_HASPART = "http://purl.org/dc/terms/hasPart";

	public static final Set<String> excludeAttributes    = Sets.newHashSet();
	public static final Set<String> excludeSubAttributes = Sets.newHashSet();

	static {

		AttributeServiceTestUtils.excludeAttributes.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributeServiceTestUtils.excludeAttributes.add("http://xmlns.com/foaf/0.1/givenName");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/elements/1.1/title");
		AttributeServiceTestUtils.excludeAttributes.add("http://rdvocab.info/Elements/otherTitleInformation");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/alternative");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/ontology/bibo/shortTitle");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/creator");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/elements/1.1/creator");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/contributor");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/elements/1.1/contributor");
		AttributeServiceTestUtils.excludeAttributes.add("http://rdvocab.info/Elements/publicationStatement");
		AttributeServiceTestUtils.excludeAttributes.add("http://rdvocab.info/Elements/placeOfPublication");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/elements/1.1/publisher");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/issued");
		AttributeServiceTestUtils.excludeAttributes.add("http://www.w3.org/2002/07/owl#sameAs");
		AttributeServiceTestUtils.excludeAttributes.add("http://umbel.org/umbel#isLike");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/ontology/bibo/issn");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/ontology/bibo/eissn");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/ontology/bibo/lccn");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/ontology/bibo/oclcnum");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/ontology/bibo/isbn");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/medium");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/hasPart");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/isPartOf");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/hasVersion");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/isFormatOf");
		AttributeServiceTestUtils.excludeAttributes.add("http://rdvocab.info/Elements/precededBy");
		AttributeServiceTestUtils.excludeAttributes.add("http://rdvocab.info/Elements/succeededBy");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/language");
		AttributeServiceTestUtils.excludeAttributes.add("http://iflastandards.info/ns/isbd/elements/1053");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/ontology/bibo/edition");
		AttributeServiceTestUtils.excludeAttributes.add("http://purl.org/dc/terms/bibliographicCitation");
		AttributeServiceTestUtils.excludeAttributes.add("http://xmlns.com/foaf/0.1/familyName");
		AttributeServiceTestUtils.excludeAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#id");
		AttributeServiceTestUtils.excludeAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#typ");
		AttributeServiceTestUtils.excludeAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#status");
		AttributeServiceTestUtils.excludeAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#mabVersion");
		AttributeServiceTestUtils.excludeAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#nr");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ind");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		AttributeServiceTestUtils.excludeSubAttributes.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#code");
	}

	private static final Map<String, Tuple<String, String>> commonsAttributesMap = new HashMap<>();

	static {

		commonsAttributesMap.put(DCTERMS_TITLE, new Tuple<>(DCTERMS_TITLE, "title"));
		commonsAttributesMap.put(DCTERMS_HASPART, new Tuple<>(DCTERMS_HASPART, "hasPart"));
	}

	private Map<String, Attribute> map = new HashMap<>();

	public AttributeServiceTestUtils() {

		super(Attribute.class, AttributeService.class);
	}

	public Attribute createAttribute(final String id, final String name) throws Exception {

		final Attribute attribute = new Attribute(id, name);

		return createObject(attribute, attribute);
	}

	@Override
	public void deleteObject(final Attribute object) {

		if (object == null) {

			return;
		}

		if (object.getUri() == null) {

			return;
		}

		if (AttributeServiceTestUtils.excludeAttributes.contains(object.getUri())
				|| AttributeServiceTestUtils.excludeSubAttributes.contains(object.getUri())) {

			// don't delete attributes that should be kept

			return;
		}

		super.deleteObject(object);
	}

	@Override
	public void reset() {

	}

	/**
	 * note: if the attribute was created before, you'll get the cached result. if you would like to get a fresh attribute from the database (e.g. for uniqueness test etc.), then you should utilise the #createAttribute(String, String) method
	 * 
	 * @param identifier
	 * @return
	 * @throws Exception
	 */
	public Attribute getAttribute(final String identifier) throws Exception {

		if (!map.containsKey(identifier)) {

			if(!commonsAttributesMap.containsKey(identifier)) {

				throw new DMPPersistenceException(identifier + " is no common, please define it or utilise AttributeServiceTestUtils#createAttribute(Attribute)");
			}

			final Tuple<String, String> tuple = commonsAttributesMap.get(identifier);

			map.put(identifier, createAttribute(tuple.v1(), tuple.v2()));
		}

		return map.get(identifier);
	}

	public Attribute getDctermsTitle() throws Exception {

		return getAttribute(DCTERMS_TITLE);
	}

	public Attribute getDctermsHaspart() throws Exception {

		return getAttribute(DCTERMS_HASPART);
	}

	@Override
	public Attribute getObject(JsonNode objectDescription) throws Exception {

		// TODO Auto-generated method stub
		return null;
	}

}
