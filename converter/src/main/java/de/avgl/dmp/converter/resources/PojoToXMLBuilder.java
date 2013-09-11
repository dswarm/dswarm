package de.avgl.dmp.converter.resources;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import de.avgl.dmp.persistence.model.Component;
import de.avgl.dmp.persistence.model.Parameter;
import de.avgl.dmp.persistence.model.Transformation;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PojoToXMLBuilder {

	private static final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

	private static final TransformerFactory transformerFactory;

	static {
		transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", 4);
	}

	private Document doc;


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

		transformer.setOutputProperty(OutputKeys.INDENT, indent? "yes" : "no");

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

	public PojoToXMLBuilder apply(final List<Transformation> transformations) throws TransformationsCoverterException {
		final DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new TransformationsCoverterException(e);
		}

		doc = docBuilder.newDocument();
		doc.setXmlVersion("1.0");

		Element rootElement = doc.createElement("metamorph");
		rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
		rootElement.setAttribute("version", "1");
		rootElement.setAttribute("entityMarker", ".");
		doc.appendChild(rootElement);

		Element meta = doc.createElement("meta");
		rootElement.appendChild(meta);

		Element metaName = doc.createElement("name");
		meta.appendChild(metaName);

		Element rules = doc.createElement("rules");
		rootElement.appendChild(rules);

		final List<String> metas = new ArrayList<>();

		for (Transformation transformation : transformations) {
			metas.add(transformation.getName());
			Element data = createTransformation(transformation);

			rules.appendChild(data);
		}

		metaName.setTextContent(Joiner.on(", ").join(metas));

		return this;
	}

	public PojoToXMLBuilder apply(final Transformation transformation) throws TransformationsCoverterException {
		return apply(Lists.newArrayList(transformation));
	}
}
