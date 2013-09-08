package de.avgl.dmp.converter.flow;

import de.avgl.dmp.converter.pipe.StreamJsonCollapser;
import de.avgl.dmp.converter.pipe.StreamUnflattener;
import de.avgl.dmp.converter.reader.QucosaReader;
import de.avgl.dmp.converter.resources.PojoToXMLBuilder;
import de.avgl.dmp.converter.resources.TransformationsCoverterException;
import de.avgl.dmp.persistence.model.Transformation;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.ResourceOpener;

import java.io.*;
import java.util.List;

public class TransformationFlow {

	public static String DEFAULT_RECORD = "qucosa_record.xml";

	private final Metamorph transformer;

	public TransformationFlow(final Metamorph transformer) {
		this.transformer = transformer;
	}

	public String apply(String record) {
		final String recordDummy = "record";

		final ResourceOpener opener = new ResourceOpener();
		final QucosaReader reader = new QucosaReader(recordDummy);

		final StreamUnflattener unflattener = new StreamUnflattener(recordDummy);
		final StreamJsonCollapser collapser = new StreamJsonCollapser();

		final JsonEncoder converter = new JsonEncoder();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<>(stringWriter);

		opener
				.setReceiver(reader)
				.setReceiver(transformer)
				.setReceiver(unflattener)
				.setReceiver(collapser)
				.setReceiver(converter)
				.setReceiver(writer);

		opener.process(record);

		return stringWriter.toString();
	}

	public String apply() {
		return apply(DEFAULT_RECORD);
	}

	public static TransformationFlow from(final File file) throws FileNotFoundException {
		final FileInputStream is = new FileInputStream(file);
		final Metamorph transformer = new Metamorph(is);

		return new TransformationFlow(transformer);
	}

	public static TransformationFlow from(String resourcePath) {
		final InputStream morph = TransformationFlow.class.getClassLoader().getResourceAsStream(resourcePath);
		final Metamorph transformer = new Metamorph(morph);

		return new TransformationFlow(transformer);
	}

	public static TransformationFlow from(List<Transformation> transformations) throws IOException, TransformationsCoverterException {
		final File file = new PojoToXMLBuilder().apply(transformations).toFile();

		return from(file);
	}

	public static void main(String[] args) {
		TransformationFlow t = TransformationFlow.from("qucosa-morph.xml");
		System.out.println(t.apply());
	}
}
