package org.dswarm.converter.morph;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import com.google.common.io.Resources;
import org.culturegraph.mf.formeta.formatter.FormatterStyle;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.converter.FormetaEncoder;
import org.culturegraph.mf.stream.converter.xml.SimpleXmlEncoder;
import org.culturegraph.mf.stream.reader.CsvReader;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.FileOpener;
import org.junit.Test;

public class RegexMapMFTest {
	
	public final static String MORPH_DEFINITION = "dd-962.regexmap.morph.xml";
	public final static String MAB_XML_EXAMPLE = "mabxml.tuples.json";
	public final static String EXAMPLE_DATA_CSV = "dd-962.regexmap.csv";
	
	@Test
	public void testRegexMap() throws IOException {
		
		final Metamorph metamorph = new Metamorph(Resources.getResource(MORPH_DEFINITION).getPath());
		final File file = new File(Resources.getResource(EXAMPLE_DATA_CSV).getPath());
		final FileOpener opener = new FileOpener();

		final CsvReader csvReader = new CsvReader(",");
		csvReader.setHasHeader(true);

		final SimpleXmlEncoder xmlEncoder = new SimpleXmlEncoder();	
		final FormetaEncoder formetaEncoder = new FormetaEncoder();	
		formetaEncoder.setStyle(FormatterStyle.MULTILINE);

		opener
		.setReceiver(csvReader)
		.setReceiver(metamorph)
		.setReceiver(xmlEncoder)
		;	
		
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> streamWriter = new ObjectJavaIoWriter<String>(stringWriter);
		
		xmlEncoder.setReceiver(streamWriter);
		opener.process(file.getAbsolutePath());
		
		String result = stringWriter.toString();
		System.out.println(result);		
	}
	

}