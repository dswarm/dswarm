package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.List;

import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.reader.CsvReader;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.StringReader;
import org.junit.Test;

import de.avgl.dmp.converter.flow.TransformationFlow;
import de.avgl.dmp.init.util.DMPUtil;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.transformation.Transformation;

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

	@Test
	public void readCSVTest() throws Exception {

		StringReader opener = new StringReader();
		final String testCSVString = DMPUtil.getResourceAsString("test_csv.csv");

		final CsvReader reader = new CsvReader();
		reader.setHasHeader(true);
		final JsonEncoder converter = new JsonEncoder();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<>(stringWriter);

		opener.setReceiver(reader).setReceiver(converter).setReceiver(writer);

		opener.process(testCSVString);

		System.out.println(stringWriter.toString());
	}
}
