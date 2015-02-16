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
package org.dswarm.converter.flow;

import java.io.Reader;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.xml.XmlDecoder;
import org.culturegraph.mf.stream.source.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.GDMModelReceiver;
import org.dswarm.converter.mf.stream.source.BOMResourceOpener;
import org.dswarm.converter.mf.stream.source.XMLGDMEncoder;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;

/**
 * Flow that transforms a given XML source into GDM statements.
 * 
 * @author tgaengler
 */
public class XMLSourceResourceGDMStmtsFlow {

	private static final Logger			LOG	= LoggerFactory.getLogger(XMLSourceResourceGDMStmtsFlow.class);

	private final Optional<String>		recordTagName;
	private final Optional<DataModel>	dataModel;

	public XMLSourceResourceGDMStmtsFlow(final DataModel dataModel) throws DMPConverterException {

		if (dataModel == null) {

			throw new DMPConverterException("the data model shouldn't be null");
		}

		if (dataModel.getConfiguration() == null) {

			throw new DMPConverterException("the data model configuration shouldn't be null");
		}

		if (dataModel.getConfiguration().getParameters() == null) {

			throw new DMPConverterException("the data model configuration parameters shouldn't be null");
		}

		if (dataModel.getUuid() != null) {

			this.dataModel = Optional.of(dataModel);
		} else {

			this.dataModel = Optional.absent();
		}

		recordTagName = getStringParameter(dataModel.getConfiguration(), ConfigurationStatics.RECORD_TAG);
	}

	public List<GDMModel> applyRecord(final String record) {

		final StringReader opener = new StringReader();

		return apply(record, opener);
	}

	public List<GDMModel> applyResource(final String resourcePath) {

		final BOMResourceOpener opener = new BOMResourceOpener();

		return apply(resourcePath, opener);
	}

	List<GDMModel> apply(final String object, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		final XmlDecoder decoder = new XmlDecoder();

		final XMLGDMEncoder encoder;

		if (recordTagName.isPresent()) {

			encoder = new XMLGDMEncoder(recordTagName.get(), dataModel);
		} else {

			encoder = new XMLGDMEncoder(dataModel);
		}
		final GDMModelReceiver writer = new GDMModelReceiver();

		opener.setReceiver(decoder).setReceiver(encoder).setReceiver(writer);

		opener.process(object);

		return writer.getCollection();
	}

	private Optional<String> getStringParameter(final Configuration configuration, final String key) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return Optional.absent();
		}

		return Optional.of(jsonNode.asText());
	}

	private JsonNode getParameterValue(final Configuration configuration, final String key) throws DMPConverterException {

		if (key == null) {

			throw new DMPConverterException("the parameter key shouldn't be null");
		}

		final JsonNode valueNode = configuration.getParameter(key);

		if (valueNode == null) {

			XMLSourceResourceGDMStmtsFlow.LOG.debug("couldn't find value for parameter '" + key
					+ "'; try to utilise default value for this parameter");
		}

		return valueNode;
	}
}
