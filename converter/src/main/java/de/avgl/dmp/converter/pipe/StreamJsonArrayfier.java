package de.avgl.dmp.converter.pipe;

import com.google.common.collect.LinkedListMultimap;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;

import java.util.Collection;
import java.util.Map;

public class StreamJsonArrayfier extends DefaultStreamPipe<StreamReceiver> {

	public static final String ARRAY_MARKER = "[]";

	private final LinkedListMultimap<String, String> valueMap = LinkedListMultimap.create();

	@Override
	public void startRecord(final String identifier) {
		valueMap.clear();
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		flushValues();
		getReceiver().endRecord();
	}

	@Override
	public void startEntity(final String name) {
		flushValues();
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		flushValues();
		getReceiver().endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		valueMap.put(name, value);
	}

	private void flushValues() {
		for (Map.Entry<String, Collection<String>> entry : valueMap.asMap().entrySet()) {
			final String name = entry.getKey();
			final boolean isMulti = entry.getValue().size() > 1;
			if (isMulti) {
				getReceiver().startEntity(name + ARRAY_MARKER);
			}
			for (String value : entry.getValue()) {
				getReceiver().literal(name, value);
			}
			if (isMulti) {
				getReceiver().endEntity();
			}
		}

		valueMap.clear();
	}
}
