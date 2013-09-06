package de.avgl.dmp.converter.reader;

import de.avgl.dmp.converter.decoder.QucosaDecoder;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.reader.Reader;

/**
 * Read a Qucosa OAI-PMH file. This class implements Reader directly instead of
 *   extending e.g. ReaderBase b/c implementations would emit records line wise
 *   while the Qucosa record is one XML document per file.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
@Description("Read a Qucosa OAI=PMH file. Complete file is interpreted as one record. Provide the record tag if necessary.")
@In(Reader.class)
@Out(StreamReceiver.class)
public final class QucosaReader implements Reader {

	private final QucosaDecoder decoder;

	/**
	 * Class Constructor setting the <code>recordPrefix</code>
	 *
	 * @param recordPrefix  the prefix that will be used to identify the
	 *                      relevant record section. See {@link QucosaDecoder}.
	 */
	public QucosaReader(String recordPrefix) {
		super();
		this.decoder = new QucosaDecoder(recordPrefix);
	}

	/**
	 * Class Constructor.  QucosaReader implements {@link Reader} by deferring
	 *   all operations to {@link QucosaDecoder}.
	 */
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
