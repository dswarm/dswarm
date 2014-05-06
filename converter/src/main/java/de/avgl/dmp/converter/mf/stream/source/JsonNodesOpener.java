package de.avgl.dmp.converter.mf.stream.source;

import java.util.Iterator;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;

import com.fasterxml.jackson.databind.JsonNode;

import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * @author phorn
 */
public class JsonNodesOpener extends DefaultObjectPipe<String, ObjectReceiver<JsonNode>> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(JsonNodesOpener.class);

	public JsonNodesOpener(final Iterator<Tuple<String, JsonNode>> iterator) {
	}

	@Override
	public void process(final String obj) {
		super.process(obj); // To change body of overridden methods use File | Settings | File Templates.
	}
}
