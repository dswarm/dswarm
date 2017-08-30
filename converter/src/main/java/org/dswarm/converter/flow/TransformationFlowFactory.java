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


import com.google.common.io.CharStreams;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.pipe.Filter;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.DMPMorphDefException;
import org.dswarm.converter.morph.FilterMorphScriptBuilder;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.resource.DataModel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;

import static org.dswarm.converter.flow.JSONTransformationFlow.*;

public interface TransformationFlowFactory<TRANSFORMATIONFLOW> {

	TRANSFORMATIONFLOW create(
			final Metamorph transformer,
			final String scriptArg,
			final Optional<DataModel> outputDataModelArg,
			final Optional<Filter> optionalSkipFilterArg);

	default TRANSFORMATIONFLOW fromString(
			final String morphScriptString) throws DMPConverterException {

		return fromAnything(readString(morphScriptString));
	}

	default TRANSFORMATIONFLOW fromString(
			final String morphScriptString,
			final String filterScriptString) throws DMPConverterException {

		return fromAnything(
				readString(morphScriptString),
				readString(filterScriptString));
	}

	default TRANSFORMATIONFLOW fromString(
			final String morphScriptString,
			final DataModel outputDataModel) throws DMPConverterException {

		return fromAnything(
				readString(morphScriptString),
				outputDataModel);
	}

	default TRANSFORMATIONFLOW fromString(
			final String morphScriptString,
			final String filterScriptString,
			final DataModel outputDataModel) throws DMPConverterException {

		return fromAnything(
				readString(morphScriptString),
				readString(filterScriptString),
				outputDataModel);
	}

	default TRANSFORMATIONFLOW fromFile(final File morphFile) throws DMPConverterException {

		return fromAnything(readFile(morphFile));
	}

	default TRANSFORMATIONFLOW fromFile(final File morphFile, final File filterFile) throws DMPConverterException {

		return fromAnything(
				readFile(morphFile),
				readFile(filterFile));
	}

	default TRANSFORMATIONFLOW fromFile(final String morphResource) throws DMPConverterException {

		return fromAnything(readResource(morphResource));
	}

	default TRANSFORMATIONFLOW fromFile(final String morphResource, final String filterResource) throws DMPConverterException {

		return fromAnything(
				readResource(morphResource),
				readResource(filterResource));
	}

	default TRANSFORMATIONFLOW fromTask(final Task task) throws DMPConverterException {

		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();
		final Optional<String> maybeFilterScript = Optional.ofNullable(
				new FilterMorphScriptBuilder().apply(task).toString());

		return fromAnything(
				readString(morphScriptString),
				readString(maybeFilterScript),
				Optional.ofNullable(task.getOutputDataModel()));
	}

	// private-ish

	default TRANSFORMATIONFLOW fromAnything(
			final Reader morphScript) throws DMPMorphDefException {
		return fromAnything(morphScript, Optional.empty(), Optional.empty());
	}

	default TRANSFORMATIONFLOW fromAnything(
			final Reader morphScript,
			final Reader filterScript) throws DMPMorphDefException {
		return fromAnything(morphScript, Optional.of(filterScript), Optional.empty());
	}

	default TRANSFORMATIONFLOW fromAnything(
			final Reader morphScript,
			final DataModel dataModel) throws DMPMorphDefException {
		return fromAnything(morphScript, Optional.empty(), Optional.of(dataModel));
	}

	default TRANSFORMATIONFLOW fromAnything(
			final Reader morphScript,
			final Reader filterScript,
			final DataModel dataModel) throws DMPMorphDefException {
		return fromAnything(morphScript, Optional.of(filterScript), Optional.of(dataModel));
	}

	default TRANSFORMATIONFLOW fromAnything(
			final Reader morphScript,
			final Optional<Reader> filterScript,
			final Optional<DataModel> outputDataModel) throws DMPMorphDefException {

		final String morphContent;
		try {
			morphContent = CharStreams.toString(morphScript);
			morphScript.close();
		} catch (final IOException e) {
			throw new DMPMorphDefException("could not read morph string", e);
		}

		final Metamorph morph = createMorph(new StringReader(morphContent));
		final Optional<Filter> filter;
		if (filterScript.isPresent()) {
			filter = Optional.of(createFilter(filterScript.get()));
		} else {
			filter = Optional.empty();
		}

		return create(morph, morphContent, outputDataModel, filter);
	}
}
