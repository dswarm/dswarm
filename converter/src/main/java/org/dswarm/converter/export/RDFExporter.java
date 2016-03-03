/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.export;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import rx.Observable;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

/**
 * Created by tgaengler on 03.03.16.
 */
public class RDFExporter implements Exporter {

	private final MediaType mediaType;
	final Lang rdfSerializationFormat;

	public RDFExporter(final MediaType mediaTypeArg) {

		mediaType = mediaTypeArg;
		rdfSerializationFormat = RDFLanguages.contentTypeToLang(mediaType.toString());
	}

	@Override
	public Observable<JsonNode> generate(final Observable<JsonNode> recordGDM, final OutputStream outputStream) throws XMLStreamException {

		StreamRDF writer = StreamRDFWriter.getWriterStream(outputStream, rdfSerializationFormat) ;

		return null;
	}
}
