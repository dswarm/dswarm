package de.avgl.dmp.persistence.service.schema.test.utils;

import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

import java.util.Set;

public class AttributeServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<AttributeService, ProxyAttribute, Attribute> {

	public static final Set<String>	excludeAttributes	= Sets.newHashSet();

	static {

		excludeAttributes.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		excludeAttributes.add("http://xmlns.com/foaf/0.1/givenName");
		excludeAttributes.add("http://purl.org/dc/elements/1.1/title");
		excludeAttributes.add("http://rdvocab.info/Elements/otherTitleInformation");
		excludeAttributes.add("http://purl.org/dc/terms/alternative");
		excludeAttributes.add("http://purl.org/ontology/bibo/shortTitle");
		excludeAttributes.add("http://purl.org/dc/terms/creator");
		excludeAttributes.add("http://purl.org/dc/elements/1.1/creator");
		excludeAttributes.add("http://purl.org/dc/terms/contributor");
		excludeAttributes.add("http://purl.org/dc/elements/1.1/contributor");
		excludeAttributes.add("http://rdvocab.info/Elements/publicationStatement");
		excludeAttributes.add("http://rdvocab.info/Elements/placeOfPublication");
		excludeAttributes.add("http://purl.org/dc/elements/1.1/publisher");
		excludeAttributes.add("http://purl.org/dc/terms/issued");
		excludeAttributes.add("http://www.w3.org/2002/07/owl#sameAs");
		excludeAttributes.add("http://umbel.org/umbel#isLike");
		excludeAttributes.add("http://purl.org/ontology/bibo/issn");
		excludeAttributes.add("http://purl.org/ontology/bibo/eissn");
		excludeAttributes.add("http://purl.org/ontology/bibo/lccn");
		excludeAttributes.add("http://purl.org/ontology/bibo/oclcnum");
		excludeAttributes.add("http://purl.org/ontology/bibo/isbn");
		excludeAttributes.add("http://purl.org/dc/terms/medium");
		excludeAttributes.add("http://purl.org/dc/terms/hasPart");
		excludeAttributes.add("http://purl.org/dc/terms/isPartOf");
		excludeAttributes.add("http://purl.org/dc/terms/hasVersion");
		excludeAttributes.add("http://purl.org/dc/terms/isFormatOf");
		excludeAttributes.add("http://rdvocab.info/Elements/precededBy");
		excludeAttributes.add("http://rdvocab.info/Elements/succeededBy");
		excludeAttributes.add("http://purl.org/dc/terms/language");
		excludeAttributes.add("http://iflastandards.info/ns/isbd/elements/1053");
		excludeAttributes.add("http://purl.org/ontology/bibo/edition");
		excludeAttributes.add("http://purl.org/dc/terms/bibliographicCitation");
		excludeAttributes.add("http://xmlns.com/foaf/0.1/familyName");
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
	public void deleteObject(Attribute object) {

		if (object == null) {

			return;
		}

		if(object.getUri() == null) {

			return;
		}

		if(excludeAttributes.contains(object.getUri())) {

			// don't delete attributes that should be kept

			return;
		}

		super.deleteObject(object);
	}

	@Override
	public void reset() {

	}
}
