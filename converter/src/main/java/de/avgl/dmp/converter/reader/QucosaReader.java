package de.avgl.dmp.converter.reader;

import de.avgl.dmp.converter.decoder.QucosaDecoder;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.reader.Reader;

public final class QucosaReader implements Reader {

	private final QucosaDecoder decoder;

	public QucosaReader() {
		super();
		this.decoder = new QucosaDecoder();
	}

	@Override
	public final <R extends StreamReceiver> R setReceiver(final R receiver) {
		decoder.setReceiver(receiver);
		return receiver;
	}

	@Override
	public final void process(final java.io.Reader reader) {
		decoder.process(reader);
	}

	@Override
	public final void read(final String entry) {
		decoder.process(entry);
	}

	@Override
	public final void resetStream() {
		decoder.resetStream();
	}

	@Override
	public final void closeStream() {
		decoder.closeStream();
	}
}
