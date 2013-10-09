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
	public QucosaReader(final String recordPrefix) {
		this(new QucosaDecoder(recordPrefix));
	}

	/**
	 * Class Constructor, setting up the default {@link QucosaDecoder}.
	 * QucosaReader implements {@link Reader} by deferring all operations to
	 * this QucosaDecoder.
	 */
	public QucosaReader() {
		this(new QucosaDecoder());
	}

	/**
	 * Class Constructor, allowing for injecting a custom QucosaDecoder.
	 */
	public QucosaReader(final QucosaDecoder decoder) {
		super();
		this.decoder = decoder;
	}

	@Override
	public <R extends StreamReceiver> R setReceiver(final R receiver) {
		decoder.setReceiver(receiver);
		return receiver;
	}

	@Override
	public void process(final java.io.Reader reader) {
		decoder.process(reader);
	}

	@Override
	public void read(final String entry) {
		decoder.process(entry);
	}

	@Override
	public void resetStream() {
		decoder.resetStream();
	}

	@Override
	public void closeStream() {
		decoder.closeStream();
	}
}
