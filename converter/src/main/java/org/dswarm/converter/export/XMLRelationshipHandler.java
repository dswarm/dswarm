/**
 * This file is part of d:swarm graph extension.
 *
 * d:swarm graph extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * d:swarm graph extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with d:swarm graph extension.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dswarm.converter.export;

import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.databind.JsonNode;

import org.dswarm.common.web.URI;
import org.dswarm.converter.DMPConverterException;

/**
 * @author tgaengler
 */
public interface XMLRelationshipHandler {

	void handleRelationship(final URI predicateURI, final JsonNode node) throws DMPConverterException, XMLStreamException;
}
