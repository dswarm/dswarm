package de.avgl.dmp.converter.decoder;

import de.avgl.dmp.converter.functional.Function1;
import de.avgl.dmp.converter.functional.NodeListOps;
import de.avgl.dmp.converter.functional.UnitFunction1;
import de.avgl.dmp.converter.functional.UnitFunction2;
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

/**
 * Decode a OAI-PMH file containing a Qucosa record.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
@Description("Decode a OAI-PMH file containing a Qucosa record.")
@In(Reader.class)
@Out(StreamReceiver.class)
public final class QucosaDecoder extends DefaultObjectPipe<Reader, StreamReceiver> {

	private static final String RECORD_TAG = "record";
	private static final String HEADER_TAG = "header";
	private static final String METADATA_TAG = "metadata";
	private static final String OAI_DATA_TAG = "oai_dc:dc";
	private static final String ENTITY_MARKER = ".";

	static final String DEFAULT_RECORD_PREFIX = "record";

	private final String recordPrefix;

	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

	private class Emitter implements UnitFunction2<NodeListOps, String> {

		private final StreamReceiver receiver;

		public Emitter(StreamReceiver receiver) {
			this.receiver = receiver;
		}

		@Override
		public void apply(NodeListOps node, String section) {
			String[] sections = section.split("\\.");

			for (String s : sections) {
				receiver.startEntity(s);
			}

			node.filter(new Function1<Node, Boolean>() {
				@Override
				public Boolean apply(Node obj) {
					return obj.getNodeType() == Node.ELEMENT_NODE;
				}
			}).forEach(new UnitFunction1<Node>() {
				@Override
				public void apply(Node in) {
					receiver.literal(
							in.getNodeName(),
							in.getTextContent());
				}
			});

			for (int i = sections.length; i --> 0 ;) {
				receiver.endEntity();
			}
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

	private void processOneRecord(final Node record, Emitter emit) {
		getReceiver().startEntity(recordPrefix);

		final NodeListOps recordChildren = new NodeListOps(record.getChildNodes());

		final Node header = recordChildren.getElementByTagName(HEADER_TAG);
		final Node metadata = recordChildren.getElementByTagName(METADATA_TAG);

		final NodeListOps headerNodes = new NodeListOps(header.getChildNodes());

		emit.apply(headerNodes, HEADER_TAG);

		final NodeListOps metadataNodes = new NodeListOps(metadata.getChildNodes());
		final Node oaiDc = metadataNodes.getElementByTagName(OAI_DATA_TAG);

		final NodeListOps dcNodes = new NodeListOps(oaiDc.getChildNodes());

		emit.apply(dcNodes, METADATA_TAG + ENTITY_MARKER + OAI_DATA_TAG);

		getReceiver().endEntity();
	}

	/**
	 * Process the Qucosa file. Parse the XML and for evert occurrence of
	 * <code>recordPrefix</code>, send a new record downstream.
	 * @param in  The {@link InputSource}  for the XML file.
	 */
	public void process(final InputSource in) {
		final DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new MetafactureException(e);
		}

		final Document doc;
		try {
			doc = documentBuilder.parse(in);
		} catch (SAXException | IOException e) {
			throw new MetafactureException(e);
		}

		final StreamReceiver receiver = getReceiver();
		final Emitter emit = new Emitter(receiver);

		doc.getDocumentElement().normalize();

		new NodeListOps(doc.getElementsByTagName(RECORD_TAG)).forEach(new UnitFunction1<Node>() {
			@Override
			public void apply(Node in) {
				receiver.startRecord("");
				processOneRecord(in, emit);
				receiver.endRecord();
			}
		});
	}

	/**
	 * Process a Qucosa file provide as String (where the string denotes the
	 *   actual file content, not the filename.)
	 * @param content  The textual content of the Qucosa file
	 */
	public void process(final String content) {
		InputStream is;
		try {
			is = new ByteArrayInputStream(content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
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
		InputSource in = new InputSource(reader);
		process(in);
	}
}
