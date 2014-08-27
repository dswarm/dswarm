package org.dswarm.persistence.model.schema.utils;

import java.util.LinkedList;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.utils.BasicDMPJPAObjectUtils;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 */
public final class SchemaUtils extends BasicDMPJPAObjectUtils<Schema> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaUtils.class);

	public static String determineRelativeURIPart(final String uri) {

		final String lastPartDelimiter;

		if (uri.lastIndexOf("#") > 0) {

			lastPartDelimiter = "#";
		} else if (uri.lastIndexOf("/") > 0) {

			lastPartDelimiter = "/";
		} else {

			lastPartDelimiter = null;
		}

		final String relativeURIPart;

		if (lastPartDelimiter != null) {

			relativeURIPart = uri.substring(uri.lastIndexOf(lastPartDelimiter) + 1, uri.length());
		} else {

			relativeURIPart = uri;
		}

		return relativeURIPart;
	}

	public static String determineSchemaNamespaceURI(final Long schemaId) {

		return "http://data.slub-dresden.de/schemas/" + schemaId + "/";
	}

	public static String mintAttributeURI(final String attributeName, final String namespaceURI) {

		final String attributeNameURLEncoded = UrlEscapers.urlFormParameterEscaper().escape(attributeName);

		return namespaceURI + attributeNameURLEncoded;
	}

	public static boolean addRecordClass(final Schema schema, final String recordClassUri, final Provider<ClaszService> classServiceProvider)
			throws DMPPersistenceException {

		final Clasz recordClass;

		if (schema.getRecordClass() != null) {

			if (schema.getRecordClass().getUri().equals(recordClassUri)) {

				// nothing to do, record class is already set

				return false;
			}
		} else {

			// create new class
			final ProxyClasz proxyRecordClass = classServiceProvider.get().createOrGetObjectTransactional(recordClassUri);

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

		return true;
	}

	public static boolean addAttributePaths(final Schema schema, final Set<AttributePathHelper> attributePathHelpers,
			final Provider<AttributePathService> attributePathServiceProvider, final Provider<AttributeService> attributeServiceProvider)
			throws DMPPersistenceException {

		if (attributePathHelpers == null) {

			SchemaUtils.LOG.debug("couldn't determine attribute paths for schema '" + schema.getId() + "'");

			return false;
		}

		if (attributePathHelpers.isEmpty()) {

			SchemaUtils.LOG.debug("there are no attribute paths for schema '" + schema.getId() + "'");
		}

		if (schema.getId() == 3) {

			// mabxml schema is already there

			return false;
		}

		for (final AttributePathHelper attributePathHelper : attributePathHelpers) {

			final LinkedList<Attribute> attributes = Lists.newLinkedList();

			final LinkedList<String> attributePathFromHelper = attributePathHelper.getAttributePath();

			if (attributePathFromHelper.isEmpty()) {

				SchemaUtils.LOG.debug("there are no attributes for this attribute path for schema '" + schema.getId() + "'");
			}

			for (final String attributeString : attributePathFromHelper) {

				final ProxyAttribute proxyAttribute = attributeServiceProvider.get().createOrGetObjectTransactional(attributeString);

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

			final ProxyAttributePath proxyAttributePath = attributePathServiceProvider.get().createOrGetObjectTransactional(attributes);

			if (proxyAttributePath == null) {

				throw new DMPPersistenceException("couldn't create or retrieve attribute path");
			}

			final AttributePath attributePath = proxyAttributePath.getObject();

			if (attributePath == null) {

				throw new DMPPersistenceException("couldn't create or retrieve attribute path");
			}

			schema.addAttributePath(attributePath);
		}

		return true;
	}

	private Schema updateSchema(final Schema schema, final Provider<SchemaService> schemaServiceProvider) throws DMPPersistenceException {

		final ProxySchema proxyUpdatedSchema = schemaServiceProvider.get().updateObjectTransactional(schema);

		if (proxyUpdatedSchema == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedSchema.getObject();
	}

}
