package de.avgl.dmp.persistence.service.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 */
@Singleton
public class InternalTripleService implements InternalModelService {

	private static final org.apache.log4j.Logger	LOG								= org.apache.log4j.Logger.getLogger(InternalTripleService.class);

	private final Dataset							dataset;
	private final ResourceService					resourceService;
	private final DataModelService					dataModelService;
	private final SchemaService						schemaService;
	private final ClaszService						classService;
	private static final String						OLD_RESOURCE_GRAPH_URI_PATTERN	= "http://data.slub-dresden.de/resource/{resourceid}/configurations/{configurationid}/data";
	private static final String						RESOURCE_GRAPH_URI_PATTERN		= "http://data.slub-dresden.de/datamodel/{datamodelid}/data";

	@Inject
	public InternalTripleService(final ResourceService resourceService, final DataModelService dataModelService, final SchemaService schemaService,
			final ClaszService classService, @Named("TdbPath") final String directory) {
		dataset = TDBFactory.createDataset(directory);
		this.resourceService = resourceService;
		this.dataModelService = dataModelService;
		this.schemaService = schemaService;
		this.classService = classService;
	}

	@Deprecated
	@Override
	public void createObject(final Long id, final Long id1, final Object model) throws DMPPersistenceException {

		// if (dataset == null) {
		//
		// throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot add new model to DB");
		// }
		//
		// if (id == null) {
		//
		// throw new DMPPersistenceException("resource id shouldn't be null");
		// }
		//
		// if (id1 == null) {
		//
		// throw new DMPPersistenceException("configuration id shouldn't be null");
		// }
		//
		// if (model == null) {
		//
		// throw new DMPPersistenceException("model that should be added to DB shouldn't be null");
		// }
		//
		// if (!RDFModel.class.isInstance(model)) {
		//
		// throw new DMPPersistenceException("this service can only process RDF models");
		// }
		//
		// final RDFModel rdfModel = (RDFModel) model;
		//
		// final com.hp.hpl.jena.rdf.model.Model realModel = rdfModel.getModel();
		//
		// if (realModel == null) {
		//
		// throw new DMPPersistenceException("real model that should be added to DB shouldn't be null");
		// }
		//
		// final String resourceGraphURI = InternalTripleService.OLD_RESOURCE_GRAPH_URI_PATTERN.replace("{resourceid}",
		// id.toString()).replace(
		// "{configurationid}", id1.toString());
		//
		// // add resource uri to resource attributes (maybe to resource directly later)
		// final Resource resource = resourceService.getObject(id);
		//
		// if (resource != null) {
		//
		// resource.addAttribute("uri", rdfModel.getResourceURI());
		//
		// resourceService.updateObjectTransactional(resource);
		// } else {
		//
		// InternalTripleService.LOG.debug("couldn't find resource '" + id + "' to add resource uri");
		// }
		//
		// dataset.begin(ReadWrite.WRITE);
		// dataset.addNamedModel(resourceGraphURI, realModel);
		// dataset.commit();
		// dataset.end();

		throw new NotImplementedException(
				"object creation via this method is not implemented yet, please utilise #createObject(dataModelId, model) instead.");
	}

	@Deprecated
	@Override
	public Optional<Map<String, Model>> getObjects(final Long resourceId, final Long configurationId, final Optional<Integer> atMost)
			throws DMPPersistenceException {

		// if (dataset == null) {
		//
		// throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot retrieve model from DB");
		// }
		//
		// if (resourceId == null) {
		//
		// throw new DMPPersistenceException("resource id shouldn't be null");
		// }
		//
		// if (configurationId == null) {
		//
		// throw new DMPPersistenceException("configuration id shouldn't be null");
		// }
		//
		// final String resourceGraphURI = InternalTripleService.OLD_RESOURCE_GRAPH_URI_PATTERN.replace("{resourceid}",
		// resourceId.toString()).replace(
		// "{configurationid}", configurationId.toString());
		//
		// dataset.begin(ReadWrite.READ);
		// final com.hp.hpl.jena.rdf.model.Model model = dataset.getNamedModel(resourceGraphURI);
		// dataset.end();
		//
		// if (model == null) {
		//
		// InternalTripleService.LOG.debug("couldn't find model for resource '" + resourceId + "' and configuration id '" +
		// configurationId
		// + " in dataset");
		//
		// return Optional.absent();
		// }
		//
		// // retrieve resource uri(s) from resource attributes (maybe from resource directly later)
		// final Resource resource = resourceService.getObject(resourceId);
		//
		// if (resource == null) {
		//
		// InternalTripleService.LOG.debug("couldn't find resource '" + resourceId + "' to retrieve resource uri from");
		//
		// throw new DMPPersistenceException("couldn't find resource '" + resourceId + "' to retrieve resource uri from");
		// }
		//
		// // TODO: this needs to be refactored to "retrieve records by record class (from data model -> schema)"
		// final JsonNode valueNode = resource.getAttribute("uri");
		//
		// if (valueNode == null) {
		//
		// InternalTripleService.LOG.debug("couldn't find resource uri in resource '" + resourceId + "'");
		//
		// throw new DMPPersistenceException("couldn't find resource uri in resource '" + resourceId + "'");
		// }
		//
		// final String resourceURI = valueNode.asText();
		//
		// final Model rdfModel = new RDFModel(model, resourceURI);
		//
		// final Map<String, Model> modelMap = Maps.newHashMap();
		//
		// modelMap.put(resourceURI, rdfModel);
		//
		// return Optional.of(modelMap);

		throw new NotImplementedException(
				"object retrieval via this method is not implemented yet, please utilise #getObjects(dataModelId, atMost) instead.");
	}

	@Deprecated
	@Override
	public void deleteObject(final Long resourceId, final Long configurationId) throws DMPPersistenceException {

		// if (dataset == null) {
		//
		// throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot remove model from DB");
		// }
		//
		// if (resourceId == null) {
		//
		// throw new DMPPersistenceException("resource id shouldn't be null");
		// }
		//
		// if (configurationId == null) {
		//
		// throw new DMPPersistenceException("configuration id shouldn't be null");
		// }
		//
		// final String resourceGraphURI = InternalTripleService.OLD_RESOURCE_GRAPH_URI_PATTERN.replace("{resourceid}",
		// resourceId.toString()).replace(
		// "{configurationid}", configurationId.toString());
		//
		// dataset.begin(ReadWrite.WRITE);
		// dataset.removeNamedModel(resourceGraphURI);
		// dataset.commit();
		// dataset.end();

		throw new NotImplementedException(
				"object deletion via this method is not implemented yet, please utilise #deleteObject(dataModelId) instead.");
	}

	@Deprecated
	@Override
	public Optional<Set<String>> getSchema(final Long resourceId, final Long configurationId) {

		throw new NotImplementedException("schema retrieval via this method is not implemented yet, please utilise #getSchema(dataModelId) instead.");
	}

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

		final String resourceGraphURI = InternalTripleService.RESOURCE_GRAPH_URI_PATTERN.replace("{datamodelid}", dataModelId.toString());

		addRecordClass(dataModelId, rdfModel.getRecordClassURI());

		dataset.begin(ReadWrite.WRITE);
		dataset.addNamedModel(resourceGraphURI, realModel);
		dataset.commit();
		dataset.end();

	}

	@Override
	public Optional<Map<String, Model>> getObjects(final Long dataModelId, final Optional<Integer> atMost) throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot retrieve model from DB");
		}

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String resourceGraphURI = InternalTripleService.RESOURCE_GRAPH_URI_PATTERN.replace("{datamodelid}", dataModelId.toString());

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
		final DataModel dataModel = dataModelService.getObject(dataModelId);

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

		final String recordClassUri = recordClass.getId();

		final Set<com.hp.hpl.jena.rdf.model.Resource> recordResources = getRecordResources(recordClassUri, model);

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

	@Override
	public void deleteObject(final Long dataModelId) throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot remove model from DB");
		}

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String resourceGraphURI = InternalTripleService.RESOURCE_GRAPH_URI_PATTERN.replace("{datamodelid}", dataModelId.toString());

		// TODO: delete DataModel object from DB here as well?

		dataset.begin(ReadWrite.WRITE);
		dataset.removeNamedModel(resourceGraphURI);
		dataset.commit();
		dataset.end();

	}

	@Override
	public Optional<Schema> getSchema(final Long dataModelId) throws DMPPersistenceException {

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final DataModel dataModel = dataModelService.getObject(dataModelId);

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

	private void addRecordClass(final Long dataModelId, final String recordClassUri) throws DMPPersistenceException {

		// (try) add record class uri to schema
		final DataModel dataModel = dataModelService.getObject(dataModelId);

		if (dataModel == null) {

			InternalTripleService.LOG.debug("couldn't find data model '" + dataModelId + "' to add the record class to it's schema");

			return;
		}

		final Schema schema;

		if (dataModel.getSchema() != null) {

			schema = dataModel.getSchema();
		} else {

			// create new schema
			schema = schemaService.createObject();
			dataModel.setSchema(schema);
		}

		final Clasz recordClass;

		if (schema.getRecordClass() != null) {

			if (schema.getRecordClass().getId().equals(recordClassUri)) {

				// nothing to do, record class is already set

				return;
			}

			recordClass = schema.getRecordClass();
		} else {

			// create new class
			recordClass = classService.createObject(recordClassUri);
			schema.setRecordClass(recordClass);
		}

		dataModelService.updateObjectTransactional(dataModel);
	}

	private Set<com.hp.hpl.jena.rdf.model.Resource> getRecordResources(final String recordClassURI, final com.hp.hpl.jena.rdf.model.Model model) {

		LOG.debug("start processing all record resources SPARQL query");

		final String allRecordResourcesQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "SELECT ?resource\n"
				+ "WHERE { \n" + "        ?resource rdf:type <" + recordClassURI + "> . \n" + "     }";

		final Query allRecordResourcesQuery = QueryFactory.create(allRecordResourcesQueryString);
		final QueryExecution allRecordResourcesQueryExec = QueryExecutionFactory.create(allRecordResourcesQuery, model);

		final ResultSet realResultSet = allRecordResourcesQueryExec.execSelect();

		LOG.debug("end processing all record resources SPARQL query");

		if (realResultSet == null || !realResultSet.hasNext()) {

			LOG.error("all record resources result set was 'null' or empty");

			return null;
		}

		LOG.debug("start copying all record resource SPARQL query result set");

		final ResultSetMem results = new ResultSetMem(realResultSet);

		allRecordResourcesQueryExec.close();

		LOG.debug("end copying all record resources SPARQL query result set");

		// final ResultSetMem results2 = new ResultSetMem(results, true);

		// ResultSetFormatter.out(System.out, results2, allTagsQuery);

		final Set<com.hp.hpl.jena.rdf.model.Resource> recordResources = Sets.newHashSet();

		while (results.hasNext()) {

			final QuerySolution querySolution = results.next();

			if (null != querySolution) {

				final com.hp.hpl.jena.rdf.model.Resource recordResource = querySolution.getResource("resource");

				if (null != recordResource) {

					recordResources.add(recordResource);
				}
			}
		}

		return recordResources;
	}
}
