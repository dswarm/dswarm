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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import org.culturegraph.mf.exceptions.MorphDefException;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.DMPMorphDefException;
import org.dswarm.converter.mf.stream.GDMEncoder;
import org.dswarm.converter.mf.stream.GDMModelReceiver;
import org.dswarm.converter.mf.stream.reader.JsonNodeReader;
import org.dswarm.converter.morph.MorphScriptBuilder;
import org.dswarm.converter.pipe.StreamJsonCollapser;
import org.dswarm.converter.pipe.StreamUnflattener;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

/**
 * Flow that executes a given set of transformations on data of a given data model.
 *
 * @author phorn
 * @author tgaengler
 * @author sreichert
 * @author polowins
 */
public class TransformationFlow {

	private static final Logger LOG               = LoggerFactory.getLogger(TransformationFlow.class);
	private static final String BIBO_DOCUMENT_URI = "http://purl.org/ontology/bibo/Document";

	private final Metamorph transformer;

	private final String script;

	private final Optional<DataModel> outputDataModel;

	private final Provider<InternalModelServiceFactory> internalModelServiceFactoryProvider;

	public TransformationFlow(final Metamorph transformer, final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) {

		this.transformer = transformer;
		script = null;
		outputDataModel = Optional.absent();
		internalModelServiceFactoryProvider = internalModelServiceFactoryProviderArg;
	}

	public TransformationFlow(final Metamorph transformer, final String scriptArg,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) {

		this.transformer = transformer;
		script = scriptArg;
		outputDataModel = Optional.absent();
		internalModelServiceFactoryProvider = internalModelServiceFactoryProviderArg;
	}

	public TransformationFlow(final Metamorph transformer, final String scriptArg, final DataModel outputDataModelArg,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) {

		this.transformer = transformer;
		script = scriptArg;
		outputDataModel = Optional.of(outputDataModelArg);
		internalModelServiceFactoryProvider = internalModelServiceFactoryProviderArg;
	}

	// public String applyRecord(final String record) {
	//
	// final StringReader opener = new StringReader();
	//
	// return apply(record, opener);
	// }

	public String getScript() {

		return script;
	}

	public String applyRecord(final String record) throws DMPConverterException {

		// TODO: convert JSON string to Iterator with tuples of string + JsonNode pairs
		List<Tuple<String, JsonNode>> tuplesList = null;

		try {

			tuplesList = DMPPersistenceUtil.getJSONObjectMapper().readValue(record, new TypeReference<List<Tuple<String, JsonNode>>>() {

			});
		} catch (final JsonParseException e) {

			TransformationFlow.LOG.debug("couldn't parse the transformation result tuples to a JSON string");
		} catch (final JsonMappingException e) {

			TransformationFlow.LOG.debug("couldn't map the transformation result tuples to a JSON string");
		} catch (final IOException e) {

			TransformationFlow.LOG.debug("something went wrong while processing the transformation result tuples to a JSON string");
		}

		if (tuplesList == null) {

			TransformationFlow.LOG.debug("couldn't process the transformation result tuples to a JSON string");

			return null;
		}

		return apply(tuplesList.iterator(), false);
	}

	public String applyResource(final String resourcePath) throws DMPConverterException {

		String resourceString = null;

		try {

			resourceString = DMPPersistenceUtil.getResourceAsString(resourcePath);
		} catch (final IOException e) {

			// TODO: log something
		}

		if (resourceString == null) {

			// TODO log something

			return null;
		}

		return applyRecord(resourceString);
	}

	public String apply(final Iterator<Tuple<String, JsonNode>> tuples, final ObjectPipe<Iterator<Tuple<String, JsonNode>>, StreamReceiver> opener,
			final boolean writeResultToDatahub) throws DMPConverterException {

		// final String recordDummy = "record";

		final StreamUnflattener unflattener = new StreamUnflattener("", DMPStatics.ATTRIBUTE_DELIMITER);
		final StreamJsonCollapser collapser = new StreamJsonCollapser();
		final GDMEncoder converter = new GDMEncoder(outputDataModel);
		final GDMModelReceiver writer = new GDMModelReceiver();

		// .setReceiver(collapser)
		opener.setReceiver(transformer).setReceiver(unflattener).setReceiver(converter).setReceiver(writer);
		//opener.setReceiver(transformer).setReceiver(converter).setReceiver(writer);

		opener.process(tuples);
		opener.closeStream();
		// objectReceiver.closeStream();

		// return stringWriter.toString();

		final ImmutableList<GDMModel> gdmModels = writer.getCollection();

		final org.dswarm.graph.json.Model model = new org.dswarm.graph.json.Model();
		String recordClassUri = null;

		// transform to FE friendly JSON => or use Model#toJSON() ;)

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		// final ArrayNode result = objectMapper.createArrayNode();
		final Set<String> recordURIs = Sets.newLinkedHashSet();

		final List<GDMModel> finalGDMModels = Lists.newArrayList();

		for (final GDMModel gdmModel : gdmModels) {

			// result.add(rdfModel.toRawJSON());

			if (gdmModel.getModel() == null) {

				finalGDMModels.add(gdmModel);

				continue;
			}

			final Set<String> recordURIsFromGDMModel = gdmModel.getRecordURIs();

			if (recordURIsFromGDMModel == null || recordURIsFromGDMModel.isEmpty()) {

				// skip, since it seems to look like that there are no records in the model

				continue;
			}

			for (final org.dswarm.graph.json.Resource jsonResource : gdmModel.getModel().getResources()) {

				model.addResource(jsonResource);
			}

			if (recordClassUri == null) {

				recordClassUri = gdmModel.getRecordClassURI();
			}

			final GDMModel finalGDMModel;

			// TODO: this a WORKAROUND to insert a default type (bibo:Document) for records in the output data model
			if (gdmModel.getRecordClassURI() == null) {

				final String recordURI = gdmModel.getRecordURIs().iterator().next();
				final String defaultRecordClassURI = BIBO_DOCUMENT_URI;

				final Resource recordResource = model.getResource(recordURI);

				if (recordResource != null) {

					// TODO check this: subject OK?
					recordResource.addStatement(new ResourceNode(recordResource.getUri()), new Predicate(GDMUtil.RDF_type),
							new ResourceNode(defaultRecordClassURI));
				}

				// re-write GDM model
				finalGDMModel = new GDMModel(gdmModel.getModel(), recordURI, defaultRecordClassURI);
			} else {

				finalGDMModel = gdmModel;
			}

			if (recordClassUri == null) {

				recordClassUri = finalGDMModel.getRecordClassURI();
			}

			finalGDMModels.add(finalGDMModel);

			recordURIs.addAll(recordURIsFromGDMModel);
		}

		if (recordClassUri == null) {

			if (outputDataModel.isPresent()) {

				final Schema schema = outputDataModel.get().getSchema();

				if (schema != null) {

					final Clasz recordClass = schema.getRecordClass();

					if (recordClass != null) {

						recordClassUri = recordClass.getUri();
					}
				}
			}
		}

		// note: we may don't really need the record class uri here (I guess), because we can provide the record identifiers
		// separately
		final GDMModel gdmModel = new GDMModel(model, null, recordClassUri);
		gdmModel.setRecordURIs(recordURIs);

		if (writeResultToDatahub) {

			if (outputDataModel.isPresent() && outputDataModel.get().getUuid() != null) {

				// write result to graph db
				final InternalModelService internalModelService = internalModelServiceFactoryProvider.get().getInternalGDMGraphService();

				try {

					internalModelService.createObject(outputDataModel.get().getUuid(), gdmModel);
				} catch (final DMPPersistenceException e1) {

					final String message = "couldn't persist the result of the transformation: " + e1.getMessage();

					TransformationFlow.LOG.error(message);

					throw new DMPConverterException(message, e1);
				}

			} else {

				final String message = "couldn't persist the result of the transformation, because there is no output data model assigned at this task";

				TransformationFlow.LOG.error(message);

				throw new DMPConverterException(message);
			}
		}

		final String resultString;

		try {

			resultString = objectMapper.writeValueAsString(gdmModel.toJSON());
		} catch (final JsonProcessingException e) {

			final String message = "couldn't convert result into JSON";

			TransformationFlow.LOG.error(message);

			throw new DMPConverterException(message, e);
		}

		return resultString;
	}

	public String apply(final Iterator<Tuple<String, JsonNode>> tuples, final boolean writeResultToDatahub) throws DMPConverterException {

		final JsonNodeReader opener = new JsonNodeReader();

		return apply(tuples, opener, writeResultToDatahub);
	}

	public static TransformationFlow fromString(final String morphScriptString,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) {
		final java.io.StringReader stringReader = new java.io.StringReader(morphScriptString);
		final Metamorph transformer = new Metamorph(stringReader);

		return new TransformationFlow(transformer, morphScriptString, internalModelServiceFactoryProviderArg);
	}

	public static TransformationFlow fromString(final String morphScriptString, final DataModel outputDataModel,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) throws DMPConverterException {

		final java.io.StringReader stringReader = new java.io.StringReader(morphScriptString);
		final Metamorph transformer;
		try {
			transformer = new Metamorph(stringReader);
		} catch (final MorphDefException e) {
			throw new DMPMorphDefException(e.getMessage(), e);
		}

		return new TransformationFlow(transformer, morphScriptString, outputDataModel, internalModelServiceFactoryProviderArg);
	}

	public static TransformationFlow fromFile(final File file, final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg)
			throws FileNotFoundException, DMPConverterException {

		final FileInputStream is = new FileInputStream(file);
		final Reader inputSource = TransformationFlow.getReader(is);

		final Metamorph transformer = new Metamorph(inputSource);

		return new TransformationFlow(transformer, internalModelServiceFactoryProviderArg);
	}

	public static TransformationFlow fromFile(final String resourcePath,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) throws DMPConverterException {

		final InputStream morph = TransformationFlow.class.getClassLoader().getResourceAsStream(resourcePath);
		final Reader inputSource = TransformationFlow.getReader(morph);
		final Metamorph transformer = new Metamorph(inputSource);

		return new TransformationFlow(transformer, internalModelServiceFactoryProviderArg);
	}

	public static TransformationFlow fromTask(final Task task, final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg)
			throws DMPConverterException {

		final String morphScriptString = new MorphScriptBuilder().apply(task).toString();

		final DataModel outputDataModel = task.getOutputDataModel();

		return TransformationFlow.fromString(morphScriptString, outputDataModel, internalModelServiceFactoryProviderArg);
	}

	private static Reader getReader(final InputStream is) throws DMPConverterException {

		final Reader reader;

		try {

			reader = new InputStreamReader(is, "UTF-8");
		} catch (final UnsupportedEncodingException e) {

			try {

				is.close();
			} catch (final IOException e1) {

				TransformationFlow.LOG.error("couldn't close file input stream");
			}

			throw new DMPConverterException("couldn't parse file with UTF-8", e);
		}

		return reader;
	}
}
