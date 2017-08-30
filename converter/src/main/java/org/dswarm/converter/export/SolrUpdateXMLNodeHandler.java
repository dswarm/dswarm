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
package org.dswarm.converter.export;

import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.databind.JsonNode;

import org.dswarm.common.web.URI;
import org.dswarm.converter.DMPConverterException;

/**
 *
 * @author tgaengler
 *
 */
public interface SolrUpdateXMLNodeHandler {

	void handleNode(final String previousPredicateTag, final JsonNode node) throws DMPConverterException, XMLStreamException;
}
