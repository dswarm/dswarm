package de.avgl.dmp.converter.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.culturegraph.mf.stream.source.StringReader;

import com.google.common.collect.ImmutableList;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.morph.MorphScriptBuilder;
import de.avgl.dmp.converter.pipe.StreamJsonCollapser;
import de.avgl.dmp.converter.pipe.StreamUnflattener;
import de.avgl.dmp.converter.reader.QucosaReader;
import de.avgl.dmp.persistence.model.job.Job;
import de.avgl.dmp.persistence.model.job.Transformation;

public class TransformationFlow {

	public final static String	DEFAULT_RESOURCE_PATH	= "qucosa_record.xml";

	private final Metamorph	transformer;

	public TransformationFlow(final Metamorph transformer) {
		this.transformer = transformer;
	}

	public String applyRecord(final String record) {

		final StringReader opener = new StringReader();

		return apply(record, opener);
	}

	public String applyResource(final String resourcePath) {

		final ResourceOpener opener = new ResourceOpener();

		return apply(resourcePath, opener);
	}

	public String apply(final String object, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		final String recordDummy = "record";

		final QucosaReader reader = new QucosaReader(recordDummy);

		final StreamUnflattener unflattener = new StreamUnflattener(recordDummy);
		final StreamJsonCollapser collapser = new StreamJsonCollapser();

		final JsonEncoder converter = new JsonEncoder();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<String>(stringWriter);

		opener.setReceiver(reader).setReceiver(transformer).setReceiver(unflattener).setReceiver(collapser).setReceiver(converter)
				.setReceiver(writer);

		opener.process(object);

		return stringWriter.toString();
	}

	public String apply() {
		return applyResource(DEFAULT_RESOURCE_PATH);
	}

	public static TransformationFlow fromString(final String morphScriptString) throws FileNotFoundException {
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

	public static TransformationFlow fromJob(final Job job) throws IOException, DMPConverterException {

		final String morphScriptString = new MorphScriptBuilder().apply(job.getTransformations()).toString();

		return fromString(morphScriptString);
	}

	public static TransformationFlow fromTransformation(final Transformation transformation) throws IOException, DMPConverterException {

		final ImmutableList.Builder<Transformation> transformationsBuilder = ImmutableList.builder();

		transformationsBuilder.add(transformation);

		final String morphScriptString = new MorphScriptBuilder().apply(transformationsBuilder.build()).toString();

		return fromString(morphScriptString);
	}
}
