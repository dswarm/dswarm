package de.avgl.dmp.converter.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.RecordAwareJsonEncoder;
import de.avgl.dmp.converter.mf.stream.reader.JsonNodeReader;
import de.avgl.dmp.converter.morph.MorphScriptBuilder;
import de.avgl.dmp.converter.pipe.StreamJsonCollapser;
import de.avgl.dmp.converter.pipe.StreamUnflattener;
import de.avgl.dmp.converter.reader.QucosaReader;
import de.avgl.dmp.init.util.DMPStatics;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * Flow that executes a given set of transformations on data of a given data model.
 * 
 * @author phorn
 * @author tgaengler
 */
public class TransformationFlow {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(TransformationFlow.class);

	public static final String						DEFAULT_RESOURCE_PATH	= "qucosa_record.xml";

	private final Metamorph							transformer;

	public TransformationFlow(final Metamorph transformer) {
		this.transformer = transformer;
	}

	// public String applyRecord(final String record) {
	//
	// final StringReader opener = new StringReader();
	//
	// return apply(record, opener);
	// }

	public String applyRecordDemo(final String record) {

		final StringReader opener = new StringReader();

		return applyDemo(record, opener);
	}

	// public String applyResource(final String resourcePath) {
	//
	// // public String applyResource(final String resourcePath) {
	// //
	// // final ResourceOpener opener = new ResourceOpener();
	// //
	// // return apply(resourcePath, opener);
	// // }
	//
	// String applyResourceDemo(final String resourcePath) {
	//
	// final ResourceOpener opener = new ResourceOpener();
	//
	// return applyDemo(resourcePath, opener);
	// }

	public String applyResourceDemo(final String resourcePath) {

		final ResourceOpener opener = new ResourceOpener();

		return applyDemo(resourcePath, opener);
	}

	public String apply(final Iterator<Tuple<String, JsonNode>> tuples, final ObjectPipe<Iterator<Tuple<String, JsonNode>>, StreamReceiver> opener) {

		// final String recordDummy = "record";

		final StreamUnflattener unflattener = new StreamUnflattener("", DMPStatics.ATTRIBUTE_DELIMITER);
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

		// final StreamOutWriter streamOutWriter = new StreamOutWriter();

		opener.setReceiver(transformer).setReceiver(unflattener).setReceiver(collapser).setReceiver(converter);

		opener.process(tuples);
		opener.closeStream();
		objectReceiver.closeStream();

		return stringWriter.toString();
	}

	public String apply(final Iterator<Tuple<String, JsonNode>> tuples) {
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
		return applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);
	}

	public static TransformationFlow fromString(final String morphScriptString) {
		final java.io.StringReader stringReader = new java.io.StringReader(morphScriptString);
		final Metamorph transformer = new Metamorph(stringReader);

		return new TransformationFlow(transformer);
	}

	public static TransformationFlow fromFile(final File file) throws FileNotFoundException, DMPConverterException {
		final FileInputStream is = new FileInputStream(file);
		final Reader inputSource = TransformationFlow.getReader(is);

		final Metamorph transformer = new Metamorph(inputSource);

		return new TransformationFlow(transformer);
	}

	public static TransformationFlow fromFile(final String resourcePath) throws DMPConverterException {
		final InputStream morph = TransformationFlow.class.getClassLoader().getResourceAsStream(resourcePath);
		final Reader inputSource = TransformationFlow.getReader(morph);
		final Metamorph transformer = new Metamorph(inputSource);

		return new TransformationFlow(transformer);
	}

	public static TransformationFlow fromTask(final Task task) throws DMPConverterException {

		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();

		return TransformationFlow.fromString(morphScriptString);
	}

	private static Reader getReader(final InputStream is) throws DMPConverterException {

		Reader reader = null;

		try {

			reader = new InputStreamReader(is, "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			try {

				is.close();
			} catch (final IOException e1) {

				TransformationFlow.LOG.error("couldn't close file input stream");
			}

			throw new DMPConverterException("couldn't parse file with UTF-8");
		}

		return reader;
	}
}
