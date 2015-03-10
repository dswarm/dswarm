/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.morph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;

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

import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.model.job.Task;

/**
 * @author tgaengler
 */
public abstract class AbstractMorphScriptBuilder<MORPHSCRIPTBUILDERIMPL extends AbstractMorphScriptBuilder> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractMorphScriptBuilder.class);

	private static final DocumentBuilderFactory DOC_FACTORY = DocumentBuilderFactory.newInstance();

	private static final TransformerFactory TRANSFORMER_FACTORY;

	private static final String TRANSFORMER_FACTORY_CLASS = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

	private static final String SCHEMA_PATH = "schemata/metamorph.xsd";

	protected Document doc;

	protected Element metaName;

	protected Element rules;

	protected Element maps;

	static {

		System.setProperty("javax.xml.transform.TransformerFactory", AbstractMorphScriptBuilder.TRANSFORMER_FACTORY_CLASS);
		TRANSFORMER_FACTORY = TransformerFactory.newInstance();
		AbstractMorphScriptBuilder.TRANSFORMER_FACTORY.setAttribute("indent-number", 4);

		final URL resource = Resources.getResource(AbstractMorphScriptBuilder.SCHEMA_PATH);
		final CharSource inputStreamInputSupplier = Resources.asCharSource(resource, Charsets.UTF_8);

		try (final Reader schemaStream = inputStreamInputSupplier.openStream()) {

			// final StreamSource SCHEMA_SOURCE = new StreamSource(schemaStream);
			final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = null;

			try {

				// TODO: dummy schema right now, since it couldn't parse the metamorph schema for some reason
				schema = sf.newSchema();
			} catch (final SAXException e) {

				LOG.error("couldn't read schema", e);
			}

			if (schema == null) {

				AbstractMorphScriptBuilder.LOG.error("couldn't parse schema");
			}

			AbstractMorphScriptBuilder.DOC_FACTORY.setSchema(schema);

		} catch (final IOException e1) {

			AbstractMorphScriptBuilder.LOG.error("couldn't read schema resource", e1);
		}
	}

	protected static final String METAMORPH_IDENTIFIER = "metamorph";

	protected static final String METAMORPH_ELEMENT_META_INFORMATION = "meta";

	protected static final String METAMORPH_ELEMENT_RULESET = "rules";

	protected static final String METAMORPH_ELEMENT_MAP_CONTAINER = "maps";

	public String render(final boolean indent, final Charset encoding) {

		if(doc == null) {

			// don't render, when there's no document

			LOG.debug("no document available for morph script rendering");

			return null;
		}

		final String defaultEncoding = encoding.name();
		final Transformer transformer;
		try {

			transformer = AbstractMorphScriptBuilder.TRANSFORMER_FACTORY.newTransformer();
		} catch (final TransformerConfigurationException e) {

			LOG.error("couldn't create transformer for morph script builder");

			return null;
		}

		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

		transformer.setOutputProperty(OutputKeys.ENCODING, defaultEncoding);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		final StreamResult result;

		try {

			result = new StreamResult(new OutputStreamWriter(stream, defaultEncoding));
		} catch (final UnsupportedEncodingException e) {

			LOG.error("couldn't render morph script, because the encoding is not supported", e);

			return null;
		}

		try {

			transformer.transform(new DOMSource(doc), result);
		} catch (final TransformerException e) {

			LOG.error("couldn't render morph script", e);

			return null;
		}

		try {

			return stream.toString(defaultEncoding);
		} catch (final UnsupportedEncodingException e) {

			LOG.error("couldn't render morph script, because the encoding is not supported", e);

			return null;
		}
	}

	public String render(final boolean indent) {

		return render(indent, Charsets.UTF_8);
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

	public MORPHSCRIPTBUILDERIMPL apply(final Task task) throws DMPConverterException {

		final DocumentBuilder docBuilder;
		try {
			docBuilder = AbstractMorphScriptBuilder.DOC_FACTORY.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new DMPConverterException(e.getMessage());
		}

		doc = docBuilder.newDocument();
		doc.setXmlVersion("1.1");

		final Element rootElement = doc.createElement(METAMORPH_IDENTIFIER);
		rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:schemaLocation", "http://www.culturegraph.org/metamorph metamorph.xsd");
		rootElement.setAttribute("entityMarker", DMPStatics.ATTRIBUTE_DELIMITER.toString());
		rootElement.setAttribute("version", "1");
		doc.appendChild(rootElement);

		final Element meta = doc.createElement(METAMORPH_ELEMENT_META_INFORMATION);
		rootElement.appendChild(meta);

		metaName = doc.createElement("name");
		meta.appendChild(metaName);

		rules = doc.createElement(METAMORPH_ELEMENT_RULESET);
		rootElement.appendChild(rules);

		maps = doc.createElement(METAMORPH_ELEMENT_MAP_CONTAINER);
		rootElement.appendChild(maps);

		return (MORPHSCRIPTBUILDERIMPL) this;
	}
}
