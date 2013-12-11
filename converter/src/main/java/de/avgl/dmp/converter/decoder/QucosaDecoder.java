package de.avgl.dmp.converter.decoder;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import de.avgl.dmp.converter.util.NodeListIterable;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import static de.avgl.dmp.converter.util.NodeListOps.getChildrenFor;

/**
 * Decode a OAI-PMH file containing a Qucosa record.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
@Description("Decode a OAI-PMH file containing a Qucosa record.")
@In(Reader.class)
@Out(StreamReceiver.class)
public class QucosaDecoder extends DefaultObjectPipe<Reader, StreamReceiver> {

	public static final String RECORD_TAG = "record";
	public static final String HEADER_TAG = "header";
	public static final String METADATA_TAG = "metadata";
	public static final String OAI_DATA_TAG = "oai_dc:dc";
	public static final char ENTITY_MARKER = '.';

	private static final String DEFAULT_RECORD_PREFIX = "record";

	private final String recordPrefix;

	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();


	private void emit(final Iterable<Node> nodes, final String section) {
		final Iterable<String> sections = Splitter.on(ENTITY_MARKER).split(section);

		for (final String s : sections) {
			getReceiver().startEntity(s);
		}

		for (final Node node : nodes) {
			getReceiver().literal(node.getNodeName(), node.getTextContent());
		}

		for (final String ignored : sections) {
			getReceiver().endEntity();
		}
	}

	/**
	 * Class Constructor setting the <code>recordPrefix</code>
	 *
	 * @param recordPrefix  the prefix that will be used to identify the
	 *                      relevant record section.
	 */
	public QucosaDecoder(final String recordPrefix) {
		super();
		this.recordPrefix = recordPrefix;
	}

	/**
	 * Class Constructor setting the <code>DEFAULT_RECORD_PREFIX</code> for
	 * <code>recordPrefix</code>
	 */
	public QucosaDecoder() {
		this(DEFAULT_RECORD_PREFIX);
	}

	private void processOneRecord(final Node record) {
		getReceiver().startEntity(recordPrefix);

		final NodeListIterable recordChildren = new NodeListIterable(record.getChildNodes());

		final Optional<Iterable<Node>> headerNodes = getChildrenFor(recordChildren, HEADER_TAG);

		if (headerNodes.isPresent()) {
			emit(headerNodes.get(), HEADER_TAG);
		}


		final Optional<Iterable<Node>> metadataNodes = getChildrenFor(recordChildren, METADATA_TAG);

		if (metadataNodes.isPresent()) {
			final Optional<Iterable<Node>> dcNodes = getChildrenFor(metadataNodes.get(), OAI_DATA_TAG);

			if (dcNodes.isPresent()) {
				emit(dcNodes.get(), Joiner.on(ENTITY_MARKER).join(METADATA_TAG, OAI_DATA_TAG));
			}
		}

		getReceiver().endEntity();
	}

	/**
	 * Process the Qucosa file. Parse the XML and for evert occurrence of
	 * <code>recordPrefix</code>, send a new record downstream.
	 * @param in  The {@link InputSource}  for the XML file.
	 */
	void process(final InputSource in) {
		final DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new MetafactureException(e);
		}

		final Document doc;
		try {
			doc = documentBuilder.parse(in);
		} catch (final SAXException | IOException e) {
			throw new MetafactureException(e);
		}


		doc.getDocumentElement().normalize();

		for (final Node node : new NodeListIterable(doc.getElementsByTagName(RECORD_TAG))) {
			getReceiver().startRecord("");
			processOneRecord(node);
			getReceiver().endRecord();
		}
	}

	/**
	 * Process a Qucosa file provide as String (where the string denotes the
	 *   actual file content, not the filename.)
	 * @param content  The textual content of the Qucosa file
	 */
	public void process(final String content) {
		final InputStream is;
		try {
			is = new ByteArrayInputStream(content.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			throw new MetafactureException(e);
		}

		final InputSource in = new InputSource(is);
		process(in);
	}

	/**
	 * Process a Qucosa file provide as Reader. This is the method that will be
	 * used in a flow/flux context.
	 *
	 * @param reader  A {@link Reader} pointing to the Qucosa file
	 */
	@Override
	public void process(final Reader reader) {
		final InputSource in = new InputSource(reader);
		process(in);
	}
}
