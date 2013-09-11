package de.avgl.dmp.converter.util;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class NodeListIterableTest {
	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private Node node1;
	private Node node2;
	private Node node3;
	private NodeList nodeList;

	@Before
	public void setUp() throws Exception {
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = documentBuilder.newDocument();
		Node root = doc.createElement("root");

		node1 = doc.createElement("tag1");
		node1.setTextContent("text1");
		node2 = doc.createElement("tag2");
		node2.setTextContent("text2");
		node3 = doc.createElement("tag3");
		node3.setTextContent("text3");

		root.appendChild(node1);
		root.appendChild(node2);
		root.appendChild(node3);

		nodeList = root.getChildNodes();
	}

	@Test
	public void testIterator() throws Exception {
		Iterator<Node> nodes = Lists.newArrayList(node1, node2, node3).iterator();

		for (Node node : new NodeListIterable(nodeList)) {
			assertEquals(nodes.next(), node);
		}

		assertFalse(nodes.hasNext());
	}
}
