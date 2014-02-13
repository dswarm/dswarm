package de.avgl.dmp.persistence.service.impl;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.internal.rdf.helper.AttributePathHelper;
import de.avgl.dmp.persistence.model.proxy.RetrievalType;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyDataModel;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyClasz;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.model.schema.utils.SchemaUtils;
import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.util.RDFUtil;

/**
 * A internal model service implementation for RDF triples.<br/>
 * Currently, the Jena TDB triple store is utilised.
 * 
 * @author tgaengler
 */
@Singleton
public class InternalTripleService implements InternalModelService {

	private static final org.apache.log4j.Logger	LOG								= org.apache.log4j.Logger.getLogger(InternalTripleService.class);

	/**
	 * The Jena TDB triple store.
	 */
	private final Dataset							dataset;

	/**
	 * The data model persistence service.
	 */
	private final Provider<DataModelService>		dataModelService;

	/**
	 * The schema persistence service.
	 */
	private final Provider<SchemaService>			schemaService;

	/**
	 * The class persistence service.
	 */
	private final Provider<ClaszService>			classService;

	private final Provider<AttributePathService>	attributePathService;

	private final Provider<AttributeService>		attributeService;

	/**
	 * The data model graph URI pattern
	 */
	private static final String						DATA_MODEL_GRAPH_URI_PATTERN	= "http://data.slub-dresden.de/datamodel/{datamodelid}/data";

	/**
	 * Creates a new internal triple service with the given data model persistence service, schema persistence service, class
	 * persistence service and the directory of the Jena TDB triple store.
	 * 
	 * @param dataModelService the data model persistence service
	 * @param schemaService the schema persistence service
	 * @param classService the class persistence service
	 * @param directory the directory of the Jena TDB triple store
	 */
	@Inject
	public InternalTripleService(final Provider<DataModelService> dataModelService, final Provider<SchemaService> schemaService,
			final Provider<ClaszService> classService, final Provider<AttributePathService> attributePathService,
			final Provider<AttributeService> attributeService, @Named("TdbPath") final String directory) {
		dataset = TDBFactory.createDataset(directory);
		this.dataModelService = dataModelService;
		this.schemaService = schemaService;
		this.classService = classService;
		this.attributePathService = attributePathService;
		this.attributeService = attributeService;
	}

	@Deprecated
	@Override
	public Optional<Set<String>> getSchema(final Long resourceId, final Long configurationId) {

		throw new NotImplementedException("schema retrieval via this method is not implemented yet, please utilise #getSchema(dataModelId) instead.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createObject(final Long dataModelId, final Object model) throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot add new model to DB");
		}

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		if (model == null) {

			throw new DMPPersistenceException("model that should be added to DB shouldn't be null");
		}

		if (!RDFModel.class.isInstance(model)) {

			throw new DMPPersistenceException("this service can only process RDF models");
		}

		final RDFModel rdfModel = (RDFModel) model;

		final com.hp.hpl.jena.rdf.model.Model realModel = rdfModel.getModel();

		if (realModel == null) {

			throw new DMPPersistenceException("real model that should be added to DB shouldn't be null");
		}

		final String resourceGraphURI = InternalTripleService.DATA_MODEL_GRAPH_URI_PATTERN.replace("{datamodelid}", dataModelId.toString());

		final DataModel dataModel = addRecordClass(dataModelId, rdfModel.getRecordClassURI());

		final DataModel finalDataModel;

		if (dataModel != null) {

			finalDataModel = dataModel;
		} else {

			finalDataModel = getDataModel(dataModelId);
		}

		if (dataModel.getSchema() != null) {

			if (dataModel.getSchema().getRecordClass() != null) {

				final String recordClassURI = dataModel.getSchema().getRecordClass().getUri();

				final Set<com.hp.hpl.jena.rdf.model.Resource> recordResources = RDFUtil.getRecordResources(recordClassURI, realModel);

				if (recordResources != null) {

					final Set<String> recordURIs = Sets.newHashSet();

					for (final com.hp.hpl.jena.rdf.model.Resource recordResource : recordResources) {

						recordURIs.add(recordResource.getURI());
					}

					rdfModel.setRecordURIs(recordURIs);
				}
			}
		}

		addAttributePaths(finalDataModel, rdfModel.getAttributePaths());

		dataset.begin(ReadWrite.WRITE);
		dataset.addNamedModel(resourceGraphURI, realModel);
		dataset.commit();
		dataset.end();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Map<String, Model>> getObjects(final Long dataModelId, final Optional<Integer> atMost) throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot retrieve model from DB");
		}

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String resourceGraphURI = InternalTripleService.DATA_MODEL_GRAPH_URI_PATTERN.replace("{datamodelid}", dataModelId.toString());

		dataset.begin(ReadWrite.READ);
		final com.hp.hpl.jena.rdf.model.Model model = dataset.getNamedModel(resourceGraphURI);
		dataset.end();

		if (model == null) {

			InternalTripleService.LOG.debug("couldn't find model for data model '" + dataModelId + "' in dataset");

			return Optional.absent();
		}

		if (model.isEmpty()) {

			InternalTripleService.LOG.debug("model is empty for data model '" + dataModelId + "' in dataset");

			return Optional.absent();
		}

		// retrieve record class uri from data model schema
		final DataModel dataModel = dataModelService.get().getObject(dataModelId);

		if (dataModel == null) {

			InternalTripleService.LOG.debug("couldn't find data model '" + dataModelId + "' to retrieve record class from");

			throw new DMPPersistenceException("couldn't find data model '" + dataModelId + "' to retrieve record class from");
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalTripleService.LOG.debug("couldn't find schema in data model '" + dataModelId + "'");

			throw new DMPPersistenceException("couldn't find schema in data model '" + dataModelId + "'");
		}

		final Clasz recordClass = schema.getRecordClass();

		if (recordClass == null) {

			InternalTripleService.LOG.debug("couldn't find record class in schema '" + schema.getId() + "' of data model '" + dataModelId + "'");

			throw new DMPPersistenceException("couldn't find record class in schema '" + schema.getId() + "' of data model '" + dataModelId + "'");
		}

		final String recordClassUri = recordClass.getUri();

		final Set<com.hp.hpl.jena.rdf.model.Resource> recordResources = RDFUtil.getRecordResources(recordClassUri, model);

		if (recordResources == null || recordResources.isEmpty()) {

			InternalTripleService.LOG.debug("couldn't find records for record class'" + recordClassUri + "' in data model '" + dataModelId + "'");

			throw new DMPPersistenceException("couldn't find records for record class'" + recordClassUri + "' in data model '" + dataModelId + "'");
		}

		final Map<String, Model> modelMap = Maps.newHashMap();

		int i = 0;

		for (final com.hp.hpl.jena.rdf.model.Resource recordResource : recordResources) {

			if (atMost.isPresent()) {

				if (i >= atMost.get()) {

					break;
				}
			}

			// TODO: maybe extract only the related part of the model for the record (however, afaik, recordResource.getModel()
			// will return an empty model for now, or?)
			final Model rdfModel = new RDFModel(model, recordResource.getURI());

			modelMap.put(recordResource.getURI(), rdfModel);

			i++;
		}

		return Optional.of(modelMap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteObject(final Long dataModelId) throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot remove model from DB");
		}

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String resourceGraphURI = InternalTripleService.DATA_MODEL_GRAPH_URI_PATTERN.replace("{datamodelid}", dataModelId.toString());

		// TODO: delete DataModel object from DB here as well?

		dataset.begin(ReadWrite.WRITE);
		dataset.removeNamedModel(resourceGraphURI);
		dataset.commit();
		dataset.end();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Schema> getSchema(final Long dataModelId) throws DMPPersistenceException {

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final DataModel dataModel = dataModelService.get().getObject(dataModelId);

		if (dataModel == null) {

			InternalTripleService.LOG.debug("couldn't find data model '" + dataModelId + "' to retrieve it's schema");

			throw new DMPPersistenceException("couldn't find data model '" + dataModelId + "' to retrieve it's schema");
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalTripleService.LOG.debug("couldn't find schema in data model '" + dataModelId + "'");

			return Optional.absent();
		}

		return Optional.of(schema);
	}

	/**
	 * Adds the record class to the schema of the data model.
	 * 
	 * @param dataModelId the identifier of the data model
	 * @param recordClassUri the identifier of the record class
	 * @throws DMPPersistenceException
	 */
	private DataModel addRecordClass(final Long dataModelId, final String recordClassUri) throws DMPPersistenceException {

		// (try) add record class uri to schema
		final DataModel dataModel = getSchemaInternal(dataModelId);
		final Schema schema = dataModel.getSchema();

		final Clasz recordClass;

		if (schema.getRecordClass() != null) {

			if (schema.getRecordClass().getId().equals(recordClassUri)) {

				// nothing to do, record class is already set

				return dataModel;
			}

			recordClass = schema.getRecordClass();
		} else {

			// create new class
			final ProxyClasz proxyRecordClass = classService.get().createOrGetObjectTransactional(recordClassUri);

			if (proxyRecordClass == null) {

				throw new DMPPersistenceException("couldn't create or retrieve record class");
			}

			recordClass = proxyRecordClass.getObject();

			if (proxyRecordClass.getType().equals(RetrievalType.CREATED)) {

				if (recordClass == null) {

					throw new DMPPersistenceException("couldn't create new record class");
				}

				final String recordClassName = SchemaUtils.determineRelativeURIPart(recordClassUri);

				recordClass.setName(recordClassName);
			}

			schema.setRecordClass(recordClass);
		}

		final ProxyDataModel proxyUpdatedDataModel = dataModelService.get().updateObjectTransactional(dataModel);

		if (proxyUpdatedDataModel == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedDataModel.getObject();
	}

	private DataModel addAttributePaths(final DataModel dataModel, final Set<AttributePathHelper> attributePathHelpers)
			throws DMPPersistenceException {

		if (attributePathHelpers == null) {

			InternalTripleService.LOG.debug("couldn't datermine attribute paths from data model '" + dataModel.getId() + "'");

			return dataModel;
		}

		for (final AttributePathHelper attributePathHelper : attributePathHelpers) {

			final LinkedList<Attribute> attributes = Lists.newLinkedList();

			for (final String attributeString : attributePathHelper.getAttributePath()) {

				final ProxyAttribute proxyAttribute = attributeService.get().createOrGetObjectTransactional(attributeString);

				if (proxyAttribute == null) {

					throw new DMPPersistenceException("couldn't create or retrieve attribute");
				}

				final Attribute attribute = proxyAttribute.getObject();

				if (attribute == null) {

					throw new DMPPersistenceException("couldn't create or retrieve attribute");
				}

				attributes.add(attribute);

				final String attributeName = SchemaUtils.determineRelativeURIPart(attributeString);

				attribute.setName(attributeName);
			}

			final ProxyAttributePath proxyAttributePath = attributePathService.get().createOrGetObject(attributes);

			if (proxyAttributePath == null) {

				throw new DMPPersistenceException("couldn't create or retrieve attribute path");
			}

			final AttributePath attributePath = proxyAttributePath.getObject();

			if (attributePath == null) {

				throw new DMPPersistenceException("couldn't create or retrieve attribute path");
			}

			dataModel.getSchema().addAttributePath(attributePath);
		}

		final ProxyDataModel proxyUpdatedDataModel = dataModelService.get().updateObjectTransactional(dataModel);

		if (proxyUpdatedDataModel == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedDataModel.getObject();
	}

	private DataModel getSchemaInternal(final Long dataModelId) throws DMPPersistenceException {

		final DataModel dataModel = getDataModel(dataModelId);

		final Schema schema;

		if (dataModel.getSchema() != null) {

			schema = dataModel.getSchema();
		} else {

			// create new schema
			final ProxySchema proxySchema = schemaService.get().createObjectTransactional();

			if (proxySchema != null) {

				schema = proxySchema.getObject();
			} else {

				schema = null;
			}

			dataModel.setSchema(schema);
		}

		return dataModel;
	}

	private DataModel getDataModel(final Long dataModelId) {

		final DataModel dataModel = dataModelService.get().getObject(dataModelId);

		if (dataModel == null) {

			InternalTripleService.LOG.debug("couldn't find data model '" + dataModelId + "'");

			return null;
		}

		return dataModel;
	}

	
}
