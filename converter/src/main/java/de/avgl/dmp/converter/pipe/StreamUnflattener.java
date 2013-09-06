package de.avgl.dmp.converter.pipe;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;

import java.util.HashMap;
import java.util.Map;

public class StreamUnflattener extends DefaultStreamPipe<StreamReceiver> {

	public static final String DEFAULT_ENTITY_MARKER = ".";

	private final Map<Integer, String> openEntities = new HashMap<>();
	private int currentLevel = 0;

	private final String entityMarker;
	private final String initialDiscard;


	public String getEntityMarker() {
		return entityMarker;
	}

	public String getInitialDiscard() {
		return initialDiscard;
	}

	public StreamUnflattener(final String initialDiscard, final String entityMarker) {
		this.entityMarker = entityMarker;
		this.initialDiscard = initialDiscard;
	}

	public StreamUnflattener(String initialDiscard) {
		this(initialDiscard, DEFAULT_ENTITY_MARKER);
	}

	public StreamUnflattener() {
		this("");
	}

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		openEntities.clear();
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		for (int i = currentLevel; i -- > 0; ) {
			getReceiver().endEntity();
		}
		currentLevel = 0;
		getReceiver().endRecord();
	}

	@Override
	public void startEntity(final String name) {
		throw new IllegalStateException(getClass().getName() + " has to be called on a flat stream, e.g. as produces by StreamFlattener");
	}

	@Override
	public void endEntity() {
		throw new IllegalStateException(getClass().getName() + " has to be called on a flat stream, e.g. as produces by StreamFlattener");
	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		final String[] es = name.split("\\.");

		int l = es.length;

		for (int i = 0; i < l; i++) {
			String entity = es[i];

			if (i + 1 == l) {
				getReceiver().literal(entity, value);
				continue;
			}

			if (i == 0 && entity.equals(initialDiscard)) {
				continue;
			}

			if (entity.equals(openEntities.get(i))) {
				continue;
			}

			for (int ii = currentLevel; ii --> i ;) {
				getReceiver().endEntity();
			}

			if (openEntities.containsKey(i)) {
				getReceiver().endEntity();
			}

			currentLevel = i;
			openEntities.put(i, entity);

			getReceiver().startEntity(entity);
		}
	}
}
