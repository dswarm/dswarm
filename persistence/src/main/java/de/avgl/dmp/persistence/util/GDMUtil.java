package de.avgl.dmp.persistence.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;

import ch.lambdaj.Lambda;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.graph.json.Model;
import de.avgl.dmp.graph.json.NodeType;
import de.avgl.dmp.graph.json.Resource;
import de.avgl.dmp.graph.json.ResourceNode;
import de.avgl.dmp.graph.json.Statement;

/**
 * @author tgaengler
 */
public final class GDMUtil {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(GDMUtil.class);

	/**
	 * Gets all resources for the given record class identifier in the given GDM model.
	 * 
	 * @param recordClassURI the record class identifier
	 * @param model the GDM model
	 * @return
	 */
	public static Set<Resource> getRecordResources(final String recordClassURI, final Model model) {

		if (recordClassURI == null || model == null) {

			GDMUtil.LOG.debug("record class URI or model is null");

			return null;
		}

		final Collection<Resource> resources = model.getResources();

		if (resources == null || resources.isEmpty()) {

			GDMUtil.LOG.debug("model contains no resources");

			return null;
		}

		final Set<Resource> recordResources = Sets.newHashSet();

		for (final Resource resource : resources) {

			final Set<Statement> statements = resource.getStatements();

			// determine all statements that have a resource as subject
			final List<Statement> resourceStatements = Lambda.filter(
					Lambda.having(Lambda.on(Statement.class).getSubject().getType(), Matchers.equalTo(NodeType.Resource)), statements);

			if (resourceStatements == null || resourceStatements.isEmpty()) {

				continue;
			}

			// determine all statements that have this resource as subject
			final List<Statement> thisResourceStatements = Lists.newArrayList();

			for (final Statement statement : resourceStatements) {

				final ResourceNode subject = (ResourceNode) statement.getSubject();

				if (resource.getUri().equals(subject.getUri())) {

					thisResourceStatements.add(statement);
				}
			}

			if (thisResourceStatements == null || thisResourceStatements.isEmpty()) {

				continue;
			}

			// determine all statements that are type statements
			final List<Statement> resourceTypeStatements = Lambda.filter(
					Lambda.having(Lambda.on(Statement.class).getPredicate().getUri(), Matchers.equalTo(RDF.type.getURI())), thisResourceStatements);

			if (resourceTypeStatements == null || resourceTypeStatements.isEmpty()) {

				continue;
			}

			// determine all statements whose types are resources
			final List<Statement> typeStatements = Lambda.filter(
					Lambda.having(Lambda.on(Statement.class).getObject().getType(), Matchers.equalTo(NodeType.Resource)), resourceTypeStatements);

			if (typeStatements == null || typeStatements.isEmpty()) {

				continue;
			}

			// determine all statements whose type are the record type
			final List<Statement> recordTypeStatements = Lists.newArrayList();

			for (final Statement statement : typeStatements) {

				final ResourceNode object = (ResourceNode) statement.getObject();

				if (recordClassURI.equals(object.getUri())) {

					recordTypeStatements.add(statement);
				}
			}

			if (recordTypeStatements == null || recordTypeStatements.isEmpty()) {

				continue;
			}

			recordResources.add(resource);
		}

		return recordResources;
	}
}
