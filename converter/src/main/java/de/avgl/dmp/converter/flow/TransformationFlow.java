package de.avgl.dmp.converter.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Iterator;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.culturegraph.mf.stream.source.StringReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.RecordAwareJsonEncoder;
import de.avgl.dmp.converter.mf.stream.reader.JsonNodeReader;
import de.avgl.dmp.converter.morph.MorphScriptBuilder;
import de.avgl.dmp.converter.pipe.StreamJsonCollapser;
import de.avgl.dmp.converter.pipe.StreamUnflattener;
import de.avgl.dmp.converter.reader.QucosaReader;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.types.Tuple;

public class TransformationFlow {

	public static final String	DEFAULT_RESOURCE_PATH	= "qucosa_record.xml";

	private final Metamorph	transformer;

	public TransformationFlow(final Metamorph transformer) {
		this.transformer = transformer;
	}

//	public String applyRecord(final String record) {
//
//		final StringReader opener = new StringReader();
//
//		return apply(record, opener);
//	}

	public String applyRecordDemo(final String record) {

		final StringReader opener = new StringReader();

		return applyDemo(record, opener);
	}

//	public String applyResource(final String resourcePath) {
//
////	public String applyResource(final String resourcePath) {
////
////		final ResourceOpener opener = new ResourceOpener();
////
////		return apply(resourcePath, opener);
////	}
//
//	String applyResourceDemo(final String resourcePath) {
//
//		final ResourceOpener opener = new ResourceOpener();
//
//		return applyDemo(resourcePath, opener);
//	}

	public String applyResourceDemo(final String resourcePath) {

		final ResourceOpener opener = new ResourceOpener();

		return applyDemo(resourcePath, opener);
	}

	public String apply(final Iterator<Tuple<String,JsonNode>> tuples, final ObjectPipe<Iterator<Tuple<String,JsonNode>>, StreamReceiver> opener) {

		final String recordDummy = "record";

		final StreamUnflattener unflattener = new StreamUnflattener(recordDummy);
		final StreamJsonCollapser collapser = new StreamJsonCollapser();

		final StringWriter stringWriter = new StringWriter();
		stringWriter.append('[');

		final ObjectReceiver<String> objectReceiver = new ObjectReceiver<String>() {
			@Override
			public void process(final String obj) {
				stringWriter.append(obj);
				stringWriter.append(',');
			}

			@Override
			public void resetStream() {
				final StringBuffer buffer = stringWriter.getBuffer();
				buffer.delete(0, buffer.length());
			}

			@Override
			public void closeStream() {
				System.out.println("close stream called");
				final StringBuffer buffer = stringWriter.getBuffer();
				buffer.deleteCharAt(buffer.length() - 1);
				stringWriter.append(']');
			}
		};

		final JsonEncoder jsonEncoder = new JsonEncoder();

		final RecordAwareJsonEncoder converter = new RecordAwareJsonEncoder(jsonEncoder);
		jsonEncoder.setReceiver(objectReceiver);

		opener.setReceiver(transformer).setReceiver(unflattener).setReceiver(collapser).setReceiver(converter);

		opener.process(tuples);
		opener.closeStream();
		objectReceiver.closeStream();

		return stringWriter.toString();
	}

	public String apply(final Iterator<Tuple<String,JsonNode>> tuples) {
		final JsonNodeReader opener = new JsonNodeReader();
		return apply(tuples, opener);
	}

	public String applyDemo(final String object, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		final String recordDummy = "record";

		final QucosaReader reader = new QucosaReader(recordDummy);

		final StreamUnflattener unflattener = new StreamUnflattener(recordDummy);
		final StreamJsonCollapser collapser = new StreamJsonCollapser();

		final JsonEncoder converter = new JsonEncoder();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<>(stringWriter);

		opener.setReceiver(reader).setReceiver(transformer).setReceiver(unflattener).setReceiver(collapser).setReceiver(converter)
				.setReceiver(writer);

		opener.process(object);

		return stringWriter.toString();
	}

	public String applyDemo() {
		return applyResourceDemo(DEFAULT_RESOURCE_PATH);
	}

	public static TransformationFlow fromString(final String morphScriptString) {
		final java.io.StringReader stringReader = new java.io.StringReader(morphScriptString);
		final Metamorph transformer = new Metamorph(stringReader);

		return new TransformationFlow(transformer);
	}

	public static TransformationFlow fromFile(final File file) throws FileNotFoundException {
		final FileInputStream is = new FileInputStream(file);
		final Metamorph transformer = new Metamorph(is);

		return new TransformationFlow(transformer);
	}

	public static TransformationFlow fromFile(final String resourcePath) {
		final InputStream morph = TransformationFlow.class.getClassLoader().getResourceAsStream(resourcePath);
		final Metamorph transformer = new Metamorph(morph);

		return new TransformationFlow(transformer);
	}

	// TODO:

//	public static TransformationFlow fromJob(final Job job) throws IOException, DMPConverterException {
//
//		final String morphScriptString = new MorphScriptBuilder().apply(job.getTransformations()).toString();
//
//		return fromString(morphScriptString);
//	}

	public static TransformationFlow fromTransformation(final Transformation transformation) {

		final ImmutableList.Builder<Transformation> transformationsBuilder = ImmutableList.builder();

		transformationsBuilder.add(transformation);

		final String morphScriptString = null;

				//new MorphScriptBuilder().apply(transformationsBuilder.build()).toString();

		return fromString(morphScriptString);
	}
	
	public static TransformationFlow fromTask(final Task task) throws DMPConverterException {
		
		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();

		return fromString(morphScriptString);
	}
}
