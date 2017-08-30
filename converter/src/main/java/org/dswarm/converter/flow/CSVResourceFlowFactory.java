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
package org.dswarm.converter.flow;

import com.google.inject.assistedinject.Assisted;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * @author phorn
 */
public interface CSVResourceFlowFactory {

	MonitoringCSVSourceResourceTriplesFlow fromDataModel(final DataModel dataModel);

	CSVSourceResourceTriplesFlow fromConfiguration(final Configuration configuration);

	CSVSourceResourceTriplesFlow fromConfigurationParameters(
			@Assisted("encoding") final String encoding,
			@Assisted("escapeCharacter") final Character escapeCharacter,
			@Assisted("quoteCharacter") final Character quoteCharacter,
			@Assisted("columnDelimiter") final Character columnDelimiter,
			@Assisted("rowDelimiter") final String rowDelimiter);

	CSVSourceResourceCSVJSONPreviewFlow jsonPreview(final Configuration configuration);

	CSVSourceResourceCSVJSONPreviewFlow jsonPreview(
			@Assisted("encoding") final String encoding,
			@Assisted("escapeCharacter") final Character escapeCharacter,
			@Assisted("quoteCharacter") final Character quoteCharacter,
			@Assisted("columnDelimiter") final Character columnDelimiter,
			@Assisted("rowDelimiter") final String rowDelimiter);

	CSVSourceResourceCSVPreviewFlow csvPreview(final Configuration configuration);

	CSVSourceResourceCSVPreviewFlow csvPreview(
			@Assisted("encoding") final String encoding,
			@Assisted("escapeCharacter") final Character escapeCharacter,
			@Assisted("quoteCharacter") final Character quoteCharacter,
			@Assisted("columnDelimiter") final Character columnDelimiter,
			@Assisted("rowDelimiter") final String rowDelimiter);
}
