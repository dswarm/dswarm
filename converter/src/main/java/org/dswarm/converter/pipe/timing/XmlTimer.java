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

	private final Deque<TimingContext> dtdContexts;
	private final Deque<TimingContext> entityContexts;
	private final Deque<TimingContext> cdataContexts;
	private final Deque<TimingContext> documentContexts;
	private final Deque<TimingContext> prefixContexts;
	private final Deque<TimingContext> elementContexts;

	@Inject
	private XmlTimer(
			@Named("Monitoring") final MetricRegistry registry,
			@Assisted final String prefix) {
		super(registry, prefix);

		dtdContexts = new LinkedList<>();
		entityContexts = new LinkedList<>();
		cdataContexts = new LinkedList<>();
		documentContexts = new LinkedList<>();
		prefixContexts = new LinkedList<>();
		elementContexts = new LinkedList<>();
	}

	@Override
	public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
		final TimingContext context = startMeasurement("DTD");
		dtdContexts.offerLast(context);
		try {
			getReceiver().startDTD(name, publicId, systemId);
		} catch (final Throwable t) {
			dtdContexts.removeLast();
		}
	}

	@Override
	public void endDTD() throws SAXException {
		try {
			getReceiver().endDTD();
		} finally {
			final TimingContext context = dtdContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void startEntity(final String name) throws SAXException {
		final TimingContext context = startMeasurement("entity");
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
		final TimingContext context = startMeasurement("CDATA");
		cdataContexts.offerLast(context);
		try {
			getReceiver().startCDATA();
		} catch (final Throwable t) {
			cdataContexts.removeLast();
		}
	}

	@Override
	public void endCDATA() throws SAXException {
		try {
			getReceiver().endCDATA();
		} finally {
			final TimingContext context = cdataContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void comment(final char[] ch, final int start, final int length) throws SAXException {
		try (final TimingContext ignore = startMeasurement("comment")) {
			getReceiver().comment(ch, start, length);
		}
	}

	@Override
	public void warning(final SAXParseException exception) throws SAXException {
		try (final TimingContext ignore = startMeasurement("warning")) {
			getReceiver().warning(exception);
		}
	}

	@Override
	public void error(final SAXParseException exception) throws SAXException {
		try (final TimingContext ignore = startMeasurement("error")) {
			getReceiver().error(exception);
		}
	}

	@Override
	public void fatalError(final SAXParseException exception) throws SAXException {
		try (final TimingContext ignore = startMeasurement("fatalError")) {
			getReceiver().fatalError(exception);
		}
	}

	@Override
	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
		try (final TimingContext ignore = startMeasurement("getReceiver")) {
			return getReceiver().resolveEntity(publicId, systemId);
		}
	}

	@Override
	public void notationDecl(final String name, final String publicId, final String systemId) throws SAXException {
		try (final TimingContext ignore = startMeasurement("notationDecl")) {
			getReceiver().notationDecl(name, publicId, systemId);
		}
	}

	@Override
	public void unparsedEntityDecl(final String name, final String publicId, final String systemId, final String notationName) throws SAXException {
		try (final TimingContext ignore = startMeasurement("unparsedEntityDecl")) {
			getReceiver().unparsedEntityDecl(name, publicId, systemId, notationName);
		}
	}

	@Override
	public void setDocumentLocator(final Locator locator) {
		try (final TimingContext ignore = startMeasurement("setDocumentLocator")) {
			getReceiver().setDocumentLocator(locator);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		final TimingContext context = startMeasurement("document");
		documentContexts.offerLast(context);
		try {
			getReceiver().startDocument();
		} catch (final Throwable t) {
			documentContexts.removeLast();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			getReceiver().endDocument();
		} finally {
			final TimingContext context = documentContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		final TimingContext context = startMeasurement("prefixMapping");
		prefixContexts.offerLast(context);
		try {
			getReceiver().startPrefixMapping(prefix, uri);
		} catch (final Throwable t) {
			prefixContexts.removeLast();
		}
	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		try {
			getReceiver().endPrefixMapping(prefix);
		} finally {
			final TimingContext context = prefixContexts.pollLast();
			if (context != null) {
				context.stop();
			}
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
		final TimingContext context = startMeasurement("element");
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
		try (final TimingContext ignore = startMeasurement("characters")) {
			getReceiver().characters(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
		try (final TimingContext ignore = startMeasurement("ignorableWhitespace")) {
			getReceiver().ignorableWhitespace(ch, start, length);
		}
	}

	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {
		try (final TimingContext ignore = startMeasurement("processingInstruction")) {
			getReceiver().processingInstruction(target, data);
		}
	}

	@Override
	public void skippedEntity(final String name) throws SAXException {
		try (final TimingContext ignore = startMeasurement("skippedEntity")) {
			getReceiver().skippedEntity(name);
		}
	}
}
