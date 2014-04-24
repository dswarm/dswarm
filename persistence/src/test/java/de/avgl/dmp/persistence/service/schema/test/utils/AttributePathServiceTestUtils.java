package de.avgl.dmp.persistence.service.schema.test.utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.test.utils.BasicJPAServiceTestUtils;

public class AttributePathServiceTestUtils extends BasicJPAServiceTestUtils<AttributePathService, ProxyAttributePath, AttributePath, Long> {

	private final AttributeServiceTestUtils	attributeResourceTestUtils;

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

		AttributePath updatedAttributePath = createObject(attributePath, attributePath);

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
	public void deleteObject(AttributePath object) {

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

		super.deleteObject(object);
	}

	@Override
	public void reset() {

		attributeResourceTestUtils.reset();
	}
}
