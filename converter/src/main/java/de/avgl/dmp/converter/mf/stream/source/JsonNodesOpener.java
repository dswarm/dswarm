package de.avgl.dmp.converter.mf.stream.source;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.util.ResourceUtil;

import de.avgl.dmp.persistence.model.types.Tuple;

public class JsonNodesOpener extends DefaultObjectPipe<String, ObjectReceiver<JsonNode>> {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(JsonNodesOpener.class);

	public JsonNodesOpener(final Iterator<Tuple<String,JsonNode>> iterator) {
	}

	@Override
	public void process(String obj) {
		super.process(obj);    //To change body of overridden methods use File | Settings | File Templates.
	}
}
