package de.avgl.dmp.persistence.service.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.service.InternalService;
import de.avgl.dmp.persistence.service.resource.ResourceService;

/**
 * @author tgaengler
 */
@Singleton
public class InternalTripleService implements InternalService {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(InternalTripleService.class);

	private final Dataset							dataset;
	private final ResourceService					resourceService;
	private static final String						resourceGraphURIPattern	= "http://data.slub-dresden.de/resource/{resourceid}/configurations/{configurationid}/data";

	@Inject
	public InternalTripleService(final ResourceService resourceService, @Named("TdbPath") final String directory) {
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

		final String resourceGraphURI = resourceGraphURIPattern.replace("{resourceid}", id.toString()).replace("{configurationid}", id1.toString());

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
	public Optional<Map<String, Model>> getObjects(final Long resourceId, final Long configurationId, final Optional<Integer> atMost)
			throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot retrieve model from DB");
		}

		if (resourceId == null) {

			throw new DMPPersistenceException("resource id shouldn't be null");
		}

		if (configurationId == null) {

			throw new DMPPersistenceException("configuration id shouldn't be null");
		}

		final String resourceGraphURI = resourceGraphURIPattern.replace("{resourceid}", resourceId.toString()).replace("{configurationid}",
				configurationId.toString());

		dataset.begin(ReadWrite.READ);
		final com.hp.hpl.jena.rdf.model.Model model = dataset.getNamedModel(resourceGraphURI);
		dataset.end();

		if (model == null) {

			LOG.debug("couldn't find model for resource '" + resourceId + "' and configuration id '" + configurationId + " in dataset");

			return Optional.absent();
		}

		// retrieve resource uri(s) from resource attributes (maybe from resource directly later)
		final Resource resource = resourceService.getObject(resourceId);

		if (resource == null) {

			LOG.debug("couldn't find resource '" + resourceId + "' to retrieve resource uri from");

			throw new DMPPersistenceException("couldn't find resource '" + resourceId + "' to retrieve resource uri from");
		}

		final JsonNode valueNode = resource.getAttribute("uri");

		if (valueNode == null) {

			LOG.debug("couldn't find resource uri in resource '" + resourceId + "'");

			throw new DMPPersistenceException("couldn't find resource uri in resource '" + resourceId + "'");
		}

		final String resourceURI = valueNode.asText();

		final Model rdfModel = new RDFModel(model, resourceURI);

		final Map<String, Model> modelMap = Maps.newHashMap();

		modelMap.put(resourceURI, rdfModel);

		return Optional.of(modelMap);
	}

	@Override
	public void deleteObject(final Long resourceId, final Long configurationId) throws DMPPersistenceException {

		if (dataset == null) {

			throw new DMPPersistenceException("couldn't establish connection to DB, i.e., cannot remove model from DB");
		}

		if (resourceId == null) {

			throw new DMPPersistenceException("resource id shouldn't be null");
		}

		if (configurationId == null) {

			throw new DMPPersistenceException("configuration id shouldn't be null");
		}

		final String resourceGraphURI = resourceGraphURIPattern.replace("{resourceid}", resourceId.toString()).replace("{configurationid}",
				configurationId.toString());

		dataset.begin(ReadWrite.WRITE);
		dataset.removeNamedModel(resourceGraphURI);
		dataset.commit();
		dataset.end();
	}

	@Override
	public Optional<Set<String>> getSchema(final Long resourceId, final Long configurationId) {

		throw new NotImplementedException("schema storage is not implemented yet");
	}
}
