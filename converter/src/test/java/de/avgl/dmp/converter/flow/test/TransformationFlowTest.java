package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.Transformation;



public class TransformationFlowTest {
	
	@Test
	public void testEndToEnd() throws Exception {

		final String request = DMPUtil.getResourceAsString("complex-request.json");
		final String expected = DMPUtil.getResourceAsString("complex-result.json");

		List<Transformation> pojos = new JsonToPojoMapper().apply(request);
		final TransformationFlow flow = TransformationFlow.fromTransformations(pojos);

		final String actual = flow.apply();

		assertEquals(expected, actual);
	}

	@Test
	public void testMorphToEnd() throws Exception {

		final String expected = DMPUtil.getResourceAsString("complex-result.json");

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph.xml");

		final String actual = flow.apply();

		assertEquals(expected, actual);
	}
	
	@Test
	public void testEndToEndByRecordStringExample() throws Exception {

		final String request = DMPUtil.getResourceAsString("qucosa_record.xml");
		final String expected = DMPUtil.getResourceAsString("complex-result.json");

		final TransformationFlow flow = TransformationFlow.fromFile("complex-metamorph.xml");

		final String actual = flow.applyRecord(request);

		assertEquals(expected, actual);
	}
}
