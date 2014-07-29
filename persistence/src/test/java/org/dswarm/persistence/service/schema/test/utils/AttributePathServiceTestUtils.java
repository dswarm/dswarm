package org.dswarm.persistence.service.schema.test.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils;

public class AttributePathServiceTestUtils extends BasicJPAServiceTestUtils<AttributePathService, ProxyAttributePath, AttributePath, Long> {

	private final AttributeServiceTestUtils		attributeResourceTestUtils;

	public static final Set<LinkedList<String>>	excludeAttributePaths	= Sets.newHashSet();

	static {

		final LinkedList<String> attributePath32 = Lists.newLinkedList();
		attributePath32.add("http://purl.org/dc/terms/creator");
		attributePath32.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath32);

		final LinkedList<String> attributePath33 = Lists.newLinkedList();
		attributePath33.add("http://purl.org/dc/terms/creator");
		attributePath33.add("http://xmlns.com/foaf/0.1/familyName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath33);

		final LinkedList<String> attributePath34 = Lists.newLinkedList();
		attributePath34.add("http://purl.org/dc/terms/creator");
		attributePath34.add("http://xmlns.com/foaf/0.1/givenName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath34);

		final LinkedList<String> attributePath35 = Lists.newLinkedList();
		attributePath35.add("http://purl.org/dc/terms/contributor");
		attributePath35.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath35);

		final LinkedList<String> attributePath36 = Lists.newLinkedList();
		attributePath36.add("http://purl.org/dc/terms/contributor");
		attributePath36.add("http://xmlns.com/foaf/0.1/familyName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath36);

		final LinkedList<String> attributePath37 = Lists.newLinkedList();
		attributePath37.add("http://purl.org/dc/terms/contributor");
		attributePath37.add("http://xmlns.com/foaf/0.1/givenName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath37);

		final LinkedList<String> attributePath45 = Lists.newLinkedList();
		attributePath45.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath45.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath45);

		final LinkedList<String> attributePath46 = Lists.newLinkedList();
		attributePath46.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath46.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#nr");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath46);

		final LinkedList<String> attributePath47 = Lists.newLinkedList();
		attributePath47.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath47.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ind");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath47);

		final LinkedList<String> attributePath48 = Lists.newLinkedList();
		attributePath48.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath48.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath48);

		final LinkedList<String> attributePath49 = Lists.newLinkedList();
		attributePath49.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath49.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath49);

		final LinkedList<String> attributePath50 = Lists.newLinkedList();
		attributePath50.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath50.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		attributePath50.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath50);

		final LinkedList<String> attributePath51 = Lists.newLinkedList();
		attributePath51.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath51.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		attributePath51.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath51);

		final LinkedList<String> attributePath52 = Lists.newLinkedList();
		attributePath52.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath52.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath52);

		final LinkedList<String> attributePath53 = Lists.newLinkedList();
		attributePath53.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath53.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf");
		attributePath53.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath53);
	}

	public AttributePathServiceTestUtils() {

		super(AttributePath.class, AttributePathService.class);

		attributeResourceTestUtils = new AttributeServiceTestUtils();
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link AttributePath}s have either no {@link Attribute}s or {@link Attribute}s are equal. See
	 * {@link BasicJPAServiceTestUtils#compareObjects(Set, Map)} for details
	 * 
	 * @param expectedObject
	 * @param actualObject
	 */
	@Override
	public void compareObjects(final AttributePath expectedObject, final AttributePath actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getAttributes() == null || expectedObject.getAttributes().isEmpty()) {

			final boolean actualHasNoAttributes = actualObject.getAttributes() == null || actualObject.getAttributes().isEmpty();
			Assert.assertTrue("the actual attribute path should not have any attributes", actualHasNoAttributes);

		} else {
			// !null && !empty

			final Set<Attribute> actualAttributes = actualObject.getAttributes();

			Assert.assertNotNull("attributes of actual attribute path '" + actualObject.getId() + "' shouldn't be null", actualAttributes);
			Assert.assertFalse("attributes of actual attribute path '" + actualObject.getId() + "' shouldn't be empty", actualAttributes.isEmpty());

			final Map<Long, Attribute> actualAttributesMap = Maps.newHashMap();

			for (final Attribute actualAttribute : actualAttributes) {

				actualAttributesMap.put(actualAttribute.getId(), actualAttribute);
			}

			attributeResourceTestUtils.compareObjects(expectedObject.getAttributes(), actualAttributesMap);
		}
	}

	public AttributePath createAttributePath(final LinkedList<Attribute> attributePathArg) throws Exception {

		final AttributePath attributePath = new AttributePath(attributePathArg);

		final AttributePath updatedAttributePath = createObject(attributePath, attributePath);

		return updatedAttributePath;
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and list of attributes of the attribute path.
	 */
	@Override
	protected AttributePath prepareObjectForUpdate(final AttributePath objectWithUpdates, final AttributePath object) {

		object.setAttributePath(objectWithUpdates.getAttributePath());

		return object;
	}

	@Override
	protected ProxyAttributePath createObject(final AttributePath object) throws DMPPersistenceException {

		return jpaService.createObjectTransactional(object);
	}

	@Override
	public void deleteObject(final AttributePath object) {

		if (object == null) {

			return;
		}

		final Set<Attribute> attributes = object.getAttributes();

		if (attributes != null && !attributes.isEmpty() && attributes.size() == 1) {

			final Attribute attribute = attributes.iterator().next();

			if (attribute != null && attribute.getUri() != null && AttributeServiceTestUtils.excludeAttributes.contains(attribute.getUri())) {

				// don't delete attribute paths of attributes that should be excluded from removal

				return;
			}
		}

		final LinkedList<Attribute> orderedAttributes = object.getAttributePath();

		if (orderedAttributes != null && !orderedAttributes.isEmpty()) {

			for (final LinkedList<String> excludeAttributePath : AttributePathServiceTestUtils.excludeAttributePaths) {

				if (excludeAttributePath.size() != orderedAttributes.size()) {

					// only compare attribute paths of the same size

					continue;
				}

				final Iterator<String> excludeIter = excludeAttributePath.iterator();

				boolean interrupted = false;

				while (excludeIter.hasNext()) {

					final Iterator<Attribute> iter = orderedAttributes.iterator();

					while (iter.hasNext()) {

						final Attribute orderedAttribute = iter.next();

						if (orderedAttribute.getUri() == null) {

							break;
						}

						final String excludeAttribute = excludeIter.next();

						if (orderedAttribute.getUri() == null) {

							interrupted = true;

							break;
						}

						if (!excludeAttribute.equals(orderedAttribute.getUri())) {

							interrupted = true;

							break;
						}

						if (!excludeIter.hasNext() && iter.hasNext()) {

							interrupted = true;

							break;
						}
					}

					if (interrupted) {

						break;
					}
				}

				if (!interrupted) {

					// found match

					return;
				}
			}
		}

		super.deleteObject(object);
	}

	@Override
	public void reset() {

		attributeResourceTestUtils.reset();
	}
}
