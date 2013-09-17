package de.avgl.dmp.converter.morph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Parameter;
import de.avgl.dmp.persistence.model.job.Transformation;

public class MorphScriptBuilder {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(MorphScriptBuilder.class);
	
	private static final DocumentBuilderFactory	docFactory					= DocumentBuilderFactory.newInstance();

	private static final String					SCHEMA_PATH					= "schemata/metamorph.xsd";

	private static final TransformerFactory		transformerFactory;

	private static final String					TRANSFORMER_FACTORY_CLASS	= "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

	static {
		System.setProperty("javax.xml.transform.TransformerFactory", TRANSFORMER_FACTORY_CLASS);
		transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", 4);

		InputStream SCHEMA_STREAM = null;
		
		try {
			
			SCHEMA_STREAM = Resources.newInputStreamSupplier(Resources.getResource(SCHEMA_PATH)).getInput();
		} catch (final IOException e1) {
			
			e1.printStackTrace();
		}
		
		if(SCHEMA_STREAM == null) {
			
			LOG.error("couldn't read schema resource");
		}
		
		// final StreamSource SCHEMA_SOURCE = new StreamSource(SCHEMA_STREAM);
		final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;

		try {

			// TODO: dummy schema right now, since it couldn't parse the metamorph schema for some reason
			schema = sf.newSchema();
		} catch (final SAXException e) {

			e.printStackTrace();
		}
		
		if(schema == null) {
			
			LOG.error("couldn't parse schema");
		}

		docFactory.setSchema(schema);
	}

	private Document							doc;

	private void createParameters(final Map<String, Parameter> parameters, Element component) {
		if (parameters != null) {
			for (Parameter parameter : parameters.values()) {
				if (parameter.getName() != null) {
					if (parameter.getData() != null) {
						final Attr param = doc.createAttribute(parameter.getName());
						param.setValue(parameter.getData());
						component.setAttributeNode(param);
					} else if (parameter.isRepeat()) {
						final Element subEl = doc.createElement(parameter.getName());
						component.appendChild(subEl);
						createParameters(parameter.getParameters(), subEl);
					}
				}
			}
		}
	}

	private Element createTransformation(final Transformation transformation) {
		Element data = doc.createElement("data");
		data.setAttribute("source", "record." + transformation.getSource().getName());
		data.setAttribute("name", "record." + transformation.getTarget().getName());

		for (Component component : transformation.getComponents()) {
			Element comp = doc.createElement(component.getName());

			createParameters(component.getPayload().getParameters(), comp);
			data.appendChild(comp);
		}

		return data;
	}

	public String render(boolean indent, Charset encoding) {
		final String defaultEncoding = encoding.name();
		final Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

		transformer.setOutputProperty(OutputKeys.ENCODING, defaultEncoding);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		final StreamResult result;
		try {
			result = new StreamResult(new OutputStreamWriter(stream, defaultEncoding));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		try {
			transformer.transform(new DOMSource(doc), result);
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}

		try {
			return stream.toString(defaultEncoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String render(boolean indent) {
		return render(indent, Charset.forName("UTF-8"));
	}

	@Override
	public String toString() {
		return render(true);
	}

	public File toFile() throws IOException {
		final String str = render(false);

		final File file = File.createTempFile("avgl_dmp", ".tmp");

		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(str);
		bw.close();

		return file;
	}

	public MorphScriptBuilder apply(final List<Transformation> transformations) throws DMPConverterException {
		final DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new DMPConverterException(e.getMessage());
		}

		doc = docBuilder.newDocument();
		doc.setXmlVersion("1.0");

		Element rootElement = doc.createElement("metamorph");
		rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:schemaLocation", "http://www.culturegraph.org/metamorph metamorph.xsd");
		rootElement.setAttribute("entityMarker", ".");
		rootElement.setAttribute("version", "1");
		doc.appendChild(rootElement);

		Element meta = doc.createElement("meta");
		rootElement.appendChild(meta);

		Element metaName = doc.createElement("name");
		meta.appendChild(metaName);

		Element rules = doc.createElement("rules");
		rootElement.appendChild(rules);

		final List<String> metas = Lists.newArrayList();

		for (Transformation transformation : transformations) {
			metas.add(transformation.getName());
			Element data = createTransformation(transformation);

			rules.appendChild(data);
		}

		metaName.setTextContent(Joiner.on(", ").join(metas));

		return this;
	}

	public MorphScriptBuilder apply(final Transformation transformation) throws DMPConverterException {
		
		return apply(Lists.newArrayList(transformation));
	}
}
