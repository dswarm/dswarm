package de.avgl.dmp.persistence.services.impl;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.InternalService;
import de.avgl.dmp.persistence.services.ResourceService;

@Singleton
public class InternalTripleService implements InternalService {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(InternalTripleService.class);

	private final Dataset							dataset;
	private final ResourceService					resourceService;
	private static final String						resourceGraphURIPattern	= "http://data.slub-dresden.de/resource/{resourceid}/configurations/{configurationid}/data";

	@Inject
	public InternalTripleService(final ResourceService resourceService) {

		String directory = "target/h2";
		dataset = TDBFactory.createDataset(directory);
		this.resourceService = resourceService;
	}

	@Override
	public void createObject(final Long id, final Long id1, Object model) throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot add new model to DB");
		}

		if (id == null) {

			throw new DMPPersistenceException("resource id shouldn't be null");
		}

		if (id1 == null) {

			throw new DMPPersistenceException("configuration id shouldn't be null");
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

		final String resourceGraphURI = resourceGraphURIPattern.replace("{resourceid}", id.toString()).replace("[configurationid}", id1.toString());

		// add resource uri to resource attributes (maybe to resource directly later)
		final Resource resource = resourceService.getObject(id);

		if (resource != null) {

			resource.addAttribute("uri", rdfModel.getResourceURI());

			resourceService.updateObjectTransactional(resource);
		} else {

			LOG.debug("couldn't find resource '" + id + "' to add resource uri");
		}

		dataset.begin(ReadWrite.WRITE);
		dataset.addNamedModel(resourceGraphURI, realModel);
		dataset.commit();
		dataset.end();
	}

	@Override
	public Optional<Map<String, Model>> getObjects(final Long id, final Long configurationId, final Optional<Integer> atMost)
			throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot add new model to DB");
		}

		if (id == null) {

			throw new DMPPersistenceException("resource id shouldn't be null");
		}

		if (configurationId == null) {

			throw new DMPPersistenceException("configuration id shouldn't be null");
		}

		final String resourceGraphURI = resourceGraphURIPattern.replace("{resourceid}", id.toString()).replace("[configurationid}",
				configurationId.toString());

		dataset.begin(ReadWrite.READ);
		final com.hp.hpl.jena.rdf.model.Model model = dataset.getNamedModel(resourceGraphURI);
		dataset.end();

		if (model == null) {

			LOG.debug("couldn't find model for resource '" + id + "' and configuration id '" + configurationId + " in dataset");

			return Optional.absent();
		}

		// retrieve resource uri from resource attributes (maybe from resource directly later)
		final Resource resource = resourceService.getObject(id);

		if (resource == null) {

			LOG.debug("couldn't find resource '" + id + "' to retrieve resource uri from");

			throw new DMPPersistenceException("couldn't find resource '" + id + "' to retrieve resource uri from");
		}

		final JsonNode valueNode = resource.getAttribute("uri");

		if (valueNode == null) {

			LOG.debug("couldn't find resource uri in resource '" + id + "'");

			throw new DMPPersistenceException("couldn't find resource uri in resource '" + id + "'");
		}

		final String resourceURI = valueNode.toString();

		final Model rdfModel = new RDFModel(model, resourceURI);

		final Map<String, Model> modelMap = Maps.newHashMap();

		modelMap.put(resourceURI, rdfModel);

		return Optional.of(modelMap);
	}

	@Override
	public void deleteObject(final Long id, final Long configurationId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Set<String>> getSchema(final Long id, final Long configurationId) {
		// TODO Auto-generated method stub
		return null;
	}
}
