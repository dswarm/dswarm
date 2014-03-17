package de.avgl.dmp.converter.morph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.init.util.DMPStatics;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;

/**
 * Creates a metamorph script from a given {@link Task}.
 * 
 * @author phorn
 * @author tgaengler
 */
public class MorphScriptBuilder {

	private static final org.apache.log4j.Logger	LOG							= org.apache.log4j.Logger.getLogger(MorphScriptBuilder.class);

	private static final String						MAPPING_PREFIX				= "mapping";

	private static final DocumentBuilderFactory		DOC_FACTORY					= DocumentBuilderFactory.newInstance();

	private static final String						SCHEMA_PATH					= "schemata/metamorph.xsd";

	private static final TransformerFactory			TRANSFORMER_FACTORY;

	private static final String						TRANSFORMER_FACTORY_CLASS	= "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

	static {
		System.setProperty("javax.xml.transform.TransformerFactory", MorphScriptBuilder.TRANSFORMER_FACTORY_CLASS);
		TRANSFORMER_FACTORY = TransformerFactory.newInstance();
		MorphScriptBuilder.TRANSFORMER_FACTORY.setAttribute("indent-number", 4);

		final URL resource = Resources.getResource(MorphScriptBuilder.SCHEMA_PATH);
		final InputSupplier<InputStream> inputStreamInputSupplier = Resources.newInputStreamSupplier(resource);

		try (final InputStream schemaStream = inputStreamInputSupplier.getInput()) {

			// final StreamSource SCHEMA_SOURCE = new StreamSource(schemaStream);
			final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = null;

			try {

				// TODO: dummy schema right now, since it couldn't parse the metamorph schema for some reason
				schema = sf.newSchema();
			} catch (final SAXException e) {

				e.printStackTrace();
			}

			if (schema == null) {

				MorphScriptBuilder.LOG.error("couldn't parse schema");
			}

			MorphScriptBuilder.DOC_FACTORY.setSchema(schema);

		} catch (final IOException e1) {
			MorphScriptBuilder.LOG.error("couldn't read schema resource", e1);
		}
	}

	private Document								doc;

	private Element varDefinition(final String key, final String value) {
		final Element var = doc.createElement("var");
		var.setAttribute("name", key);
		var.setAttribute("value", value);

		return var;
	}

	public String render(final boolean indent, final Charset encoding) {
		final String defaultEncoding = encoding.name();
		final Transformer transformer;
		try {
			transformer = MorphScriptBuilder.TRANSFORMER_FACTORY.newTransformer();
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

		transformer.setOutputProperty(OutputKeys.ENCODING, defaultEncoding);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		final StreamResult result;
		try {
			result = new StreamResult(new OutputStreamWriter(stream, defaultEncoding));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		try {
			transformer.transform(new DOMSource(doc), result);
		} catch (final TransformerException e) {
			e.printStackTrace();
			return null;
		}

		try {
			return stream.toString(defaultEncoding);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String render(final boolean indent) {
		return render(indent, Charset.forName("UTF-8"));
	}

	@Override
	public String toString() {
		return render(true);
	}

	public File toFile() throws IOException {
		final String str = render(false);

		final File file = File.createTempFile("avgl_dmp", ".tmp");

		final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(str);
		bw.close();

		return file;
	}

	// public MorphScriptBuilder apply(final List<Transformation> transformations) throws DMPConverterException {
	// final DocumentBuilder docBuilder;
	// try {
	// docBuilder = DOC_FACTORY.newDocumentBuilder();
	// } catch (ParserConfigurationException e) {
	// throw new DMPConverterException(e.getMessage());
	// }
	//
	// doc = docBuilder.newDocument();
	// doc.setXmlVersion("1.0");
	//
	// final Element rootElement = doc.createElement("metamorph");
	// rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
	// rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	// rootElement.setAttribute("xsi:schemaLocation", "http://www.culturegraph.org/metamorph metamorph.xsd");
	// rootElement.setAttribute("entityMarker", ".");
	// rootElement.setAttribute("version", "1");
	// doc.appendChild(rootElement);
	//
	// final Element meta = doc.createElement("meta");
	// rootElement.appendChild(meta);
	//
	// final Element metaName = doc.createElement("name");
	// meta.appendChild(metaName);
	//
	// // final Element vars = doc.createElement("vars");
	// // rootElement.appendChild(vars);
	//
	// final Element rules = doc.createElement("rules");
	// rootElement.appendChild(rules);
	//
	// final List<String> metas = Lists.newArrayList();
	//
	// for (final Transformation transformation : transformations) {
	// metas.add(transformation.getName());
	//
	// // for (final Element var: createVarDefinitions(transformation)) {
	// // vars.appendChild(var);
	// // }
	//
	// final Element data = createTransformation(transformation);
	//
	// rules.appendChild(data);
	// }
	//
	// metaName.setTextContent(Joiner.on(", ").join(metas));
	//
	// return this;
	// }

	// public MorphScriptBuilder apply(final Transformation transformation) throws DMPConverterException {
	//
	// return apply(Lists.newArrayList(transformation));
	// }

	public MorphScriptBuilder apply(final Task task) throws DMPConverterException {

		final DocumentBuilder docBuilder;
		try {
			docBuilder = MorphScriptBuilder.DOC_FACTORY.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new DMPConverterException(e.getMessage());
		}

		doc = docBuilder.newDocument();
		doc.setXmlVersion("1.1");

		final Element rootElement = doc.createElement("metamorph");
		rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:schemaLocation", "http://www.culturegraph.org/metamorph metamorph.xsd");
		rootElement.setAttribute("entityMarker", DMPStatics.ATTRIBUTE_DELIMITER.toString());
		rootElement.setAttribute("version", "1");
		doc.appendChild(rootElement);

		final Element meta = doc.createElement("meta");
		rootElement.appendChild(meta);

		final Element metaName = doc.createElement("name");
		meta.appendChild(metaName);

		final Element rules = doc.createElement("rules");
		rootElement.appendChild(rules);

		final List<String> metas = Lists.newArrayList();

		for (final Mapping mapping : task.getJob().getMappings()) {
			metas.add(MorphScriptBuilder.MAPPING_PREFIX + mapping.getId());

			createTransformation(rules, mapping);

		}

		metaName.setTextContent(Joiner.on(", ").join(metas));

		return this;
	}

	private void createTransformation(final Element rules, final Mapping mapping) {

		// first handle the parameter mapping from the attribute paths of the mapping to the transformation component

		final Component transformationComponent = mapping.getTransformation();

		if (transformationComponent == null) {

			MorphScriptBuilder.LOG.debug("transformation component for mapping '" + mapping.getId() + "' was empty");
			return;
		}

		// get all input attribute paths and create datas for them

		final Set<MappingAttributePathInstance> inputAttributePathInstances = mapping.getInputAttributePaths();

		final List<String> inputAttributePaths = new LinkedList<String>();

		for (final Iterator<MappingAttributePathInstance> iterator = inputAttributePathInstances.iterator(); iterator.hasNext();) {

			final MappingAttributePathInstance mappingAttributePathInstance = iterator.next();

			final String inputAttributePathString = mappingAttributePathInstance.getAttributePath().toAttributePath();

			final Element data = doc.createElement("data");
			data.setAttribute("source", inputAttributePathString);
			data.setAttribute("name", "@" + getKeyParameterMapping(inputAttributePathString, transformationComponent));

			// TODO: filter inputAttributePath in data section

			inputAttributePaths.add(inputAttributePathString);

			rules.appendChild(data);
		}

		final String outputAttributePath = mapping.getOutputAttributePath().getAttributePath().toAttributePath();

		final Element dataOutput = doc.createElement("data");
		dataOutput.setAttribute("source", "@" + getKeyParameterMapping(outputAttributePath, transformationComponent));
		dataOutput.setAttribute("name", outputAttributePath);
		rules.appendChild(dataOutput);

		final Function transformationFunction = transformationComponent.getFunction();

		if (transformationFunction == null) {

			MorphScriptBuilder.LOG.debug("transformation component's function for mapping '" + mapping.getId() + "' was empty");
			return;
		}

		switch (transformationFunction.getFunctionType()) {

			case Function:

				// TODO: process simple function

				MorphScriptBuilder.LOG.error("transformation component's function for mapping '" + mapping.getId()
						+ "' was a real FUNCTION. this is not supported right now.");

				break;

			case Transformation:

				// TODO: process simple input -> output mapping (?)

				final Transformation transformation = (Transformation) transformationFunction;

				final Set<Component> components = transformation.getComponents();

				if (components == null) {

					MorphScriptBuilder.LOG.debug("transformation component's transformation's components for mapping '" + mapping.getId()
							+ "' are empty");
					return;
				}

				for (final Component component : components) {

					String[] inputStrings = {};

					final Map<String, String> componentParameterMapping = component.getParameterMappings();

					if (componentParameterMapping != null) {

						for (final Entry<String, String> parameterMapping : componentParameterMapping.entrySet()) {

							if (parameterMapping.getKey().equals("inputString")) {

								inputStrings = parameterMapping.getValue().split(",");

								break;
							}
						}
					}

					final LinkedList<String> sourceAttributes = new LinkedList<String>();

					for (final String inputString : inputStrings) {

						sourceAttributes.add(inputString);
					}

					if (component.getInputComponents() != null) {

						for (final Component inputComponent : component.getInputComponents()) {

							sourceAttributes.add("component" + inputComponent.getId());
						}
					}

					if (sourceAttributes.size() > 1) {

						String collectionNameAttribute = null;

						if (component.getOutputComponents() == null) {

							collectionNameAttribute = getKeyParameterMapping(outputAttributePath, transformationComponent);

						} else {

							collectionNameAttribute = "component" + component.getId();
						}

						final Element collection = createCollectionTag(component, collectionNameAttribute, sourceAttributes);

						rules.appendChild(collection);

					} else if (sourceAttributes.size() == 1) {

						String dataNameAttribute = null;

						if (component.getOutputComponents() == null) {

							dataNameAttribute = getKeyParameterMapping(outputAttributePath, transformationComponent);

						} else {

							dataNameAttribute = "component" + component.getId();
						}

						final Element data = createDataTag(component, dataNameAttribute, sourceAttributes.get(0));

						rules.appendChild(data);

					}

				}

				break;
		}

	}

	private void createParameters(final Map<String, String> parameterMappings, final Element component) {

		// TODO: parse parameter values that can be simple string values, JSON objects or JSON arrays (?)
		// => for now we expect only simple string values

		if (parameterMappings != null) {

			for (final Entry<String, String> parameterMapping : parameterMappings.entrySet()) {

				if (parameterMapping.getKey() != null) {

					if (parameterMapping.getKey().equals("inputString")) {
						continue;
					}

					if (parameterMapping.getValue() != null) {

						final Attr param = doc.createAttribute(parameterMapping.getKey());
						param.setValue(parameterMapping.getValue());
						component.setAttributeNode(param);
					}
				}
			}
		}
	}

	private Element createDataTag(final Component singleInputComponent, final String dataNameAttribute, final String dataSourceAttribute) {

		final Element data = doc.createElement("data");

		data.setAttribute("source", "@" + dataSourceAttribute);

		data.setAttribute("name", "@" + dataNameAttribute);

		final Element comp = doc.createElement(singleInputComponent.getFunction().getName());

		createParameters(singleInputComponent.getParameterMappings(), comp);

		data.appendChild(comp);

		return data;
	}

	private Element createCollectionTag(final Component multipleInputComponent, final String collectionNameAttribute,
			final List<String> collectionSourceAttributes) {

		final Element collection;

		if (multipleInputComponent.getFunction().getName().equals("concat")) {

			final Map<String, String> parameters = multipleInputComponent.getParameterMappings();

			String valueString = "";

			String delimiterString = ", ";

			if (parameters.get("prefix") != null) {
				valueString = parameters.get("prefix").toString();
			}

			if (parameters.get("delimiter") != null) {
				delimiterString = parameters.get("delimiter").toString();
			}

			collection = doc.createElement("combine");

			collection.setAttribute("name", "@" + collectionNameAttribute);

			for (int i = 0; i < collectionSourceAttributes.size(); i++) {

				valueString += "${" + collectionSourceAttributes.get(i) + "}";

				if ((i + 1) < collectionSourceAttributes.size()) {
					valueString += delimiterString;
				}

				final Element collectionData = doc.createElement("data");

				collectionData.setAttribute("source", "@" + collectionSourceAttributes.get(i));

				collectionData.setAttribute("name", collectionSourceAttributes.get(i));

				collection.appendChild(collectionData);

			}

			if (parameters.get("postfix") != null) {
				valueString += parameters.get("postfix").toString();
			}

			collection.setAttribute("value", valueString);

		} else {

			collection = doc.createElement(multipleInputComponent.getFunction().getName());

			createParameters(multipleInputComponent.getParameterMappings(), collection);

			collection.setAttribute("name", "@" + collectionNameAttribute);

			for (final String sourceAttribute : collectionSourceAttributes) {

				final Element collectionData = doc.createElement("data");

				collectionData.setAttribute("source", "@" + sourceAttribute);

				collection.appendChild(collectionData);
			}
		}
		return collection;
	}

	private String getKeyParameterMapping(final String attributePathString, final Component transformationComponent) {

		String attributePathKey = null;

		final Map<String, String> transformationParameterMapping = transformationComponent.getParameterMappings();

		for (final Entry<String, String> parameterMapping : transformationParameterMapping.entrySet()) {

			if (parameterMapping.getValue().equals(attributePathString)) {

				attributePathKey = parameterMapping.getKey();
			}
		}

		return attributePathKey;
	}

}
