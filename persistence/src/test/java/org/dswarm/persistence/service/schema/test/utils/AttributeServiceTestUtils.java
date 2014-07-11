package org.dswarm.persistence.service.schema.test.utils;

import java.util.Set;

import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

import com.google.common.collect.Sets;

public class AttributeServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<AttributeService, ProxyAttribute, Attribute> {

	public static final Set<String>	excludeAttributes	= Sets.newHashSet();

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
	}

	public AttributeServiceTestUtils() {

		super(Attribute.class, AttributeService.class);
	}

	public Attribute createAttribute(final String id, final String name) throws Exception {

		final Attribute attribute = new Attribute(id, name);
		final Attribute updatedAttribute = createObject(attribute, attribute);

		return updatedAttribute;
	}

	@Override
	public void deleteObject(final Attribute object) {

		if (object == null) {

			return;
		}

		if (object.getUri() == null) {

			return;
		}

		if (AttributeServiceTestUtils.excludeAttributes.contains(object.getUri())) {

			// don't delete attributes that should be kept

			return;
		}

		super.deleteObject(object);
	}

	@Override
	public void reset() {

	}
}
