package de.avgl.dmp.persistence.util;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;

import de.avgl.dmp.persistence.service.internal.triple.InternalTripleService;

/**
 * @author tgaengler
 */
public final class RDFUtil {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(InternalTripleService.class);

	/**
	 * Gets all resources for the given record class identifier in the given Jena model.
	 * 
	 * @param recordClassURI the record class identifier
	 * @param model the Jena model
	 * @return
	 */
	public static Set<com.hp.hpl.jena.rdf.model.Resource> getRecordResources(final String recordClassURI, final com.hp.hpl.jena.rdf.model.Model model) {

		if (recordClassURI == null || model == null) {

			RDFUtil.LOG.debug("record class URI or model is null");

			return null;
		}

		RDFUtil.LOG.debug("start processing all record resources SPARQL query");

		final String allRecordResourcesQueryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "SELECT ?resource\n"
				+ "WHERE { \n" + "        ?resource rdf:type <" + recordClassURI + "> . \n" + "     }";

		final Query allRecordResourcesQuery = QueryFactory.create(allRecordResourcesQueryString);
		final QueryExecution allRecordResourcesQueryExec = QueryExecutionFactory.create(allRecordResourcesQuery, model);

		final ResultSet realResultSet = allRecordResourcesQueryExec.execSelect();

		RDFUtil.LOG.debug("end processing all record resources SPARQL query");

		if (realResultSet == null || !realResultSet.hasNext()) {

			RDFUtil.LOG.error("all record resources result set was 'null' or empty");

			return null;
		}

		RDFUtil.LOG.debug("start copying all record resource SPARQL query result set");

		final ResultSetMem results = new ResultSetMem(realResultSet);

		allRecordResourcesQueryExec.close();

		RDFUtil.LOG.debug("end copying all record resources SPARQL query result set");

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
