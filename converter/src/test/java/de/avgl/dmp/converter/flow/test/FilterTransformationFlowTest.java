package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.inject.Provider;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class FilterTransformationFlowTest extends GuicedTest {

	// @Test
	// public void testMorphToEndDemo() throws Exception {
	//
	// final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");
	//
	// final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider =
	// injector.getProvider(InternalModelServiceFactory.class);
	//
	// final TransformationFlow flow = TransformationFlow.fromFile("filtermorph.xml", internalModelServiceFactoryProvider);
	//
	// final String actual = flow.applyDemo();
	//
	// assertEquals(expected, actual);
	// }

	@Test
	public void testFilterEndToEndByRecordStringExampleDemo() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("complex-result.json");

		final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider = injector.getProvider(InternalModelServiceFactory.class);

		final TransformationFlow flow = TransformationFlow.fromFile("filtermorph.xml", internalModelServiceFactoryProvider);

		final String actual = flow.applyResource("test-mabxml.tuples.json");

		assertEquals(expected, actual);
	}
}
