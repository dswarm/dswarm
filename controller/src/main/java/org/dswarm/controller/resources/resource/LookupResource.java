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
package org.dswarm.controller.resources.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;

/**
 * for lookup table file processing
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/lookup", description = "Provides lookup table file processing endpoints.")
@Path("lookup")
public class LookupResource {

	private static final Logger LOG = LoggerFactory.getLogger(LookupResource.class);

	private static final char   escapeCharacter = ConfigurationStatics.DEFAULT_ESCAPE_CHARACTER;
	private static final char   quoteCharacter  = ConfigurationStatics.DEFAULT_QUOTE_CHARACTER;
	private static final String lineEnding      = ConfigurationStatics.DEFAULT_ROW_DELIMITER;

	private final Provider<ObjectMapper> objectMapperProvider;

	@Inject
	public LookupResource(final Provider<ObjectMapper> objectMapperProviderArg) {

		objectMapperProvider = objectMapperProviderArg;
	}

	/**
	 *
	 * @param lookupTableFileInputStream the input stream of the lookup table file
	 * @param lookupTableFileDetail lookup table file metadata
	 * @param columnDelimiter the column delimiter of the lookup table
	 * @return
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "upload a lookup table file and processes it to JSON", notes = "Returns a JSON object with key/value pairs of the lookup tabke", response = ObjectNode.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "lookup table file was successfully processed to a JSON object"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Path("/read")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportAllRDFForDownload(
			@ApiParam(value = "lookup table file input stream", required = true) @FormDataParam("file") final InputStream lookupTableFileInputStream,
			@ApiParam("lookup table file metadata") @FormDataParam("file") final FormDataContentDisposition lookupTableFileDetail,
			@ApiParam(value = "lookup table column delimiter", required = true) @FormDataParam("column_delimiter") final String columnDelimiter)
			throws DMPControllerException {

		LookupResource.LOG.debug("try to process lookup table file '{}'", lookupTableFileDetail.getFileName());

		if (lookupTableFileInputStream == null) {

			final String message = "couldn't process lookup table file to JSON, because the lookup table file input stream was null";

			LookupResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final char finalColumnDelimiter;

		if (columnDelimiter != null) {

			finalColumnDelimiter = columnDelimiter.charAt(0);
		} else {

			finalColumnDelimiter = ConfigurationStatics.DEFAULT_COLUMN_DELIMITER;
		}

		final Reader reader = new InputStreamReader(lookupTableFileInputStream);

		final CSVFormat csvFormat = CSVFormat.newFormat(finalColumnDelimiter).withQuote(quoteCharacter).withEscape(escapeCharacter)
				.withRecordSeparator(lineEnding).withIgnoreEmptyLines(true).withIgnoreSurroundingSpaces(true);

		final CSVParser parser = createParser(reader, csvFormat);
		final ObjectNode json = parseLookupTable(parser);
		final String jsonString = serializeLookupTableJSON(json);

		LookupResource.LOG.debug("successfully processed lookup table file '{}'", lookupTableFileDetail.getFileName());

		return Response.ok(jsonString).build();
	}

	private CSVParser createParser(Reader reader, CSVFormat csvFormat) throws DMPControllerException {

		try {

			return new CSVParser(reader, csvFormat);
		} catch (final IOException e) {

			final String message = "couldn't process lookup table to JSON, because couldn't read it with the given CSV format configuration";

			LookupResource.LOG.error(message);

			throw new DMPControllerException(message, e);
		}
	}

	private ObjectNode parseLookupTable(final CSVParser parser) throws DMPControllerException {

		final Iterator<CSVRecord> csvIter = parser.iterator();

		final ObjectNode json = objectMapperProvider.get().createObjectNode();

		while (csvIter.hasNext()) {

			final CSVRecord csvRecord = csvIter.next();

			if (csvRecord.size() != 2) {

				final String message = "couldn't process lookup table to JSON, because the CSV file has not exactly two columns";

				LookupResource.LOG.error(message);

				throw new DMPControllerException(message);
			}

			final String key = csvRecord.get(0).trim();
			final String value = csvRecord.get(1).trim();

			json.put(key, value);
		}

		return json;
	}

	private String serializeLookupTableJSON(final ObjectNode objectNode) throws DMPControllerException {

		String objectJSONString;

		try {

			objectJSONString = objectMapperProvider.get().writeValueAsString(objectNode);
		} catch (final JsonProcessingException e) {

			final String message = "couldn't process lookup table to JSON, because couldn't serialize JSON.";

			LookupResource.LOG.error(message);

			throw new DMPControllerException(message, e);
		}

		if (objectJSONString == null) {

			final String message = "couldn't process lookup table to JSON, couldn't serialize JSON correctly.";

			LookupResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		return objectJSONString;
	}
}
