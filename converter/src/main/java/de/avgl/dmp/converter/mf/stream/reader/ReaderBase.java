package de.avgl.dmp.converter.mf.stream.reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.reader.Reader;

import de.avgl.dmp.converter.mf.stream.converter.LineReader;


/**
 * Inspired by org.culturegraph.mf.stream.reader.ReaderBase
 * 
 * @author tgaengler
 *
 * @param <D> type of the decoder used
 */
public class ReaderBase<D extends ObjectPipe<String, StreamReceiver>> implements Reader { 
	private final LineReader lineReader;
	private final D decoder;
	
	public ReaderBase(final D decoder) {
		super();
		
		this.decoder = decoder;
		lineReader = new LineReader();
		lineReader.setReceiver(this.decoder);
	}
	
	public ReaderBase(final D decoder, final String lineEnding) {
		super();
		
		this.decoder = decoder;
		lineReader = new LineReader(lineEnding);
		lineReader.setReceiver(this.decoder);
	}
	
	public final D getDecoder() {
		return decoder;
	}

	@Override
	public final <R extends StreamReceiver> R setReceiver(final R receiver) {
		decoder.setReceiver(receiver);
		return receiver;
	}
	
	@Override
	public final void process(final java.io.Reader reader) {
		lineReader.process(reader);
	}

	@Override
	public final void read(final String entry) {
		
		decoder.process(entry);
	}

	@Override
	public final void resetStream() {
		lineReader.resetStream();
	}
	
	@Override
	public final void closeStream() {
		lineReader.closeStream();
	}
	
}