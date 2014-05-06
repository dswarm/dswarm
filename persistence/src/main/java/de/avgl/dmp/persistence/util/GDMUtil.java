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
import de.avgl.dmp.graph.json.Node;
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

		final Set<Resource> recordResources = Sets.newLinkedHashSet();

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

	/**
	 * Gets resource node for the given resource identifier in the given record resource.
	 * 
	 * @param resourceURI the resource identifier
	 * @param model the GDM model
	 * @return
	 */
	public static ResourceNode getResourceNode(final String resourceURI, final Resource recordResource) {

		if (resourceURI == null || recordResource == null) {

			GDMUtil.LOG.debug("resource URI or record resource is null");

			return null;
		}

		final Collection<Statement> statements = recordResource.getStatements();

		if (statements == null || statements.isEmpty()) {

			GDMUtil.LOG.debug("record resource contains no statements");

			return null;
		}

		for (final Statement statement : statements) {

			final Node subjectNode = statement.getSubject();

			if (subjectNode == null) {

				// this should never be the case

				continue;
			}

			if (!(subjectNode instanceof ResourceNode)) {

				// only resource nodes are relevant in this game

				continue;
			}

			final ResourceNode subjectResourceNode = (ResourceNode) subjectNode;

			if (resourceURI.equals(subjectResourceNode.getUri())) {

				// match !!!

				return subjectResourceNode;
			}
		}

		return null;
	}

	/**
	 * Gets all statements for the given resource node in the given record resource.
	 * 
	 * @param resourceURI the resource identifier
	 * @param model the GDM model
	 * @return
	 */
	public static Set<Statement> getResourceStatement(final Node resourceNode, final Resource recordResource) {

		if (resourceNode == null || recordResource == null) {

			GDMUtil.LOG.debug("resource Node or record resource is null");

			return null;
		}

		if (resourceNode instanceof ResourceNode) {

			final ResourceNode castedResourceNode = (ResourceNode) resourceNode;
			final String resourceURI = castedResourceNode.getUri();

			return GDMUtil.getResourceStatement(resourceURI, recordResource);
		}

		final Long resourceId = resourceNode.getId();

		return GDMUtil.getResourceStatement(resourceId, recordResource);
	}

	/**
	 * Gets all statements for the given resource identifier in the given record resource.
	 * 
	 * @param resourceURI the resource identifier
	 * @param model the GDM model
	 * @return
	 */
	public static Set<Statement> getResourceStatement(final String resourceURI, final Resource recordResource) {

		if (resourceURI == null || recordResource == null) {

			GDMUtil.LOG.debug("resource URI or record resource is null");

			return null;
		}

		final Collection<Statement> statements = recordResource.getStatements();

		if (statements == null || statements.isEmpty()) {

			GDMUtil.LOG.debug("record resource contains no statements");

			return null;
		}

		final Set<Statement> resourceStatements = Sets.newLinkedHashSet();

		for (final Statement statement : statements) {

			final Node subjectNode = statement.getSubject();

			if (!(subjectNode instanceof ResourceNode)) {

				// only resource nodes are relevant here ..

				continue;
			}

			final ResourceNode subjectResourceNode = (ResourceNode) subjectNode;

			if (!resourceURI.equals(subjectResourceNode.getUri())) {

				// only resource nodes that matches the resource uri are relevant here ..

				continue;
			}

			resourceStatements.add(statement);
		}

		return resourceStatements;
	}

	/**
	 * Gets all statements for the given resource identifier in the given record resource.
	 * 
	 * @param resourceId the resource identifier
	 * @param model the GDM model
	 * @return
	 */
	public static Set<Statement> getResourceStatement(final Long resourceId, final Resource recordResource) {

		if (resourceId == null || recordResource == null) {

			GDMUtil.LOG.debug("resource id or record resource is null");

			return null;
		}

		final Collection<Statement> statements = recordResource.getStatements();

		if (statements == null || statements.isEmpty()) {

			GDMUtil.LOG.debug("record resource contains no statements");

			return null;
		}

		final Set<Statement> resourceStatements = Sets.newLinkedHashSet();

		for (final Statement statement : statements) {

			final Node subjectNode = statement.getSubject();

			if (subjectNode.getId() == null) {

				// only nodes with id are relevant here ..

				continue;
			}

			if (!resourceId.equals(subjectNode.getId())) {

				// only nodes that matches the resource id are relevant here ..

				continue;
			}

			resourceStatements.add(statement);
		}

		return resourceStatements;
	}
}
