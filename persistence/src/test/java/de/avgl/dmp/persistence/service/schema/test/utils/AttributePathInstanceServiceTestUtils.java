package de.avgl.dmp.persistence.service.schema.test.utils;

import java.util.LinkedList;

import org.junit.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.schema.AttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePathInstance;
import de.avgl.dmp.persistence.service.schema.AttributePathInstanceService;
import de.avgl.dmp.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public abstract class AttributePathInstanceServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends AttributePathInstanceService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAttributePathInstance<POJOCLASS>, POJOCLASS extends AttributePathInstance>
		extends BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	private final AttributePathServiceTestUtils	attributePathServiceTestUtils;

	public AttributePathInstanceServiceTestUtils(final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);

		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
	}

	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareAttributePathInstances(expectedObject, actualObject);
	}

	private void compareAttributePathInstances(final POJOCLASS expectedAttributePathInstance, final POJOCLASS actualAttributePathInstance) {

		if (expectedAttributePathInstance.getAttributePathInstanceType() != null) {

			Assert.assertNotNull("the " + pojoClassName + " attribute path instance type shouldn't be null",
					actualAttributePathInstance.getAttributePathInstanceType());

			Assert.assertEquals("the " + pojoClassName + " attribute path instance types are not equal",
					expectedAttributePathInstance.getAttributePathInstanceType(), actualAttributePathInstance.getAttributePathInstanceType());
		}

		if (expectedAttributePathInstance.getAttributePath() != null) {

			attributePathServiceTestUtils.compareObjects(expectedAttributePathInstance.getAttributePath(),
					actualAttributePathInstance.getAttributePath());
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectWithUpdates, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setAttributePath(objectWithUpdates.getAttributePath());

		return object;
	}

	@Override
	public void reset() {

		attributePathServiceTestUtils.reset();
	}
}
