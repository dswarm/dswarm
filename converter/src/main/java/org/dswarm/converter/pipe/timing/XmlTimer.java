/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.pipe.timing;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.XmlPipe;
import org.culturegraph.mf.framework.XmlReceiver;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public final class XmlTimer<R> extends TimerBased<XmlPipe<ObjectReceiver<R>>> implements XmlReceiver {

	private final Deque<TimingContext> entityContexts;
	private final Deque<TimingContext> elementContexts;

	@Inject
	private XmlTimer(
			@Named("Monitoring") final MetricRegistry registry,
			@Assisted final String prefix) {
		super(registry, prefix);

		entityContexts = new LinkedList<>();
		elementContexts = new LinkedList<>();
	}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
		getReceiver().startDTD(name, publicId, systemId);
	}

	@Override
	public void endDTD() throws SAXException {
		getReceiver().endDTD();
	}

	@Override
	public void startEntity(final String name) throws SAXException {
		final TimingContext context = startMeasurement(XML_ENTITIES);
		entityContexts.offerLast(context);
		try {
			getReceiver().startEntity(name);
		} catch (final Throwable t) {
			entityContexts.removeLast();
		}
	}

	@Override
	public void endEntity(final String name) throws SAXException {
		try {
			getReceiver().endEntity(name);
		} finally {
			final TimingContext context = entityContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void startCDATA() throws SAXException {
		getReceiver().startCDATA();
	}

	@Override
	public void endCDATA() throws SAXException {
		getReceiver().endCDATA();
	}

	@Override
	public void comment(final char[] ch, final int start, final int length) throws SAXException {
		getReceiver().comment(ch, start, length);
	}

	@Override
	public void warning(final SAXParseException exception) throws SAXException {
		getReceiver().warning(exception);
	}

	@Override
	public void error(final SAXParseException exception) throws SAXException {
		getReceiver().error(exception);
	}

	@Override
	public void fatalError(final SAXParseException exception) throws SAXException {
		getReceiver().fatalError(exception);
	}

	@Override
	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
		return getReceiver().resolveEntity(publicId, systemId);
	}

	@Override
	public void notationDecl(final String name, final String publicId, final String systemId) throws SAXException {
		getReceiver().notationDecl(name, publicId, systemId);
	}

	@Override
	public void unparsedEntityDecl(final String name, final String publicId, final String systemId, final String notationName) throws SAXException {
		getReceiver().unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	@Override
	public void setDocumentLocator(final Locator locator) {
		getReceiver().setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		getReceiver().startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		getReceiver().endDocument();
	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		getReceiver().startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		getReceiver().endPrefixMapping(prefix);
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
		final TimingContext context = startMeasurement(XML_ELEMENTS);
		elementContexts.offerLast(context);
		try {
			getReceiver().startElement(uri, localName, qName, atts);
		} catch (final Throwable t) {
			elementContexts.removeLast();
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		try {
			getReceiver().endElement(uri, localName, qName);
		} finally {
			final TimingContext context = elementContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		try (final TimingContext ignore = startMeasurement(XML_CHARACTERS)) {
			getReceiver().characters(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
		getReceiver().ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {
		getReceiver().processingInstruction(target, data);
	}

	@Override
	public void skippedEntity(final String name) throws SAXException {
		getReceiver().skippedEntity(name);
	}
}
