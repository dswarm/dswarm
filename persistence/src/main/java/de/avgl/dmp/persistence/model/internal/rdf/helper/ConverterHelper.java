package de.avgl.dmp.persistence.model.internal.rdf.helper;

import com.fasterxml.jackson.databind.node.ObjectNode;


public abstract class ConverterHelper {

	protected final String	property;

	public ConverterHelper(final String property) {

		this.property = property;
	}

	public abstract ObjectNode build(final ObjectNode json);

}
