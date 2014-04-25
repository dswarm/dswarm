package de.avgl.dmp.converter.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.stream.converter.JsonEncoder;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.culturegraph.mf.stream.source.StringReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.RDFModelReceiver;
import de.avgl.dmp.converter.mf.stream.TripleEncoder;
import de.avgl.dmp.converter.mf.stream.reader.JsonNodeReader;
import de.avgl.dmp.converter.morph.MorphScriptBuilder;
import de.avgl.dmp.converter.pipe.StreamJsonCollapser;
import de.avgl.dmp.converter.pipe.StreamUnflattener;
import de.avgl.dmp.converter.reader.QucosaReader;
import de.avgl.dmp.init.util.DMPStatics;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * Flow that executes a given set of transformations on data of a given data model.
 *
 * @author phorn
 * @author tgaengler
 */
public class TransformationFlow {

	private static final org.apache.log4j.Logger		LOG						= org.apache.log4j.Logger.getLogger(TransformationFlow.class);

	public static final String							DEFAULT_RESOURCE_PATH	= "qucosa_record.xml";

	private final Metamorph								transformer;

	private final String								script;

	private final Optional<DataModel>					outputDataModel;

	private final Provider<InternalModelServiceFactory>	internalModelServiceFactoryProvider;

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

	public String applyRecordDemo(final String record) {

		final StringReader opener = new StringReader();

		return applyDemo(record, opener);
	}

	// public String applyResource(final String resourcePath) {
	//
	// // public String applyResource(final String resourcePath) {
	// //
	// // final ResourceOpener opener = new ResourceOpener();
	// //
	// // return apply(resourcePath, opener);
	// // }
	//
	// String applyResourceDemo(final String resourcePath) {
	//
	// final ResourceOpener opener = new ResourceOpener();
	//
	// return applyDemo(resourcePath, opener);
	// }

	public String applyResourceDemo(final String resourcePath) {

		final ResourceOpener opener = new ResourceOpener();

		return applyDemo(resourcePath, opener);
	}

	public String apply(final Iterator<Tuple<String, JsonNode>> tuples, final ObjectPipe<Iterator<Tuple<String, JsonNode>>, StreamReceiver> opener,
			final boolean writeResultToDatahub) throws DMPConverterException {

		// final String recordDummy = "record";

		final StreamUnflattener unflattener = new StreamUnflattener("", DMPStatics.ATTRIBUTE_DELIMITER);
		final StreamJsonCollapser collapser = new StreamJsonCollapser();
		final TripleEncoder converter = new TripleEncoder(outputDataModel);
		final RDFModelReceiver writer = new RDFModelReceiver();

		opener.setReceiver(transformer).setReceiver(unflattener).setReceiver(collapser).setReceiver(converter).setReceiver(writer);

		opener.process(tuples);
		opener.closeStream();
		// objectReceiver.closeStream();

		// return stringWriter.toString();

		final ImmutableList<RDFModel> rdfModels = writer.getCollection();

		final Model model = ModelFactory.createDefaultModel();
		String recordClassUri = null;

		// transform to FE friendly JSON => or use Model#toJSON() ;)

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		// final ArrayNode result = objectMapper.createArrayNode();
		final Set<String> recordURIs = Sets.newHashSet();

		for (final RDFModel rdfModel : rdfModels) {

			// result.add(rdfModel.toRawJSON());

			if (rdfModel.getModel() == null) {

				continue;
			}

			model.add(rdfModel.getModel());

			if (recordClassUri == null) {

				recordClassUri = rdfModel.getRecordClassURI();
			}

			recordURIs.add(rdfModel.getRecordURIs().iterator().next());
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
		final RDFModel rdfModel = new RDFModel(model, null, recordClassUri);
		rdfModel.setRecordURIs(recordURIs);

		if (writeResultToDatahub) {

			if (outputDataModel.isPresent() && outputDataModel.get().getId() != null) {

				// write result to graph db
				final InternalModelService internalModelService = internalModelServiceFactoryProvider.get().getInternalRDFGraphService();

				try {

					internalModelService.createObject(outputDataModel.get().getId(), rdfModel);
				} catch (final DMPPersistenceException e1) {

					final String message = "couldn't persist the result of the transformation: " + e1.getMessage();

					LOG.error(message);

					throw new DMPConverterException(message, e1);
				}

			} else {

				final String message = "couldn't persist the result of the transformation, because there is no output data model assigned at this task";

				LOG.error(message);

				throw new DMPConverterException(message);
			}
		}

		final String resultString;

		try {

			resultString = objectMapper.writeValueAsString(rdfModel.toJSON());
		} catch (final JsonProcessingException e) {

			final String message = "couldn't convert result into JSON";

			LOG.error(message);

			throw new DMPConverterException(message, e);
		}

		return resultString;
	}

	public String apply(final Iterator<Tuple<String, JsonNode>> tuples, final boolean writeResultToDatahub) throws DMPConverterException {

		final JsonNodeReader opener = new JsonNodeReader();

		return apply(tuples, opener, writeResultToDatahub);
	}

	public String applyDemo(final String object, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		final String recordDummy = "record";

		final QucosaReader reader = new QucosaReader(recordDummy);

		final StreamUnflattener unflattener = new StreamUnflattener(recordDummy);
		final StreamJsonCollapser collapser = new StreamJsonCollapser();

		final JsonEncoder converter = new JsonEncoder();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<>(stringWriter);

		opener.setReceiver(reader).setReceiver(transformer).setReceiver(unflattener).setReceiver(collapser).setReceiver(converter)
				.setReceiver(writer);

		opener.process(object);

		return stringWriter.toString();
	}

	public String applyDemo() {
		return applyResourceDemo(TransformationFlow.DEFAULT_RESOURCE_PATH);
	}

	public static TransformationFlow fromString(final String morphScriptString,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) {
		final java.io.StringReader stringReader = new java.io.StringReader(morphScriptString);
		final Metamorph transformer = new Metamorph(stringReader);

		return new TransformationFlow(transformer, morphScriptString, internalModelServiceFactoryProviderArg);
	}

	public static TransformationFlow fromString(final String morphScriptString, final DataModel outputDataModel,
			final Provider<InternalModelServiceFactory> internalModelServiceFactoryProviderArg) {

		final java.io.StringReader stringReader = new java.io.StringReader(morphScriptString);
		final Metamorph transformer = new Metamorph(stringReader);

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
