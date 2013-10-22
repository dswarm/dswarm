package de.avgl.dmp.persistence.services.impl;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.inject.Singleton;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.MemoryDBInputModel;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.services.InternalService;

@Singleton
public class InternalTripleService implements InternalService {

	private final Dataset	dataset;
	private static final String resourceGraphURIPattern = "http://data.slub-dresden.de/resource/{resourceid}/configurations/{configurationid}/data";

	public InternalTripleService() {

		String directory = "target/tdb/dmpdb";
		dataset = TDBFactory.createDataset(directory);
	}

	@Override
	public void createObject(final Long id, final Long id1, Object model) throws DMPPersistenceException {
		
		if(dataset == null) {
			
			throw new DMPPersistenceException("coudln't establish connection to DB, i.e., cannot add new model to DB");
		}

		if (model == null) {

			throw new DMPPersistenceException("model that should be added to DB shouldn't be null");
		}

		if (!MemoryDBInputModel.class.isInstance(model)) {

			throw new DMPPersistenceException("this service can only process RDF models");
		}

		final RDFModel rdfModel = (RDFModel) model;
		
		final com.hp.hpl.jena.rdf.model.Model realModel = rdfModel.getModel();
		
		if(realModel == null) {
			
			throw new DMPPersistenceException("real model that should be added to DB shouldn't be null");
		}
		
		final String resourceGraphURI = resourceGraphURIPattern.replace("{resourceid}", id.toString()).replace("[configurationid}", id1.toString());
		
		dataset.addNamedModel(resourceGraphURI, realModel);
	}

	@Override
	public Optional<Map<String, Model>> getObjects(Long id, Long configurationId, Optional<Integer> atMost) {
		
		
		// TODO Auto-generated method stub
		return null;
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
