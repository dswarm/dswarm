package de.avgl.dmp.persistence.service.schema.test.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.test.utils.BasicJPAServiceTestUtils;

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
	}

	public AttributePathServiceTestUtils() {

		super(AttributePath.class, AttributePathService.class);

		attributeResourceTestUtils = new AttributeServiceTestUtils();
	}

	@Override
	public void compareObjects(final AttributePath expectedObject, final AttributePath actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getAttributes() != null && !expectedObject.getAttributes().isEmpty()) {

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
