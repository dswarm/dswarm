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

import com.fasterxml.jackson.databind.JsonNode;
import rx.Observable;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

/**
 * Created by tgaengler on 03.03.16.
 */
public interface Exporter<RESULTFORMAT> {

	Observable<JsonNode> generate(final Observable<RESULTFORMAT> recordGDM, final OutputStream outputStream) throws XMLStreamException;
}
